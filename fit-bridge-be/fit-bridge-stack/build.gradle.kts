/**
 * Gradle-таски для жизненного цикла локального docker-compose стенда.
 *
 * Поднимает полный стек FitBridge (PostgreSQL + Keycloak + training-service
 * + envoy + frontend + observability) одной командой.
 *
 * Использование:
 *   ./gradlew :fit-bridge-be:fit-bridge-stack:stackUpReady   # build + up + wait
 *   ./gradlew :fit-bridge-be:fit-bridge-stack:stackUp        # только docker compose up -d
 *   ./gradlew :fit-bridge-be:fit-bridge-stack:stackDown      # down
 *   ./gradlew :fit-bridge-be:fit-bridge-stack:stackLogs      # tail -f
 *   ./gradlew :fit-bridge-be:fit-bridge-stack:stackStatus    # docker compose ps
 *   ./gradlew :fit-bridge-be:fit-bridge-stack:stackRestart   # down + up
 *   ./gradlew :fit-bridge-be:fit-bridge-stack:stackClean     # down -v (УДАЛЯЕТ VOLUMES)
 *
 * Все таски работают с `deploy/docker-compose.yml` + `docker-compose.local.yml`.
 * Образы:
 *   - fit-bridge-migration-pg-training:local  — собирается через `buildInfra` (Liquibase)
 *   - fitbridge-training-service:local       — собирается через `dockerBuildJvm` (Gradle)
 *   - fitbridge-frontend:local               — собирается через `docker build` в `fit-bridge-fe/`
 */

plugins {
    base
}

// В included build `rootProject` указывает на корень сборки (fit-bridge-be),
// поэтому пути считаем через rootDir/buildDir/workingDir относительно модуля.
val repoRootDir = rootDir.parentFile  // fit-bridge-be/.. -> otuskotlin/
val deployDir = repoRootDir.resolve("deploy")
val frontendDir = repoRootDir.resolve("fit-bridge-fe")
val appVersion = (System.getenv("APP_VERSION") ?: "local").also { logger.lifecycle("APP_VERSION=$it") }

val dockerComposeArgs = listOf(
    "compose",
    "-f", "docker-compose.yml",
    "-f", "docker-compose.local.yml",
)

// --- Сборка образов -----------------------------------------------------------

/** Собирает frontend-образ через `docker build` в `fit-bridge-fe/`. */
val buildFrontendImage = tasks.register<Exec>("buildFrontendImage") {
    group = "stack"
    description = "Собрать fitbridge-frontend:local через docker build"
    workingDir(frontendDir)
    commandLine("docker", "build", "-t", "fitbridge-frontend:local", ".")
}

/** Собирает образ миграций (Liquibase) для БД training-service. */
val buildMigrationImage = tasks.register("buildMigrationImage") {
    group = "stack"
    description = "Собрать fit-bridge-migration-pg-training:local"
    dependsOn("buildInfra")
}

/** Собирает образ training-service. */
val buildTrainingServiceImage = tasks.register("buildTrainingServiceImage") {
    group = "stack"
    description = "Собрать fitbridge-training-service:local"
    dependsOn(":fit-bridge-be:training-service:app-ktor:dockerBuildJvm")
}

/** Полная пересборка всех Docker-образов стенда. */
val stackBuildImages = tasks.register("stackBuildImages") {
    group = "stack"
    description = "Пересобрать все Docker-образы: migrations + training-service + frontend"
    dependsOn(
        "buildMigrationImage",
        "buildTrainingServiceImage",
        "buildFrontendImage",
    )
}

// --- Жизненный цикл стенда --------------------------------------------------

/** Поднять стек в фоне. Не ждёт готовности. */
val stackUp = tasks.register<Exec>("stackUp") {
    group = "stack"
    description = "Поднять локальный docker-compose стек (up -d)"
    workingDir(deployDir)
    environment("APP_VERSION", appVersion)
    commandLine("docker", *dockerComposeArgs.toTypedArray(), "up", "-d")
}

/**
 * Дождаться готовности envoy через чистый `curl` с ретраями —
 * работает одинаково на Windows и Linux без зависимости от bash.
 *
 *   --retry 36              до 36 попыток
 *   --retry-delay 5         5 секунд между попытками
 *   --retry-connrefused     считать ECONNREFUSED поводом для ретрая
 *   --retry-all-errors       ретраить на любую сетевую ошибку
 *   --max-time 5            таймаут одной попытки
 *   --fail                  exit != 0 на HTTP >= 400
 *
 * Итого: до 36×5 = 180 секунд на ожидание. Возвращает exit 0 при 2xx.
 */
val stackWaitForHealth = tasks.register<Exec>("stackWaitForHealth") {
    group = "stack"
    description = "Ждать готовности envoy на http://localhost:8080/health (до ~3 минут)"
    commandLine(
        "curl",
        "--silent",
        "--fail",
        "--max-time", "5",
        "--retry", "36",
        "--retry-delay", "5",
        "--retry-connrefused",
        "--retry-all-errors",
        "--show-error",
        "http://localhost:8080/health",
    )
}

/**
 * Полный сценарий: собрать все образы + поднять + дождаться готовности.
 * Рекомендуется как одна команда для CI и первого запуска.
 */
val stackUpReady = tasks.register("stackUpReady") {
    group = "stack"
    description = "Полный старт стенда: build images + docker compose up + wait for health"
    dependsOn("stackBuildImages", "stackUp", "stackWaitForHealth")
}

/** Остановить стек, сохранив volumes. */
val stackDown = tasks.register<Exec>("stackDown") {
    group = "stack"
    description = "Остановить стек (docker compose down) — volumes сохраняются"
    workingDir(deployDir)
    environment("APP_VERSION", appVersion)
    commandLine("docker", *dockerComposeArgs.toTypedArray(), "down")
}

/** Полностью очистить стек вместе с volumes (БД, Keycloak realm). */
val stackClean = tasks.register<Exec>("stackClean") {
    group = "stack"
    description = "Остановить стек И удалить volumes (БД будет потеряна)"
    workingDir(deployDir)
    environment("APP_VERSION", appVersion)
    commandLine("docker", *dockerComposeArgs.toTypedArray(), "down", "-v")
}

/** Перезапустить стек с пересборкой образов. */
val stackRestart = tasks.register("stackRestart") {
    group = "stack"
    description = "Полный перезапуск: clean + upReady"
    dependsOn("stackClean", "stackUpReady")
}

/** Статус контейнеров. */
val stackStatus = tasks.register<Exec>("stackStatus") {
    group = "stack"
    description = "Показать статус контейнеров стенда (docker compose ps)"
    workingDir(deployDir)
    commandLine("docker", *dockerComposeArgs.toTypedArray(), "ps")
}

/** Логи контейнеров (--follow). */
val stackLogs = tasks.register<Exec>("stackLogs") {
    group = "stack"
    description = "Следить за логами всех контейнеров (Ctrl+C для выхода)"
    workingDir(deployDir)
    commandLine("docker", *dockerComposeArgs.toTypedArray(), "logs", "-f", "--tail=100")
}
