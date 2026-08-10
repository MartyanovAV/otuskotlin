# Модуль E2E тестирования (End-to-End)

Этот модуль содержит black-box E2E-тесты развернутого FitBridge. Тесты отправляют запросы через Envoy в реальный контейнер `training-service`, а access token получают из Keycloak локального стенда.

## Как запустить тесты

Из корня проекта запустите канонический E2E-скрипт:

Windows (PowerShell 7):

```powershell
pwsh -NoProfile -File ./scripts/run-e2e.ps1
```

Linux:

```bash
bash ./scripts/run-e2e.sh
```

Он выполняет полный цикл в обязательном порядке:

1. валидирует `deploy/docker-compose.yml`;
2. собирает fat JAR (`shadowJar`) Training Service;
3. размещает артефакт как `deploy/training-service.jar`;
4. поднимает Compose ограниченными по времени фазами: storage → logging → identity → gateway → application services, причём Docker-образы приложений пересобираются из новых JAR;
5. после готовности зависимостей точечно пересоздаёт только Keycloak и Envoy, чтобы они перечитали bind-mounted realm и routing configuration; массовый `--force-recreate` намеренно не используется из-за зависимости application logging от уже работающего Fluent Bit;
6. проверяет публичные readiness endpoints;
7. запускает корневую задачу `e2eTests` с `--rerun-tasks`, чтобы black-box тесты гарантированно выполнялись заново.

Прямой запуск Gradle допустим только для повторного прогона, когда Compose-стенд уже заведомо собран из текущих артефактов и остаётся healthy:

```bash
./gradlew e2eTests --console=plain
```

Или напрямую для этого модуля:

```bash
./gradlew -p fit-bridge-be :fit-bridge-e2e-be:test --console=plain
```

Обе версии выполняют одинаковые шаги и намеренно оставляют локальный стенд запущенным для диагностики и повторных прогонов. Остановить его без удаления данных можно командой `docker compose --file deploy/docker-compose.yml down`. Удаление volumes через `down -v` не является частью обычного тестового цикла.

По умолчанию тесты обращаются к `http://localhost:8080` и используют локального пользователя `fitbridge-test` / `fitbridge`. Настройки можно переопределить переменными окружения:

- `FITBRIDGE_E2E_BASE_URL`;
- `FITBRIDGE_E2E_USERNAME`;
- `FITBRIDGE_E2E_PASSWORD`;
- `FITBRIDGE_E2E_CLIENT_ID`.

Аналогичные JVM properties имеют имена `fitbridge.e2e.baseUrl`, `fitbridge.e2e.username`, `fitbridge.e2e.password` и `fitbridge.e2e.clientId`.

## Что проверяется

- готовность Envoy и Training Service;
- обязательность JWT для API;
- получение JWT из Keycloak;
- реальные маршруты API v2 для `ClientCard` и `TrainingPlan`;
- тип, `requestId`, результат и содержимое каждого ответа.

Тесты используют внешние маршруты Envoy: `/v2/clientCard/*` и `/v2/trainingPlan/*`. WebSocket Training Service доступен через `/v1/training/ws` и `/v2/training/ws`.
