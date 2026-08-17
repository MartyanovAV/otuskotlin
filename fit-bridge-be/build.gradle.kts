group = "com.github.martyanovav.otuskotlin.fitbridge"
version = "0.1.0"

tasks {
    register("build") {
        description = "Сборка всех сервисов"
        group = "build"
        gradle.includedBuilds.forEach {
            dependsOn(it.task(":build"))
        }
    }
    register("clean") {
        description = "Очистка всех сервисов"
        group = "build"
        gradle.includedBuilds.forEach {
            dependsOn(it.task(":clean"))
        }
    }
    register("check") {
        description = "Запуск тестов всех сервисов"
        group = "verification"
        gradle.includedBuilds.forEach {
            dependsOn(it.task(":check"))
        }
    }

    register("ktlintFormat") {
        description = "Форматирование кода ktlint для всех сервисов"
        group = "formatting"
        gradle.includedBuilds.filter { it.name != "build-plugin" }.forEach {
            dependsOn(it.task(":ktlintFormat"))
        }
    }

    register("ktlintCheck") {
        description = "Проверка кода ktlint для всех сервисов"
        group = "verification"
        gradle.includedBuilds.filter { it.name != "build-plugin" }.forEach {
            dependsOn(it.task(":ktlintCheck"))
        }
    }

    register("buildImages") {
        description = "Сборка Docker-образов backend-сервисов для E2E"
        group = "build"
        dependsOn(
            gradle.includedBuild("training-service").task(":app-ktor:dockerBuildJvm"),
        )
    }
}
