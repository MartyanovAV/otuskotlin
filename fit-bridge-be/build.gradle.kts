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
    register("buildImages") {
        description = "Сборка Docker-образов backend-сервисов для E2E"
        group = "build"
        dependsOn(
            gradle.includedBuild("training-service").task(":app-ktor:dockerBuildJvm"),
        )
    }
}
