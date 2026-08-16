# Локальный стенд FitBridge

`deploy/` — единственный источник Docker Compose, Envoy и Keycloak-конфигурации. Gradle-задача `buildInfra` упаковывает этот каталог без отдельной копии.

## Состав

| Сервис | Назначение | Порт |
|---|---|---|
| `training-service` | Backend API карточек и планов | через Envoy `/v1/*`, `/v2/*` |
| `postgresql` | `ClientCard` и `TrainingPlan` | `5432` |
| `envoy` | Routing и edge JWT validation | `8080` |
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

На текущей `main` Ktor-модуль `training-service/app-ktor` ещё не находится в tracked sources. Поэтому инфраструктурные сервисы и realm можно проверять отдельно, но сборка backend image станет доступна после реализации runtime-модуля.

```powershell
docker compose up -d keycloak envoy greptimedb fluent-bit postgresql
```

Адреса:

- Keycloak: `http://localhost:8080/realms/fit-bridge`;
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
