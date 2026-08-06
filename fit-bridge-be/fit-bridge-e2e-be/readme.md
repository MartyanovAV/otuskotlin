# Модуль E2E тестирования (End-to-End)

Этот модуль содержит black-box E2E-тесты развернутого FitBridge. Тесты отправляют запросы через Envoy в реальный контейнер `training-service`, а access token получают из Keycloak локального стенда.

## Как запустить тесты

Сначала соберите артефакты и поднимите стенд:

```bash
cd deploy
docker compose up -d --build --wait
cd ..
```

После этого запустите все E2E-тесты из корня проекта:

```bash
./gradlew e2eTests
```
Или напрямую:
```bash
./gradlew :fit-bridge-be:fit-bridge-e2e-be:test
```

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
