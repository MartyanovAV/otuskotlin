---
name: backend-verification
description: Select and run the smallest reliable Gradle verification ladder for FitBridge backend changes, escalating from an affected module to a service, shared builds, all backend services, and E2E only when the change risk requires it
---

# Backend Verification

Проверяй backend от узкого и быстрого уровня к широкому. Не запускай `clean` по умолчанию: используй его только при подтверждённой проблеме с устаревшими артефактами или кэшем.

## 1. Определи область изменения

- `fit-bridge-be/profile-service/**` → profile service.
- `fit-bridge-be/training-service/**` → training service.
- `fit-bridge-libs/**` → общая библиотека и все потребляющие её сервисы.
- `build-plugin/**`, общие Gradle-файлы или несколько сервисов → весь backend.
- API, transport, serialization, security, межсервисный сценарий или внешне наблюдаемое поведение → кандидат на E2E.

## 2. Выполни verification ladder

Запускай только применимые ступени и останавливайся при ошибке:

1. Тест изменённого модуля, например:
   - `./gradlew -p fit-bridge-be/profile-service :biz:allTests --console=plain`
   - `./gradlew -p fit-bridge-be/profile-service :app-ktor:jvmTest --console=plain`
2. Проверка изменённого сервиса:
   - `./gradlew -p fit-bridge-be/profile-service check --console=plain`
   - `./gradlew -p fit-bridge-be/training-service check --console=plain`
3. Для общей библиотеки сначала `./gradlew -p fit-bridge-libs check --console=plain`, затем проверка всех backend-сервисов.
4. Для shared/build/multi-service изменений: `./gradlew -p fit-bridge-be check --console=plain`.
5. Для API/transport/security/cross-service изменений выполни полный локальный E2E workflow одной командой из корня репозитория:
   - Windows/PowerShell: `pwsh -NoProfile -File ./scripts/run-e2e.ps1`;
   - Linux: `bash ./scripts/run-e2e.sh`.

Скрипт является канонической точкой входа для локального E2E. Он строго последовательно:

1. проверяет Docker Compose configuration;
2. собирает `shadowJar` для Profile Service и Training Service;
3. копирует именно fat JAR в `deploy/profile-service.jar` и `deploy/training-service.jar`;
4. поднимает стенд безопасными фазами с ограниченным ожиданием readiness: storage → logging → identity → gateway → application services;
5. после готовности зависимостей точечно пересоздаёт только Keycloak и Envoy, чтобы они загрузили актуальные bind-mounted realm и routing configuration; массовый `--force-recreate` всего стенда запрещён, поскольку контейнеры с `fluentd` logging driver зависят от уже работающего Fluent Bit;
6. проверяет статус контейнеров и публичные readiness endpoints;
7. только после этого запускает `e2eTests --rerun-tasks`, чтобы Gradle действительно выполнил black-box тесты, а не вернул `UP-TO-DATE`.

Не запускай `./gradlew e2eTests` напрямую, если у тебя нет доказательств, что текущий Compose-стенд собран из актуальных артефактов и полностью готов. Скрипт не останавливает стенд и не удаляет volumes. `docker compose down` выполняй отдельно только когда это входит в scope; `down -v` требует явного основания и подтверждения пользователя.

Если имя узкой задачи отсутствует, найди реальную задачу через `./gradlew -p <build> tasks --all` и зафиксируй использованную замену. Не объявляй проверку успешной, если обязательная ступень не запускалась или завершилась ошибкой.

## 3. Отчитайся доказательно

Укажи:

- классификацию изменения;
- все выполненные команды и их результат;
- для E2E — результат `run-e2e.ps1` или `run-e2e.sh`, staging fat JAR, состояние Compose и health checks;
- пропущенные ступени и причину;
- первый значимый фрагмент ошибки, если проверка не прошла.
