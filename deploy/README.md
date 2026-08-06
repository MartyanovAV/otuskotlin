# Локальный стенд FitBridge

`deploy/` — единственный источник Docker Compose, Envoy и Keycloak-конфигурации. Gradle-задача `buildInfra` упаковывает этот каталог без отдельной копии.

## Состав

| Сервис | Назначение | Порт |
|---|---|---|
| `training-service` | REST/WS Backend API карточек и планов | через Envoy `/v1/*`, `/v2/*` |
| `postgresql` | `ClientCard` и `TrainingPlan` | `5432` |
| `envoy` | REST/WS routing и edge JWT validation | `8080` |
| `keycloak` | Registration/login/UserInfo/JWKS | через Envoy `/admin`, `/realms` |
| `greptimedb` | Masked logs и метрики | `4000`–`4003` |
| `fluent-bit` | Доставка логов | `24224`, `2020` |

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

## Smoke test

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
