group = "com.github.martyanovav.otuskotlin.fitbridge"
version = "0.1.0"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

tasks {
    register("buildInfra") {
        group = "build"
        dependsOn(project(":fit-bridge-dcompose").getTasksByName("publish",false))
    }

    register("clean" ) {
        description = "Очистка всех подпроектов"
        group = "build"
        subprojects.forEach { proj ->
            println("PROJ $proj")
            proj.getTasksByName("clean", false).also {
                this@register.dependsOn(it)
            }
        }
    }
    register("check" ) {
        description = "Запуск тестов всех подпроектов"
        group = "verification"
        subprojects.forEach { proj ->
            println("PROJ $proj")
            proj.getTasksByName("check", false).also {
                this@register.dependsOn(it)
            }
        }
    }

}
