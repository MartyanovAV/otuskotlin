group = "com.github.martyanovav.otuskotlin.fitbridge"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register("clean") {
        group = "build"
        gradle.includedBuilds.forEach {
            dependsOn(it.task(":clean"))
        }
    }
    register("buildInfra") { ->
        dependsOn(
            gradle.includedBuild("fit-bridge-other").task(":buildInfra")
        )
    }

    register("buildImages") {
        group = "build"
        description = "Build Docker images required by backend E2E tests"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":buildImages"))
    }

    register("e2eTests") { ->
        dependsOn(
            gradle.includedBuild("fit-bridge-be").task(":fit-bridge-e2e-be:test")
        )
    }

    register("build") {
        group = "build"
        dependsOn("check")
    }

    register("check") {
        group = "verification"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":check"))
    }
    
    register("ktlintFormat") {
        group = "formatting"
        gradle.includedBuilds.filter { it.name != "fit-bridge-other" }.forEach {
            dependsOn(it.task(":ktlintFormat"))
        }
    }

    register("ktlintCheck") {
        group = "verification"
        gradle.includedBuilds.filter { it.name != "fit-bridge-other" }.forEach {
            dependsOn(it.task(":ktlintCheck"))
        }
    }

    // === Локальный стенд + UI E2E ==================================
    register("stackUpReady") {
        description = "Поднять локальный FitBridge-стек + дождаться готовности envoy"
        group = "stack"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":fit-bridge-stack:stackUpReady"))
    }

    register("stackBuildImages") {
        description = "Пересобрать все Docker-образы стенда (migrations + training + frontend)"
        group = "stack"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":fit-bridge-stack:stackBuildImages"))
    }

    register("stackDown") {
        description = "Остановить локальный стек (volumes сохраняются)"
        group = "stack"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":fit-bridge-stack:stackDown"))
    }

    register("stackClean") {
        description = "Остановить стек И удалить volumes (БД теряется)"
        group = "stack"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":fit-bridge-stack:stackClean"))
    }

    register("stackStatus") {
        description = "Показать статус контейнеров локального стека"
        group = "stack"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":fit-bridge-stack:stackStatus"))
    }

    /**
     * Полный цикл UI E2E одной командой:
     *   поднять стек → дождаться готовности → прогнать Playwright.
     * Полезно локально и в CI как gate «поднять-и-протестить».
     */
    register("e2eFeAll") {
        description = "Поднять стек + прогнать Playwright UI E2E"
        group = "verification"
        dependsOn(
            gradle.includedBuild("fit-bridge-be").task(":fit-bridge-stack:stackUpReady"),
            gradle.includedBuild("fit-bridge-be").task(":fit-bridge-e2e-fe:e2eFe"),
        )
    }
}
