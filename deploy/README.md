# Локальный стенд FitBridge

`deploy/` — единственный источник Docker Compose, Envoy и Keycloak-конфигурации и каноническая операционная инструкция локального MVP-стенда. Архитектурные границы описаны в [03-arch.md](../docs/03-architecture/03-arch.md), [C4 Container source](../docs/03-architecture/c4/C4_CONTAINER.drawio), [Security Architecture](../docs/03-architecture/SECURITY_ARCHITECTURE.md) и [ADR-006](../docs/03-architecture/ADR/ADR-006-use-greptimedb-fluent-bit-observability.md).

Пошаговая ручная приёмка UI, авторизации, CORS/JWT, ownership и lifecycle API описана в [Manual Test Scenarios](../docs/05-testing/MANUAL_TEST_SCENARIOS.md).

## Состав

| Сервис | Назначение | Порт хоста |
|--------|------------|------------|
| `training-service` | Backend API для работы с клиентами и планами | REST через Envoy `/v1/clientCard/`, `/v1/trainingPlan/`, `/v2/clientCard/`, `/v2/trainingPlan/`; WS `/v1/training/ws`, `/v2/training/ws` |
| `postgresql` | основное хранилище данных приложения | `5432` |
| `liquibase-training` | применение схемы Training DB перед запуском приложения | без порта хоста |
| `envoy` | входной proxy, CORS/preflight, JWT validation и WebSocket Upgrade для MVP `/v1/*`, `/v2/*` | `8080` |
| `keycloak` | Identity Server, импорт realm `fit-bridge` | через Envoy `/admin`, `/realms` |
| `greptimedb` | хранилище masked logs и метрик, встроенный Dashboard | `4000`–`4003` |
| `fluent-bit` | доставка логов контейнеров в GreptimeDB | `24224`, `2020` |

Отдельного Profile Service и profile database нет.

## Keycloak realm

- realm: `fit-bridge`;
- browser client: `fit-bridge-web`;
- API audience: `fit-bridge-service`;
- local smoke client: `fit-bridge-smoke`;
- регистрация явно оформлена как регистрация тренера;
- username является логином и не редактируется пользователем;
- новые пользователи автоматически получают `TRAINER`;
- вход по email и клиентская роль отключены;
- локальный `verifyEmail` выключен, потому что SMTP не настроен.

Тестовый пользователь:

- username: `fitbridge-test`;
- password: `fitbridge`;
- role: `TRAINER`.

Helper использует Direct Access Grant только для локального smoke-теста. Пользовательский UI должен использовать Authorization Code Flow + PKCE.

## Запуск

```powershell
.\gradlew.bat --no-daemon buildInfra
cd deploy
docker compose up --build -d
docker compose ps
```

`buildInfra` собирает образ миграций. Затем Compose запускает Liquibase после готовности PostgreSQL и запускает Training Service только после успешного применения схемы. `training-service/app-ktor` публикуется только через Envoy.

## CORS и граница API

CORS настраивается только на внешней границе — в `volumes/envoy/envoy.yaml` для REST-маршрутов Training API. Envoy отвечает на разрешённые preflight-запросы до JWT-фильтра и добавляет CORS-заголовки к фактическим ответам. Внутренний Ktor-сервис не устанавливает CORS plugin и не публикует порт на хост, поэтому обращаться к нему в обход Envoy нельзя.

Локальный allowlist содержит `http://localhost:5173` (Vite dev server) и `http://localhost:4173` (Vite preview). При развёртывании production origin фронтенда нужно явно добавить в `allow_origin_string_match`; wildcard использовать не следует. CORS не является механизмом авторизации: каждый прикладной запрос по-прежнему обязан пройти проверку JWT в Envoy и проверку владельца/роли в приложении.

## Миграции Training DB

В PostgreSQL существует только прикладная база `training_db`. Identity-профиль остаётся в Keycloak, поэтому отдельной Profile DB и profile-миграций нет.

