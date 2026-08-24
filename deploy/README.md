# Окружения FitBridge: Local и Production

Каталог `deploy/` содержит канонические конфигурации Docker Compose, Caddy и Keycloak для двух контуров:
1. **`local` (Локальный стенд разработчика)** — оптимизирован для быстрой отладки, сборки из исходников, локального Vite dev-сервера, открытых портов баз данных/метрик и тестовых учетных записей.
2. **`prod` (Боевой контур / Публичный стенд)** — настроен со строгими production-ограничениями (immutable OCI-образы из GHCR, resource limits, container hardening, закрытые порты внутренних БД, отсутствие тестовых учетных записей в Keycloak, строгий HTTPS).

Caddy 2.11.4 — единый edge-прокси, заменивший Envoy + Certbot. Встроенный ACME-клиент выписывает и продлевает сертификаты Let's Encrypt без внешних скриптов, host-cron и reload-хуков. Плагин `caddy-jwt` v1.1.0 валидирует токены Keycloak по JWKS на защищённых путях. Детальное обоснование выбора — в `docs/03-architecture/ADR/` (после merge).

Архитектурные границы описаны в [03-arch.md](../docs/03-architecture/03-arch.md), [C4 Container](../docs/architecture/c4-containers.md), [Security Architecture](../docs/03-architecture/SECURITY_ARCHITECTURE.md) и [ADR-006](../docs/03-architecture/ADR/ADR-006-use-greptimedb-fluent-bit-observability.md).

---

## 1. Состав сервисов

| Сервис | Назначение | Local (порт хоста) | Prod (порт хоста) |
|---|---|---|---|
| `caddy` | Входной Edge-шлюз, TLS, ACME, CORS, JWT-валидация, WS Upgrade | `8080` (Keycloak `/admin` открыт) | `80` (HTTP→HTTPS), `443` (HTTPS + ACME) |
| `frontend` | Production-сборка Vue SPA под Nginx | Только через Caddy `/` | Только через Caddy `/` |
| `training-service` | Backend API (Ktor / Kotlin) | Только через Caddy `/v1/*`, `/v2/*` | Только через Caddy `/v1/*`, `/v2/*` |
| `keycloak` | Identity Provider, OIDC/JWKS | Публичные пути через Caddy, `http://localhost:8080/admin` | Публичные пути через Caddy, Admin через SSH-туннель |
| `postgresql` | Основная БД приложения и Keycloak | `127.0.0.1:5432` | **Закрыт** (только внутренняя Docker-сеть) |
| `liquibase-training` | Применение миграций схемы `training_db` | Без порта хоста (одноразовый) | Без порта хоста (одноразовый) |
| `greptimedb` | Хранилище логов и метрик | `127.0.0.1:4000–4003` (Dashboard) | **Закрыт** (только внутренняя Docker-сеть) |
| `fluent-bit` | Сбор и доставка логов контейнеров | `127.0.0.1:24224`, `2020` | `127.0.0.1:24224` (только daemon logger) |

Образ Caddy собирается через Gradle‑модуль `fit-bridge-other/fit-bridge-caddy` (таг `fitbridge-caddy:local` для E2E, `fitbridge-caddy:2.11.4-${APP_VERSION}` для prod), Dockerfile и Caddyfile лежат в `deploy/caddy/`.

---

## 2. Разграничение окружений

### Среда `local` (Локальная разработка)
- **Конфигурация:** `docker-compose.yml` + `docker-compose.local.yml`.
- **Сборка:** локально из исходников (`Dockerfile.app`, `Dockerfile` фронтенда, `deploy/caddy/Dockerfile`).
- **Caddyfile:** `deploy/volumes/caddy/Caddyfile.local` подключается в `docker-compose.local.yml` без подстановок; `auto_https off`, plain HTTP на `:8080`.
- **Keycloak Realm:** импортируется из `volumes/keycloak/import/`:
  - Содержит тестового пользователя: `fitbridge-test` / `fitbridge` (роль `TRAINER`).
  - Включен тестовый клиент `fit-bridge-smoke` с `Direct Access Grants` (Resource Owner Password) для curl-скриптов.
  - Web Admin Console доступна в браузере по `http://localhost:8080/admin`.
- **CORS:** разрешены `http://localhost:5173` (Vite dev), `http://localhost:4173` (Vite preview), `http://localhost:8080`.

