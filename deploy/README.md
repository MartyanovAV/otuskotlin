# Локальный стенд FitBridge

`deploy/` — единственный источник Docker Compose, Envoy и Keycloak-конфигурации и каноническая операционная инструкция локального MVP-стенда. Архитектурные границы описаны в [03-arch.md](../docs/03-architecture/03-arch.md), [C4 Container source](../docs/03-architecture/c4/C4_CONTAINER.drawio), [Security Architecture](../docs/03-architecture/SECURITY_ARCHITECTURE.md) и [ADR-006](../docs/03-architecture/ADR/ADR-006-use-greptimedb-fluent-bit-observability.md).

## Состав

| Сервис | Назначение | Порт хоста |
|--------|------------|------------|
| `training-service` | Backend API для работы с клиентами и планами | REST через Envoy `/v1/clientCard/`, `/v1/trainingPlan/`, `/v2/clientCard/`, `/v2/trainingPlan/`; WS `/v1/training/ws`, `/v2/training/ws` |
| `postgresql` | основное хранилище данных приложения | `5432` |
| `envoy` | входной proxy, JWT validation и WebSocket Upgrade для MVP `/v1/*`, `/v2/*` | `8080` |
| `keycloak` | Identity Server, импорт realm `fit-bridge` | через Envoy `/admin`, `/realms` |
| `greptimedb` | хранилище masked logs и метрик, встроенный Dashboard | `4000`–`4003` |
| `fluent-bit` | доставка логов контейнеров в GreptimeDB | `24224`, `2020` |

Отдельного Profile Service и profile database нет.

## Keycloak realm

- realm: `fit-bridge`;
- client: `fit-bridge-service`;
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
cd deploy
docker compose up --build -d
docker compose ps
```

Compose собирает `training-service/app-ktor` и публикует его только через Envoy.

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

Список сервисов не должен содержать `profile-service`.

## Остановка

```powershell
docker compose down
```

`docker compose down -v` дополнительно удаляет данные PostgreSQL и GreptimeDB и используется только для намеренного чистого старта.
