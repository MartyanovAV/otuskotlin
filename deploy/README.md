# Окружения FitBridge: Local и Production

Каталог `deploy/` содержит канонические конфигурации Docker Compose, Envoy и Keycloak для двух контуров:
1. **`local` (Локальный стенд разработчика)** — оптимизирован для быстрой отладки, сборки из исходников, локального Vite dev-сервера, открытых портов баз данных/метрик и тестовых учетных записей.
2. **`prod` (Боевой контур / Публичный стенд)** — настроен со строгими production-ограничениями (immutable OCI-образы из GHCR, resource limits, container hardening, закрытые порты внутренних БД, отсутствие тестовых учетных записей в Keycloak, строгий HTTPS).

Архитектурные границы описаны в [03-arch.md](../docs/03-architecture/03-arch.md), [C4 Container](../docs/architecture/c4-containers.md), [Security Architecture](../docs/03-architecture/SECURITY_ARCHITECTURE.md) и [ADR-006](../docs/03-architecture/ADR/ADR-006-use-greptimedb-fluent-bit-observability.md).

---

## 1. Состав сервисов

| Сервис | Назначение | Local (порт хоста) | Prod (порт хоста) |
|---|---|---|---|
| `envoy` | Входной Edge-шлюз, TLS, CORS, JWT-валидация, WS Upgrade | `8080` (Keycloak `/admin` открыт) | `80` (HTTP/ACME/301), `443` (HTTPS) |
| `certbot` | Авто-выпуск и продление сертификатов Let's Encrypt | — | Без внешнего порта (one-shot / cron) |
| `certbot-helper` | Микро-сервер раздачи ACME-челленджей | — | Внутренний порт 80 |
| `frontend` | Production-сборка Vue SPA под Nginx | Только через Envoy `/` | Только через Envoy `/` |
| `training-service` | Backend API (Ktor / Kotlin) | Только через Envoy `/v1/*`, `/v2/*` | Только через Envoy `/v1/*`, `/v2/*` |
| `keycloak` | Identity Provider, OIDC/JWKS | Публичные пути через Envoy, `http://localhost:8080/admin` | Публичные пути через Envoy, Admin через SSH-туннель |
| `postgresql` | Основная БД приложения и Keycloak | `127.0.0.1:5432` | **Закрыт** (только внутренняя Docker-сеть) |
| `liquibase-training` | Применение миграций схемы `training_db` | Без порта хоста (одноразовый) | Без порта хоста (одноразовый) |
| `greptimedb` | Хранилище логов и метрик | `127.0.0.1:4000–4003` (Dashboard) | **Закрыт** (только внутренняя Docker-сеть) |
| `fluent-bit` | Сбор и доставка логов контейнеров | `127.0.0.1:24224`, `2020` | `127.0.0.1:24224` (только daemon logger) |

---

## 2. Разграничение окружений

### Среда `local` (Локальная разработка)
- **Конфигурация:** `docker-compose.yml` + `docker-compose.local.yml`.
- **Сборка:** локально из исходников (`Dockerfile.app`, `Dockerfile` фронтенда).
- **Keycloak Realm:** импортируется из `volumes/keycloak/import/`:
  - Содержит тестового пользователя: `fitbridge-test` / `fitbridge` (роль `TRAINER`).
  - Включен тестовый клиент `fit-bridge-smoke` с `Direct Access Grants` (Resource Owner Password) для curl-скриптов.
  - Web Admin Console доступна в браузере по `http://localhost:8080/admin`.
- **CORS:** разрешены `http://localhost:5173` (Vite dev), `http://localhost:4173` (Vite preview), `http://localhost:8080`.

### Среда `prod` (Боевой контур / Публичный стенд)
- **Конфигурация:** `docker-compose.yml` + `docker-compose.prod.yml`.
- **Сборка:** запуск только готовых OCI-образов из GHCR (`ghcr.io/...:SHA`), собранных и проверенных в CI.
- **Keycloak Realm:** генерируется из шаблона `volumes/keycloak/import-prod/fit-bridge-realm.json.template`:
  - **Тестовые учетные записи полностью отсутствуют** (файл `fit-bridge-users-0.json` не монтируется).
  - Direct Access Grants **выключены** (только Authorization Code Flow + PKCE).
  - Включена защита от брутфорса (`bruteForceProtected: true`).
  - Маршруты `/admin` закрыты на уровне Envoy (404).
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
# 1. Сборка миграционного образа
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

Проверка токенов и защищенных эндпоинтов локально:
```bash
./keycloak-tokens.sh
./call-envoy.sh
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
   - Пароли: `POSTGRES_PASSWORD`, `KC_DB_PASSWORD`, `LIQUIBASE_DB_PASSWORD`, `DB_PASSWORD`, `KC_BOOTSTRAP_ADMIN_PASSWORD`, `GREPTIMEDB_PASS`.

### Процесс выкатки на сервере:
1. Скрипт `prepare-prod-config.sh` проверяет наличие всех секретов, подготавливает bootstrap dummy-сертификат (если боевой еще не выпущен) и генерирует `volumes/envoy/envoy.prod.yaml` и `volumes/keycloak/import-prod/fit-bridge-realm.json` из шаблонов без мутации tracked git-файлов.
2. Скачиваются образы конкретного коммита: `docker compose -f docker-compose.yml -f docker-compose.prod.yml pull`.
3. Запускаются сервисы: `docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-build`.

---

## 6. Управление SSL-сертификатами Let's Encrypt

В production-контуре Envoy терминирует HTTPS на порту `443` с автоматическим отслеживанием обновлений сертификатов через `watched_directory` (Zero-Downtime Hot-Reload без перезапуска контейнера).

### Первоначальный выпуск боевого сертификата:
1. Убедитесь, что DNS-запись (A/AAAA) вашего домена указывает на IP-адрес вашего боевого сервера.
2. Запустите стек:
   ```bash
   sh ./prepare-prod-config.sh
   docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-build
   ```
   *Envoy запустится на портах 80 и 443 с временным bootstrap-сертификатом.*
3. Запустите первоначальный выпуск боевого сертификата:
   ```bash
   sh ./certbot-init.sh
   ```
   *Certbot пройдет HTTP-01 challenge через порт 80, выпустит ECDSA-сертификат, и Envoy мгновенно на лету подхватит его без перезапуска.*

### Автоматическое продление (Auto-Renewal):
1. **Через встроенный контейнер `certbot`:** сервис `certbot` в `docker-compose.prod.yml` автоматически выполняет проверку и продление каждые 12 часов.
2. **Через Cron на хосте (с нулевым оверхедом RAM в простое):**
   Если вы хотите отключить фоновый контейнер certbot ради экономии оперативной памяти на слабом VPS, добавьте задачу в crontab сервера:
   ```bash
   # Выполнять проверку каждый понедельник в 03:00 ночи
   0 3 * * 1 /home/user/otuskotlin/deploy/certbot-renew.sh >> /var/log/certbot-renew.log 2>&1
   ```

### Ручная проверка и отладка:
```bash
# Проверка процесса продления в режиме dry-run (без расхода лимитов Let's Encrypt)
docker compose -f docker-compose.yml -f docker-compose.prod.yml run --rm \
  --entrypoint certbot certbot renew --dry-run --webroot -w /var/www/certbot
```

