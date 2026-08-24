/**
 * Gradle-обвязка для Playwright E2E тестов фронтенда.
 *
 * Модуль не содержит JVM-кода — только shell-таски вокруг `npx playwright`.
 * Требует установленный Node.js 20+ (для CI это часть workflow).
 *
 * Полный сценарий:
 *   ./gradlew :fit-bridge-e2e-fe:e2eFe
 *
 * Прокидывает в Node:
 *   BASE_URL, KEYCLOAK_URL, TEST_USERNAME, TEST_PASSWORD, CI, HEADED
 * — из текущего окружения / из Gradle properties (prefix `e2eFe.`).
 *
 * По умолчанию:
 *   BASE_URL=http://localhost:8080 (envoy из deploy/docker-compose.local.yml)
 *   TEST_USERNAME=fitbridge-test
 *   TEST_PASSWORD=fitbridge
 */

plugins {
    base
}

val e2eFeDir = project.layout.projectDirectory.asFile

// Поиск npm/node: сначала PATH, иначе типичные места Windows.
// Gradle-демон может не иметь Node в своём окружении, поэтому
// резолвим абсолютные пути и прокидываем их в PATH дочерним процессам.
fun resolveExecutable(name: String, fallbackPath: String): String {
    val path = System.getenv("PATH") ?: ""
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val separator = if (isWindows) ";" else ":"
    // На Windows npm/npx/node — это .cmd-обёртки; .exe — сам бинарь Node
    val extensions = if (isWindows)
        listOf(".cmd", ".bat", "", ".exe", ".ps1") else listOf("")

    for (dir in path.split(separator)) {
        if (dir.isBlank()) continue
        for (ext in extensions) {
            val candidate = java.io.File(dir, name + ext)
            if (candidate.exists() && candidate.canExecute()) return candidate.absolutePath
        }
    }
    return java.io.File(fallbackPath, name).absolutePath
}

val nodeExecutable = resolveExecutable(
    "node",
    System.getenv("ProgramFiles")?.let { "$it/nodejs" } ?: "C:/Program Files/nodejs"
)
val npmExecutable = resolveExecutable(
    "npm",
    System.getenv("ProgramFiles")?.let { "$it/nodejs" } ?: "C:/Program Files/nodejs"
)
val npxExecutable = resolveExecutable(
    "npx",
    System.getenv("ProgramFiles")?.let { "$it/nodejs" } ?: "C:/Program Files/nodejs"
)

// PATH, в котором дочерние процессы найдут npm
val nodeDir = java.io.File(nodeExecutable).parent
val childPath = "$nodeDir${if (System.getProperty("os.name").lowercase().contains("win")) ";" else ":"}" +
    (System.getenv("PATH") ?: "")

// Разрешение значений: env → Gradle property → default
fun resolveProp(name: String, default: String): String =
    System.getenv(name)
        ?: (project.findProperty("e2eFe.${name.removePrefix("E2EFE_")}") as? String)
        ?: default

val baseUrl = resolveProp("BASE_URL", "http://localhost:8080")
val keycloakUrl = resolveProp("KEYCLOAK_URL", baseUrl)
val testUsername = resolveProp("TEST_USERNAME", "fitbridge-test")
val testPassword = resolveProp("TEST_PASSWORD", "fitbridge")
val isCI = System.getenv("CI") == "1"
val headless = System.getenv("HEADED") != "1"

val e2eFeInstallDeps = tasks.register<Exec>("e2eFeInstallDeps") {
    group = "verification"
    description = "Установить npm-зависимости Playwright"
    workingDir(e2eFeDir)
    environment("PATH", childPath)
    commandLine(npmExecutable, "ci")
}

val e2eFeInstallBrowsers = tasks.register<Exec>("e2eFeInstallBrowsers") {
    group = "verification"
    description = "Скачать Chromium для Playwright"
    dependsOn(e2eFeInstallDeps)
    workingDir(e2eFeDir)
    environment("PATH", childPath)
    // --with-deps требует sudo на Linux; в CI крутится от root, на Windows просто скачает
    commandLine(npxExecutable, "playwright", "install", "chromium")
}

val e2eFeTest = tasks.register<Exec>("e2eFeTest") {
    group = "verification"
    description = "Запустить Playwright E2E тесты FitBridge SPA"
    dependsOn(e2eFeInstallDeps, e2eFeInstallBrowsers)

    workingDir(e2eFeDir)
    environment("PATH", childPath)

    environment("BASE_URL", baseUrl)
    environment("KEYCLOAK_URL", keycloakUrl)
    environment("TEST_USERNAME", testUsername)
    environment("TEST_PASSWORD", testPassword)
    environment("CI", if (isCI) "1" else "0")
    environment("HEADED", if (headless) "1" else "0")

    // Полезные прокидки для Playwright
    val pwTimeout = System.getenv("PW_TIMEOUT") ?: "60000"
    environment("PW_TIMEOUT", pwTimeout)

    commandLine(npxExecutable, "playwright", "test")
}

val e2eFeReport = tasks.register<Exec>("e2eFeReport") {
    group = "verification"
    description = "Открыть HTML-отчёт Playwright (только локально)"
    workingDir(e2eFeDir)
    environment("PATH", childPath)
    commandLine(npxExecutable, "playwright", "show-report")
}

tasks.register("e2eFe") {
    group = "verification"
    description = "Полный прогон Playwright E2E (deps + browsers + tests)"
    dependsOn(e2eFeTest)
}
