# FitBridge Stack — Gradle-таски для локального docker-compose стенда

Этот модуль оборачивает `deploy/docker-compose.yml` + `docker-compose.local.yml`
в кросс-платформенные Gradle-таски. Никакого JVM-кода — только `Exec`-таски.

## Команды

```bash
# Из корня репозитория
./gradlew :fit-bridge-be:fit-bridge-stack:stackUpReady   # build + up + wait
./gradlew :fit-bridge-be:fit-bridge-stack:stackUp        # только docker compose up -d
./gradlew :fit-bridge-be:fit-bridge-stack:stackWaitForHealth
./gradlew :fit-bridge-be:fit-bridge-stack:stackStatus    # docker compose ps
./gradlew :fit-bridge-be:fit-bridge-stack:stackDown      # down (volumes сохраняются)
./gradlew :fit-bridge-be:fit-bridge-stack:stackClean     # down -v (УДАЛЯЕТ VOLUMES)
./gradlew :fit-bridge-be:fit-bridge-stack:stackRestart   # clean + upReady
./gradlew :fit-bridge-be:fit-bridge-stack:stackLogs      # docker compose logs -f
```

## Зависимости задач

```
stackUpReady
├── stackBuildImages
│   ├── buildMigrationImage
│   │   └── buildInfra                    # уже есть в fit-bridge-be
│   ├── buildTrainingServiceImage
│   │   └── :fit-bridge-be:training-service:app-ktor:dockerBuildJvm
│   └── buildFrontendImage                 # docker build в fit-bridge-fe/
├── stackUp                                # docker compose up -d
└── stackWaitForHealth                     # curl --retry до 3 минут
```

## Состав стенда

`docker compose -f docker-compose.yml -f docker-compose.local.yml`:

- **postgresql** — БД `training_db`
- **keycloak** — realm `fit-bridge`, тестовый пользователь `fitbridge-test` / `fitbridge`
- **liquibase-training** — миграции (fit-bridge-migration-pg-training:local)
- **training-service** — Ktor (fitbridge-training-service:local)
- **frontend** — Vue 3 SPA (fitbridge-frontend:local)
- **envoy** — единая точка входа на `:8080`
- **fluent-bit + greptimedb** — логи и метрики

## Полный первый запуск

```bash
# Из корня
./gradlew :fit-bridge-be:fit-bridge-stack:stackUpReady

# В другом терминале — логи
./gradlew :fit-bridge-be:fit-bridge-stack:stackLogs

# SPA доступна на http://localhost:8080
# Keycloak Admin — http://localhost:8080/admin
```

## Сценарии

### «Только поднять, образы уже есть»
```bash
./gradlew :fit-bridge-be:fit-bridge-stack:stackUp
./gradlew :fit-bridge-be:fit-bridge-stack:stackWaitForHealth
```

### «Полная пересборка после изменений в коде»
```bash
./gradlew :fit-bridge-be:fit-bridge-stack:stackRestart
# = clean + stackBuildImages + stackUp + stackWaitForHealth
```

### «Запустить UI E2E»
```bash
./gradlew :fit-bridge-be:fit-bridge-stack:stackUpReady
./gradlew :fit-bridge-be:fit-bridge-e2e-fe:e2eFe
```

### «Сбросить БД и начать заново»
```bash
./gradlew :fit-bridge-be:fit-bridge-stack:stackClean   # down -v
./gradlew :fit-bridge-be:fit-bridge-stack:stackUpReady
```

## Кросс-платформенность

Все таски используют `docker compose` (есть на Windows и Linux) и `curl` с
ретраями (`--retry 36 --retry-delay 5`) — никаких `bash -c` или `cmd.exe`.
Пути считаются через `rootDir.parentFile` (корень репо), чтобы работало
из included build.

## Переменные окружения

| Имя | По умолчанию | Назначение |
|-----|--------------|------------|
| `APP_VERSION` | `local` | Тег образов (`fitbridge-training-service:${APP_VERSION}` и т.п.) |
| `DOCKER_HOST` | (default) | Если Docker слушает не на стандартном сокете |