Исходники Liquibase находятся в `fit-bridge-other/fit-bridge-migration-pg/src/main/liquibase/training/`. Начальная схема создаёт `client_card` и `training_plan`, их ownership/search индексы, archive state и optimistic locks.

Повторный запуск безопасен: Liquibase сохраняет историю применённых changeset-ов в `training_db`.

Собрать только migration image из корня репозитория:

```powershell
.\gradlew.bat :fit-bridge-other:fit-bridge-migration-pg:buildImages
```

Повторно применить миграции или посмотреть историю из `deploy/`:

```powershell
docker compose run --rm liquibase-training
docker compose run --rm liquibase-training history
```

Для новой версии схемы нужно добавить новый formatted SQL-файл в каталог `training/` и подключить его из `changelog-master.yaml`; уже применённый changeset не редактируется.

Ручной workflow `.github/workflows/migrate.yml` использует secrets `TRAINING_DB_URL`, `TRAINING_DB_USER` и `TRAINING_DB_PASSWORD`. Он нужен только после появления доступного CI окружения базы данных.

Адреса:

- Keycloak: `http://localhost:8080/realms/fit-bridge`;
- Training health: `http://localhost:8080/health/training/ready`;
- Training WebSocket v1/v2: `ws://localhost:8080/v1/training/ws`, `ws://localhost:8080/v2/training/ws`;
- Admin Console: `http://localhost:8080/admin/` (`admin` / `admin`);
- GreptimeDB Dashboard: `http://localhost:4000/dashboard/`;
- Fluent Bit health: `http://localhost:2020`.

После изменения realm import существующий realm внутри уже созданного контейнера автоматически не заменяется. Для локальной проверки нужно пересоздать контейнер Keycloak либо применить изменения через Admin API.

## Проверка приложения

Проверить health endpoint GreptimeDB:

```powershell
curl.exe http://localhost:4000/health
```

WebSocket endpoints через Envoy:

| v1 | v2 |
|---|---|
| `ws://localhost:8080/v1/training/ws` | `ws://localhost:8080/v2/training/ws` |

Полный автоматизированный E2E-прогон не использует этот постоянно работающий
стенд. Следуйте [E2E runbook](../fit-bridge-be/fit-bridge-e2e-be/readme.md): Gradle
собирает инфраструктурный resource-артефакт и backend-образы, после чего
Testcontainers поднимает отдельный минимальный Compose-стенд на динамическом
порту и удаляет его после тестов.

Ожидаемая проверка доступности Backend API:

```powershell
curl.exe http://localhost:8080/health
curl.exe http://localhost:8080/health/training/ready
```

`/health` проверяет сам Envoy. Через `/health/training/{live|ready}` доступны проверки Training Service. Сейчас readiness означает, что приложение запущено и принимает запросы; проверка БД будет добавлена вместе с подключением репозиториев.

Проверка защищённого endpoint через helper-скрипт:

```bash
cd deploy
./keycloak-tokens.sh
./call-envoy.sh
```

`call-envoy.sh` проверяет защищённый REST endpoint. Для WebSocket Bearer token передаётся в HTTP Upgrade request; после подключения сервер отправляет `InitResponse`.

Ожидаемые claims access token:

```yaml
sub: <keycloak-user-id>
preferred_username: fitbridge-test
aud: fit-bridge-service
realm_access.roles: [TRAINER]
```

Имя и email должны возвращаться ID Token/UserInfo, но не access token.

## Проверки конфигурации

```powershell
docker compose config --quiet
docker compose config --services
```

Проверить preflight без JWT (ожидается `access-control-allow-origin: http://localhost:5173`):

```powershell
curl.exe -i -X OPTIONS http://localhost:8080/v2/clientCard/search `
  -H "Origin: http://localhost:5173" `
  -H "Access-Control-Request-Method: POST" `
  -H "Access-Control-Request-Headers: authorization,content-type"
```

Список сервисов не должен содержать `profile-service`.

## Остановка

```powershell
docker compose down
```

`docker compose down -v` дополнительно удаляет данные PostgreSQL и GreptimeDB и используется только для намеренного чистого старта.
