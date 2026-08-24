# Модуль E2E тестирования (End-to-End)

Модуль содержит black-box тесты FitBridge. Они запускают изолированный Docker
Compose-стенд через Testcontainers, отправляют запросы через Caddy в реальный
`training-service`, а access token получают из Keycloak.

## Требования

- JDK 21;
- Docker Engine или Docker Desktop;
- Docker Compose plugin.

Свободные фиксированные host ports не требуются: Testcontainers публикует Caddy
на динамический порт. Локальный стенд из `deploy/docker-compose.yml` можно не
останавливать.

## Полный запуск

Команды выполняются строго последовательно из корня репозитория.

Windows:

```powershell
.\gradlew.bat --no-daemon buildInfra --console=plain
.\gradlew.bat --no-daemon buildImages --console=plain
.\gradlew.bat --no-daemon e2eTests --rerun-tasks --console=plain
```

Linux:

```bash
./gradlew --no-daemon buildInfra --console=plain
./gradlew --no-daemon buildImages --console=plain
./gradlew --no-daemon e2eTests --rerun-tasks --console=plain
```

Этапы выполняют следующее:

1. `buildInfra` собирает и проверяет resource ZIP модуля `fit-bridge-dcompose`;
2. `buildImages` создаёт актуальный `fitbridge-training-service:local` из fat JAR;
3. `e2eTests` распаковывает resource ZIP, один раз поднимает минимальный стенд
   PostgreSQL + Keycloak + Training Service + Caddy, ждёт readiness, запускает
   тесты и останавливает контейнеры с удалением тестовых volumes.

Если инфраструктура и образы не менялись, для повторного запуска достаточно
третьей команды. `--rerun-tasks` нужен, чтобы Gradle не вернул `UP-TO-DATE`
вместо нового black-box прогона.

Логин тестового пользователя по умолчанию: `fitbridge-test` / `fitbridge`.
Его можно переопределить переменными `FITBRIDGE_E2E_USERNAME`,
`FITBRIDGE_E2E_PASSWORD`, `FITBRIDGE_E2E_CLIENT_ID` или одноимёнными JVM
properties `fitbridge.e2e.username`, `fitbridge.e2e.password`,
`fitbridge.e2e.clientId`. Адрес Caddy устанавливает Testcontainers.

## Что проверяется

- готовность Caddy и Training Service;
- обязательность JWT для API;
- получение JWT из Keycloak;
- реальные маршруты API v2 для `ClientCard` и `TrainingPlan`;
- тип, `requestId`, результат и содержимое каждого ответа.

Тесты используют внешние маршруты Caddy: `/v2/clientCard/*` и `/v2/trainingPlan/*`. WebSocket Training Service доступен через `/v1/training/ws` и `/v2/training/ws`.
