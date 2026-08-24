# FitBridge E2E (Playwright) — UI-тесты SPA

Black-box тесты для тренерского SPA `fit-bridge-fe` через Playwright.
Проверяют реальный пользовательский путь: Keycloak-логин, навигация, формы,
диалоги, тосты.

## Требования

- Node.js 20+
- Поднятый dev-стек `deploy/docker-compose.local.yml` (envoy на `localhost:8080`,
  keycloak realm `fit-bridge` с пользователем `fitbridge-test` / `fitbridge`).
- (Опционально) JDK + Gradle для запуска через `./gradlew`.

## Быстрый старт

```bash
# Из корня модуля
npm ci
npx playwright install chromium
npx playwright test
```

Через Gradle (из корня репозитория):

```bash
./gradlew :fit-bridge-e2e-fe:e2eFe
# или с параметрами
./gradlew :fit-bridge-e2e-fe:e2eFe -Pe2eFe.BASE_URL=http://localhost:8080
```

## Переменные окружения

| Имя               | По умолчанию            | Назначение                       |
|-------------------|-------------------------|----------------------------------|
| `BASE_URL`        | `http://localhost:8080` | Origin SPA / Envoy               |
| `KEYCLOAK_URL`    | = `BASE_URL`            | Origin Keycloak                  |
| `TEST_USERNAME`   | `fitbridge-test`        | Логин тестового тренера          |
| `TEST_PASSWORD`   | `fitbridge`             | Пароль                            |
| `HEADED`          | не задан (= headless)   | `=1` запускает с UI-окном         |
| `CI`              | не задан                 | `=1` включает retries и сериал    |

## Структура

- `playwright.config.ts` — конфиг (один проект `chromium` + setup)
- `tests/auth.setup.ts` — одноразовый логин через Keycloak,
  результат сохраняется в `tests/fixtures/.auth/user.json`
- `tests/plans.spec.ts` — основные сценарии (создание DRAFT, confirm-диалог,
  навигация, логаут)
- `tests/fixtures/.auth/` — генерируется автоматически, в git не коммитится

## Что проверяется

- **Smoke**: залогиненный пользователь видит `/plans` и кнопку «Создать план»
- **Create flow**: можно создать DRAFT-план через «Сохранить как черновик»,
  он появляется в фильтре DRAFT
- **Confirm dialog**: клик «Активировать» открывает наш самописный диалог
  (а не нативный `window.confirm`) с правильным заголовком
- **Logout**: после «Выйти» редиректит на Keycloak
- **Navigation**: переходы между Планы/Клиенты сохраняют сессию

## Отчёты

HTML-отчёт с trace, video, screenshot для каждого упавшего теста:

```bash
npx playwright show-report
# или
./gradlew :fit-bridge-e2e-fe:e2eFeReport
```

## CI

`.github/workflows/e2e-fe.yml`:
1. Поднимает JDK 21, Node 20
2. Кеширует `~/.gradle`, `~/.npm`, `~/.cache/ms-playwright`
3. Билдит миграции + training-service образ
4. Поднимает `deploy/docker-compose.local.yml` через Gradle-таску `:fit-bridge-stack:stackUp`
5. Ждёт готовности через `:fit-bridge-stack:stackWaitForHealth` (чистый `curl --retry`)
6. Запускает `./gradlew :fit-bridge-be:fit-bridge-e2e-fe:e2eFe`
7. Аплоадит `playwright-report/` как артефакт

## Связь со стеком

Тесты ожидают поднятый локальный стек (`deploy/docker-compose.local.yml`).
Самый простой сценарий — использовать root-таску `e2eFeAll`, которая делает
`stackUpReady` + `e2eFe` одной командой:

```bash
./gradlew e2eFeAll
```