### Среда `prod` (Боевой контур / Публичный стенд)
- **Конфигурация:** `docker-compose.yml` + `docker-compose.prod.yml`.
- **Сборка:** запуск только готовых OCI-образов из GHCR (`ghcr.io/...:SHA`), собранных и проверенных в CI. Caddy‑образ публикуется отдельным tag/push.
- **Keycloak Realm:** генерируется из шаблона `volumes/keycloak/import-prod/fit-bridge-realm.json.template`:
  - **Тестовые учетные записи полностью отсутствуют** (файл `fit-bridge-users-0.json` не монтируется).
  - Direct Access Grants **выключены** (только Authorization Code Flow + PKCE).
  - Включена защита от брутфорса (`bruteForceProtected: true`).
  - Маршруты `/admin` закрыты на уровне Caddy (404).
- **Caddyfile:** `deploy/volumes/caddy/Caddyfile.prod.template` подставляется в `deploy/volumes/caddy/Caddyfile.prod` через `prepare-prod-config.sh` (`sed`). Caddy сам выписывает и продлевает сертификат Let's Encrypt на первом старте, ACME state хранится в named volume `caddy-data`.
- **Сетевая безопасность:** порты PostgreSQL, GreptimeDB и Fluent Bit HTTP **не публикуются на хост**.
- **Лимиты ресурсов и Hardening:** для каждого контейнера заданы `cpu` и `memory` limits/reservations, `security_opt: ["no-new-privileges:true"]`, `restart: unless-stopped`.
- **Секреты:** обязательны стойкие пароли; fail-fast при их отсутствии.

---

## 3. Управление Keycloak (Admin Console)

1. **В локальном окружении (`local`)**:
   - Откройте в браузере `http://localhost:8080/admin`.
   - Логин: `admin`, пароль: `admin` (из `.env` / `docker-compose.yml`).

2. **В боевом окружении (`prod`)**:
   - Из внешней сети интернет админка закрыта.
   - **Подключение через безопасный SSH-туннель**:
     ```bash
     ssh -L 8081:localhost:8080 user@your-prod-server
     ```
     После этого откройте в локальном браузере: `http://localhost:8081/admin`.
   - **Управление через CLI `kcadm.sh`**:
     ```bash
     docker compose exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
       --server http://localhost:8080 --realm master --user admin --password "$KC_BOOTSTRAP_ADMIN_PASSWORD"
     ```

---

## 4. Запуск локального стенда

```powershell
# 1. Сборка миграционного и Caddy-образов
.\gradlew.bat --no-daemon buildInfra

# 2. Подготовка .env (при первом запуске)
cd deploy
cp .env.local.example .env

# 3. Запуск локального стека
docker compose -f docker-compose.yml -f docker-compose.local.yml up --build -d

# 4. Проверка статуса
docker compose -f docker-compose.yml -f docker-compose.local.yml ps
```

После запуска:
- Приложение доступно по адресу: `http://localhost:8080/`.
- Keycloak: `http://localhost:8080/realms/fit-bridge`.
- GreptimeDB Dashboard: `http://localhost:4000/dashboard/`.
- Training Service Health: `http://localhost:8080/health/training/ready`.

Проверка токенов и защищенных эндпoints локально:
```bash
./keycloak-tokens.sh
curl -X POST -H "Authorization: Bearer $(./keycloak-tokens.sh)" -H "Content-Type: application/json" \
  --data '{"requestType":"clientCard.search","clientCardFilter":{"pageSize":10,"pageNumber":1}}' \
  http://localhost:8080/v2/clientCard/search
```

Остановка локального стенда:
```powershell
docker compose -f docker-compose.yml -f docker-compose.local.yml down
```

---

## 5. Деплой на боевой контур (`prod`) через GitHub Actions

В проекте настроен автоматизированный CI/CD пайплайн (`.github/workflows/build.yml`), собирающий OCI-образы, публикующий их в GHCR и выполняющий безопасный деплой на сервер.

### Настройка GitHub Actions Environment:
1. В репозитории зайдите в **Settings** -> **Environments** -> **New environment** с именем **`prod`**.
2. Включите **Required reviewers** (ручной аппрув перед выкаткой).
3. Добавьте Environment Secrets:
   - `SSH_HOST`: IP-адрес сервера.
   - `SSH_USERNAME`: имя SSH-пользователя.
   - `SSH_PRIVATE_KEY`: приватный SSH-ключ.
   - `CR_PAT`: GitHub Personal Access Token с правом `read:packages`.
   - `FITBRIDGE_PUBLIC_URL`: боевой HTTPS URL (например, `https://fitbridge.example.com`).
   - `LETSENCRYPT_EMAIL`: контактный адрес ACME account.
   - Имена БД и пользователей: `POSTGRES_DB`, `POSTGRES_USER`, `KC_DB_NAME`, `KC_DB_USERNAME`, `LIQUIBASE_DB_USERNAME`, `DB_NAME`, `DB_USER`, `KC_BOOTSTRAP_ADMIN_USERNAME`, `GREPTIMEDB_USER`.
   - Пароли: `POSTGRES_PASSWORD`, `KC_DB_PASSWORD`, `LIQUIBASE_DB_PASSWORD`, `DB_PASSWORD`, `KC_BOOTSTRAP_ADMIN_PASSWORD`, `GREPTIMEDB_PASS`.

