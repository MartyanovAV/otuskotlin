# Инструкция по локальному запуску и deploy

Операционная инструкция для локального MVP-стенда через Docker Compose. Архитектурные границы не дублируются: обзор см. в [03-arch.md](../docs/03-architecture/03-arch.md), контейнеры — в [C4 Container source](../docs/03-architecture/c4/C4_CONTAINER.drawio), security/JWT — в [Security Architecture](../docs/03-architecture/SECURITY_ARCHITECTURE.md), observability — в [ADR-006](../docs/03-architecture/ADR/ADR-006-use-greptimedb-fluent-bit-observability.md).

## Состав стенда

| Сервис | Назначение | Порт хоста |
|--------|------------|------------|
| `profile-service` | Backend API для работы с тренерами | через Envoy `/v1/trainerProfile/`, `/v2/trainerProfile/` |
| `training-service` | Backend API для работы с клиентами и планами | через Envoy `/v1/clientCard/`, `/v1/trainingPlan/`, `/v2/clientCard/`, `/v2/trainingPlan/` |
| `postgresql` | основное хранилище данных приложения | `5432` |
| `envoy` | входной proxy и JWT validation для MVP `/v1/*` | `8080` |
| `keycloak` | Identity Server, импорт realm `fit-bridge` | через Envoy `/admin`, `/realms` |
| `greptimedb` | хранилище masked logs и метрик, встроенный Dashboard | `4000`–`4003` |
| `fluent-bit` | доставка логов контейнеров в GreptimeDB | `24224`, `2020` |

## Требования

- Docker Desktop или Docker Engine.
- Docker Compose plugin (`docker compose version`).
- Свободные порты: `8080`, `5432`, `4000`–`4003`, `2020`, `24224`.
- Для helper-скриптов `*.sh`: Bash, `curl`, `jq`. На Windows удобно использовать Git Bash или WSL.

## Быстрый запуск

Из корня репозитория:

```powershell
cd deploy
docker compose up --build -d
```

Проверить состояние контейнеров:

```powershell
docker compose ps
```

Первый запуск может занять несколько минут: Keycloak импортирует realm, GreptimeDB проходит healthcheck, Fluent Bit ждёт готовности GreptimeDB.

## Адреса

| Что открыть | URL | Доступ |
|-------------|-----|--------|
| Приложение | `http://localhost:8080/` | без авторизации |

| Keycloak Admin Console | `http://localhost:8080/admin/` | `admin` / `admin` |
| Keycloak realm | `http://localhost:8080/realms/fit-bridge` | публичный realm endpoint |
| GreptimeDB Dashboard | `http://localhost:4000/dashboard/` | без авторизации |
| Fluent Bit health/metrics | `http://localhost:2020` | без авторизации |

Проверить health endpoint GreptimeDB:

```powershell
curl.exe http://localhost:4000/health
```

## Проверка приложения

Ожидаемая проверка доступности Backend API:

```powershell
curl.exe http://localhost:8080/health
```

Проверка защищённого endpoint через helper-скрипт:

```bash
cd deploy
./call-envoy.sh
```

`call-envoy.sh` получает token через `keycloak-tokens.sh` для тестового пользователя локального стенда:

- realm: `fit-bridge`
- client: `fit-bridge-service`
- username: `fitbridge-test`
- password: `fitbridge`

Если запускаете из PowerShell без Bash/JQ, можно просто открыть Keycloak в браузере; helper-скрипты не обязательны для старта стенда.

## Логи

Все логи контейнеров:

```powershell
docker compose logs -f
```

Логи конкретного сервиса:

```powershell
docker compose logs -f profile-service
docker compose logs -f training-service
docker compose logs -f postgresql
docker compose logs -f envoy
docker compose logs -f keycloak
docker compose logs -f greptimedb
docker compose logs -f fluent-bit
```

Логи приложения отправляются через Fluent Bit в GreptimeDB. Для просмотра откройте встроенный Dashboard: `http://localhost:4000/dashboard/`. Правила маскирования и события описаны в [ADR-006](../docs/03-architecture/ADR/ADR-006-use-greptimedb-fluent-bit-observability.md).

## Остановка

Остановить контейнеры без удаления данных GreptimeDB:

```powershell
docker compose down
```

Остановить и удалить volume с данными GreptimeDB:

```powershell
docker compose down -v
```

Используйте `-v`, если нужен полностью чистый стенд или GreptimeDB не поднимается из-за повреждённого локального volume.

## Повторная сборка

После изменения кода приложения или compose-конфигурации:

```powershell
docker compose up --build -d
```

При изменении конфигурации Envoy, Keycloak realm или других сервисов может потребоваться полная пересборка контейнеров.

## Типовые проблемы

**Порт уже занят**

Проверьте, кто занимает порт, и остановите конфликтующий процесс или поменяйте порт в `docker-compose.yml`.

```powershell
netstat -ano | findstr :8080
```

**GreptimeDB долго стартует или unhealthy**

Посмотрите логи:

```powershell
docker compose logs -f greptimedb
```

Для чистого перезапуска можно удалить volume:

```powershell
docker compose down -v
docker compose up --build -d
```

**Keycloak admin console не открывается**

Проверьте, что `keycloak` стал healthy и Envoy поднят:

```powershell
docker compose ps
docker compose logs -f keycloak envoy
```

**`call-envoy.sh` не работает на Windows**

Скрипт рассчитан на Bash и `jq`. Запустите его из Git Bash/WSL или проверьте сервисы вручную через браузер и `curl.exe`.

## Критерии успешного запуска

- `docker compose ps` показывает запущенные `profile-service`, `training-service`, `postgresql`, `envoy`, `keycloak`, `greptimedb`, `fluent-bit`.
- `http://localhost:8080/health` возвращает статус Backend API.
- `http://localhost:8080/admin/` открывает Keycloak Admin Console.
- `http://localhost:4000/health` возвращает HTTP 200.
- `http://localhost:4000/dashboard/` открывает GreptimeDB Dashboard.
- PostgreSQL доступен на порту `5432`. Суперпользователь: `fitbridge` / `fitbridge-password`. Сервисы используют отдельные БД: `profile_db` (user: `profile_user`) и `training_db` (user: `training_user`).
