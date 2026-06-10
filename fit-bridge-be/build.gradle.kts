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
}
