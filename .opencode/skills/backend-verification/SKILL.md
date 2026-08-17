---
name: backend-verification
description: Select and run the smallest reliable Gradle verification ladder for FitBridge backend changes, escalating from an affected module to a service, shared builds, all backend services, and E2E only when the change risk requires it
---

# Backend Verification

Проверяй backend от узкого и быстрого уровня к широкому. Не запускай `clean` по
умолчанию: используй его только при подтверждённой проблеме с устаревшими
артефактами или кэшем.

## 1. Определи область изменения

- `fit-bridge-be/training-service/**` → training service.
- `fit-bridge-libs/**` → общая библиотека и все потребляющие её сервисы.
- `build-plugin/**`, общие Gradle-файлы или несколько сервисов → весь backend.
- API, transport, serialization, security, межсервисный сценарий или внешне
  наблюдаемое поведение → кандидат на E2E.

## 2. Выполни verification ladder

Запускай только применимые ступени и останавливайся при ошибке:

1. Тест изменённого модуля, например:
   - `./gradlew -p fit-bridge-be/training-service :biz:allTests --console=plain`
   - `./gradlew -p fit-bridge-be/training-service :app-ktor:jvmTest --console=plain`
2. Проверка изменённого сервиса:
   - `./gradlew -p fit-bridge-be/training-service check --console=plain`
3. Для общей библиотеки сначала
   `./gradlew -p fit-bridge-libs check --console=plain`, затем проверка всех
   backend-сервисов.
4. Для shared/build/multi-service изменений:
   `./gradlew -p fit-bridge-be check --console=plain`.
5. Для API/transport/security/cross-service изменений выполни из корня три
   команды строго последовательно.

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

`buildInfra` проверяет переиспользуемый Compose resource ZIP. `buildImages`
создаёт образ Training Service из актуального fat JAR. `e2eTests` распаковывает
ресурсы, поднимает минимальный стек через Testcontainers на динамическом порту,
ждёт readiness и после тестов удаляет контейнеры и тестовые volumes. Не подменяй
первые два шага ручной подготовкой `deploy/`: так легко проверить stale image.

Если имя узкой задачи отсутствует, найди реальную задачу через
`./gradlew -p <build> tasks --all` и зафиксируй использованную замену. Не объявляй
проверку успешной, если обязательная ступень не запускалась или завершилась
ошибкой.

## 3. Отчитайся доказуемо

Укажи:

- классификацию изменения;
- все выполненные команды и их результат;
- для E2E — результаты `buildInfra`, `buildImages`, `e2eTests`, readiness и
  Testcontainers lifecycle;
- пропущенные ступени и причину;
- первый значимый фрагмент ошибки, если проверка не прошла.
