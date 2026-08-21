# Локальный стенд FitBridge

`deploy/` — единственный источник Docker Compose, Envoy и Keycloak-конфигурации и каноническая операционная инструкция локального MVP-стенда. Архитектурные границы описаны в [03-arch.md](../docs/03-architecture/03-arch.md), [C4 Container source](../docs/03-architecture/c4/C4_CONTAINER.drawio), [Security Architecture](../docs/03-architecture/SECURITY_ARCHITECTURE.md) и [ADR-006](../docs/03-architecture/ADR/ADR-006-use-greptimedb-fluent-bit-observability.md).

Пошаговая ручная приёмка UI, авторизации, CORS/JWT, ownership и lifecycle API описана в [Manual Test Scenarios](../docs/05-testing/MANUAL_TEST_SCENARIOS.md).

## Состав

| Сервис | Назначение | Порт хоста |
|--------|------------|------------|
| `training-service` | Backend API для работы с клиентами и планами | REST через Envoy `/v1/clientCard/`, `/v1/trainingPlan/`, `/v2/clientCard/`, `/v2/trainingPlan/`; WS `/v1/training/ws`, `/v2/training/ws` |
| `frontend` | production-сборка Vue, отдаваемая внутренним Nginx | только через Envoy `/` |
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

`buildInfra` собирает образ миграций. Затем Compose запускает Liquibase после готовности PostgreSQL и запускает Training Service только после успешного применения схемы. Compose также строит многоэтапный образ `frontend`: Node собирает Vue-приложение, а финальный Nginx-образ содержит только статические файлы и стартовый скрипт конфигурации. Ни Ktor, ни Nginx фронтенда не публикуют собственные порты на хост: оба доступны только через Envoy.

После запуска пользовательский интерфейс доступен по `http://localhost:8080/`. Vite (`npm run dev`) остаётся отдельным режимом разработки на `http://localhost:5173/`; он обращается к Envoy через proxy.

## Runtime-конфигурация фронтенда

Один и тот же образ фронтенда можно использовать на разных стендах. При каждом старте контейнер создаёт `/config.js` из переменных окружения; файл не кэшируется. Поддерживаются только публичные значения:

| Переменная | Значение по умолчанию | Назначение |
|---|---|---|
| `FITBRIDGE_API_BASE_URL` | `/v2` | путь к API через Envoy |
| `FITBRIDGE_KEYCLOAK_URL` | `http://localhost:8080` | публичный URL Keycloak через Envoy |
| `FITBRIDGE_KEYCLOAK_REALM` | `fit-bridge` | Keycloak realm |
| `FITBRIDGE_KEYCLOAK_CLIENT_ID` | `fit-bridge-web` | public OIDC client |

Для локального Compose `FITBRIDGE_KEYCLOAK_URL` берётся из `FITBRIDGE_PUBLIC_URL` и по умолчанию равен `http://localhost:8080`. Для тестового или production-домена необходимо одновременно настроить этот URL, redirect URI и Web Origins клиента `fit-bridge-web` в Keycloak, а также issuer/JWKS URI JWT-провайдера Envoy. Это не секреты: access token и client secret в образ или `config.js` не помещаются.

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
- Frontend: `http://localhost:8080/`;
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

## Деплой на стенд через GitHub Actions

В проекте настроен автоматизированный CI/CD пайплайн (`.github/workflows/build.yml`), который собирает Docker-образы и публикует их в GitHub Container Registry, после чего запускает процесс обновления на удаленном сервере (стенде).

> [!NOTE]
> Пайплайн настроен так, что он автоматически срабатывает при пуше (или мерже) в ветку `main`. Но шаг деплоя защищен механизмом **GitHub Environments**, поэтому он дойдет до конца, встанет на паузу и будет ждать ручного нажатия кнопки "Approve" владельцем репозитория.

### Подготовка репозитория (Один раз)

Для того чтобы деплой заработал, нужно настроить доступы в GitHub:

#### 1. Настройка Environment (Ручное подтверждение деплоя)
1. В репозитории зайдите в **Settings** -> **Environments**.
2. Нажмите **New environment** и назовите его строго **`stand`**.
3. В настройках `stand` поставьте галочку **Required reviewers** и добавьте себя.

#### 2. Получение токена для GHCR
Серверу понадобится право скачивать собранные образы из GitHub.
1. В настройках вашего **аккаунта** GitHub зайдите в **Developer settings** -> **Personal access tokens** -> **Tokens (classic)**.
2. Создайте новый токен (Generate new token) без срока действия (No expiration).
3. Выдайте права: **`read:packages`** (и `write:packages`).
4. Скопируйте токен.

#### 3. Настройка Секретов
1. В настройках репозитория зайдите в **Settings** -> **Secrets and variables** -> **Actions**.
2. Создайте следующие **Repository secrets** (или *Environment secrets* внутри `stand`):
   - `SSH_HOST`: Публичный IP-адрес вашего стенда (например, `192.168.1.100`).
   - `SSH_USERNAME`: Имя пользователя (например, `root` или `ubuntu`).
   - `SSH_PRIVATE_KEY`: Приватный SSH ключ (содержимое файла `~/.ssh/id_rsa` или `~/.ssh/id_ed25519`). Убедитесь, что публичная часть (`.pub`) добавлена в файл `~/.ssh/authorized_keys` на стенде.
   - `CR_PAT`: Скопированный токен из предыдущего шага.

> [!TIP]
> **Приватные секреты (Опционально)**
> Чтобы не использовать стандартные (уязвимые) логины и пароли для баз данных на сервере, вы можете задать дополнительные секреты. GitHub Actions автоматически подхватит их и прокинет в `.env` файл на сервере:
> - Для суперпользователя PostgreSQL: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
> - Для БД Keycloak: `KC_DB_NAME`, `KC_DB_USERNAME`, `KC_DB_PASSWORD`
> - Для админа Keycloak: `KC_BOOTSTRAP_ADMIN_USERNAME`, `KC_BOOTSTRAP_ADMIN_PASSWORD`
> - Для Liquibase (владелец схемы): `LIQUIBASE_DB_USERNAME`, `LIQUIBASE_DB_PASSWORD`
> - Для БД микросервиса (только чтение/запись данных): `DB_NAME`, `DB_USER`, `DB_PASSWORD`
> - Для GreptimeDB: `GREPTIMEDB_USER`, `GREPTIMEDB_PASS`

### Как происходит деплой
- При успешном мерже в `main`, GitHub Action прогоняет E2E тесты.
- Образы `training-service` и `liquibase-training` получают тег с коротким хэшем коммита (Git SHA) и публикуются в GHCR.
- Экшен подключается по SSH к стенду, скачивает актуальные конфиги (`docker-compose.yml` и `docker-compose.stand.yml`).
- Выполняется `docker compose pull` и `docker compose up -d` с указанием конкретного `APP_VERSION`, гарантируя запуск именно той сборки, которая прошла тесты.
