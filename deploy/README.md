# Deploy and Local Run Guide

Инструкция описывает локальный запуск FitBridge через Docker Compose. Текущий runtime поднимает статический прототип приложения на Nginx за Envoy, Keycloak, OpenSearch, OpenSearch Dashboards и Fluent Bit.

## Состав стенда

| Сервис | Назначение | Порт хоста |
|--------|------------|------------|
| `app` | Nginx со статическим прототипом и тестовыми API-заглушками | через Envoy `8080` |
| `envoy` | входной proxy, маршрутизация и JWT-проверка для `/v1/*` | `8080` |
| `keycloak` | Identity Server, импорт realm `fit-bridge` | через Envoy `/admin`, `/realms` |
| `opensearch` | хранилище логов | `9200`, `9600` |
| `dashboards` | OpenSearch Dashboards | `5601` |
| `fluent-bit` | доставка логов контейнеров в OpenSearch | `24224`, `2020` |

## Требования

- Docker Desktop или Docker Engine.
- Docker Compose plugin (`docker compose version`).
- Свободные порты: `8080`, `5601`, `9200`, `9600`, `2020`, `24224`.
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

Первый запуск может занять несколько минут: Keycloak импортирует realm, OpenSearch проходит healthcheck, Dashboards ждёт готовности OpenSearch.

## Адреса

| Что открыть | URL | Доступ |
|-------------|-----|--------|
| Приложение | `http://localhost:8080/` | без авторизации |
| Keycloak Admin Console | `http://localhost:8080/admin/` | `admin` / `admin` |
| Keycloak realm | `http://localhost:8080/realms/fit-bridge` | публичный realm endpoint |
| OpenSearch | `https://localhost:9200` | `admin` / `adm-Password123!` |
| OpenSearch Dashboards | `http://localhost:5601` | `admin` / `adm-Password123!` |
| Fluent Bit health/metrics | `http://localhost:2020` | без авторизации |

Для OpenSearch используется self-signed certificate, поэтому при проверке через CLI нужен `-k`:

```powershell
curl.exe -k -u admin:adm-Password123! https://localhost:9200/_cluster/health?pretty
```

## Проверка приложения

Проверить главную страницу:

```powershell
curl.exe http://localhost:8080/
```

Проверить защищённый endpoint через helper-скрипт:

```bash
cd deploy
./call-envoy.sh
```

`call-envoy.sh` получает token через `keycloak-tokens.sh` для тестового пользователя:

- realm: `fit-bridge`
- client: `fit-bridge-service`
- username: `fitbridge-test`
- password: `fitbridge`

Если запускаете из PowerShell без Bash/JQ, можно просто открыть приложение и Keycloak в браузере; helper-скрипты не обязательны для старта стенда.

## Логи

Все логи контейнеров:

```powershell
docker compose logs -f
```

Логи конкретного сервиса:

```powershell
docker compose logs -f app
docker compose logs -f envoy
docker compose logs -f keycloak
docker compose logs -f opensearch
docker compose logs -f fluent-bit
```

Логи приложения также отправляются через Fluent Bit в OpenSearch. Для просмотра через UI откройте OpenSearch Dashboards: `http://localhost:5601`.

## Остановка

Остановить контейнеры без удаления данных OpenSearch:

```powershell
docker compose down
```

Остановить и удалить volume с данными OpenSearch:

```powershell
docker compose down -v
```

Используйте `-v`, если нужен полностью чистый стенд или OpenSearch не поднимается из-за повреждённого локального volume.

## Повторная сборка

После изменения файлов Nginx, HTML или compose-конфигурации:

```powershell
docker compose up --build -d
```

Если менялись только файлы в `deploy/volumes/nginx/html`, они смонтированы volume-ом и обычно достаточно обновить страницу в браузере.

## Типовые проблемы

**Порт уже занят**

Проверьте, кто занимает порт, и остановите конфликтующий процесс или поменяйте порт в `docker-compose.yml`.

```powershell
netstat -ano | findstr :8080
```

**OpenSearch долго стартует или unhealthy**

Посмотрите логи:

```powershell
docker compose logs -f opensearch
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

## Что считается успешным запуском

- `docker compose ps` показывает запущенные `app`, `envoy`, `keycloak`, `opensearch`, `dashboards`, `fluent-bit`.
- `http://localhost:8080/` возвращает страницу приложения.
- `http://localhost:8080/admin/` открывает Keycloak Admin Console.
- `https://localhost:9200/_cluster/health` возвращает статус `green` или `yellow`.
- `http://localhost:5601` открывает OpenSearch Dashboards.