Все перечисленные значения обязательны: production deployment не использует fallback-логины и пароли. Значения сохраняются в shell-compatible `.env`; секреты с одинарной кавычкой отклоняются до изменения стека и должны быть ротированы.

`KC_BOOTSTRAP_ADMIN_USERNAME` и `KC_BOOTSTRAP_ADMIN_PASSWORD` создают
администратора только при первом запуске пустой базы Keycloak. Изменение GitHub
Secret не меняет пароль уже существующего администратора: пароль нужно сначала
ротировать через Keycloak Admin Console/`kcadm`, затем синхронно обновить secret.
Realm также импортируется автоматически только при первом создании; deploy
проверяет актуальность redirect URI, но не выполняет опасный full override
существующего realm.

### Процесс выкатки на сервере:
1. GitHub Actions и `flock` не допускают параллельные production-деплои. Полный production environment атомарно записывается в `.env` с правами `0600`, чтобы ручное восстановление не использовало небезопасные значения по умолчанию.
2. `prepare-prod-config.sh` подставляет шаблон Caddyfile.prod.template в Caddyfile.prod (sed для `LETSENCRYPT_EMAIL`, `FITBRIDGE_PUBLIC_URL`, `FITBRIDGE_PUBLIC_URL_HOST`). `validate-prod-config.sh` подтверждает, что merged Compose и сгенерированный Caddyfile валидны (через `caddy validate`).
3. Скачиваются immutable-образы конкретного commit SHA. Caddy‑образ публикуется отдельным tag/push (`ghcr.io/.../fitbridge-caddy:2.11.4-${COMMIT_SHA}` и `:latest`).
4. Caddy поднимается командой `docker compose ... up -d --no-build caddy` — на первом старте Caddy сам обращается к Let's Encrypt и выпускает сертификат, в дальнейшем продлевает его в фоне.
5. Устанавливается полный стек: `docker compose ... up -d --no-build` (с health/completion dependency). Caddy обеспечивает постоянную ACME-активность без участия host cron.
6. `verify-prod.sh` ожидает readiness и проверяет публичные HTTPS health endpoints, OIDC discovery, frontend, hostname сертификата и запас срока не менее семи дней.

---

## 6. Управление SSL-сертификатами Let's Encrypt

В production-контуре Caddy терминирует HTTPS на порту `443`. Сертификат выписывается и продлевается самим Caddy через встроенный ACME-клиент. Хранилище ACME state и выпущенных сертификатов — named volume `caddy-data`, монтируется в `/data/caddy` контейнера.

### Первоначальный выпуск боевого сертификата:
1. Убедитесь, что DNS-запись (A/AAAA) вашего домена указывает на IP-адрес боевого сервера.
2. Подготовьте и провалидируйте конфигурацию:
   ```bash
   sh ./prepare-prod-config.sh
   sh ./validate-prod-config.sh
   ```
3. Запустите полный стек — Caddy сам выпустит сертификат при первом старте:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-build
   sh ./verify-prod.sh
   ```

### Автоматическое продление (Auto-Renewal):
Caddy продлевает сертификаты внутри своего процесса, без участия host cron. Время продления — за 30 дней до истечения (как и для предыдущей связки certbot). Логи Caddy (`docker logs caddy`) показывают выпуски и продления.

### Ручная проверка и отладка:
```bash
# Проверка публичного сертификата
sh ./verify-prod.sh

# Тест рестарта Caddy: ACME state должен выжить в volume
docker compose restart caddy
curl --insecure -v https://fitbridge.example.com/ 2>&1 | grep -E 'subject|issuer|expire'
```

Ошибки Caddy логируются в `docker logs caddy`, но этого недостаточно как единственного канала контроля. Для production необходим внешний HTTPS monitor с предупреждением минимум за 21 день до истечения сертификата и критическим уведомлением за 7 дней.

### Rollback на Envoy (если нужен):
Caddyfile‑кэш Caddy переживает рестарт, поэтому после `git revert` миграции нужно остановить и удалить Caddy‑контейнер: `docker compose down caddy`. Конфиги `volumes/envoy/` удалены из репо; для полного отката восстановите их из git‑истории (`git log -- deploy/volumes/envoy/`); `docker-compose.prod.yml` нужно вернуть к точке, в которой использовался `envoyproxy/envoy:v1.39.0`. Git revert PR с миграцией + ручной `docker compose up -d --no-build envoy` восстанавливает работу.