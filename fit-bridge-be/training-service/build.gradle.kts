plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "com.github.martyanovav.otuskotlin.fitbridge"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register("build") {
        description = "Сборка всех подпроектов"
        group = "build"
        subprojects.forEach { proj ->
            proj.getTasksByName("build", false).also {
                this@register.dependsOn(it)
            }
        }
    }

    register("clean") {
        description = "Очистка всех подпроектов"
        group = "build"
        subprojects.forEach { proj ->
            proj.getTasksByName("clean", false).also {
                this@register.dependsOn(it)
            }
        }
    }

    register("check") {
        description = "Запуск тестов всех подпроектов"
        group = "verification"
        subprojects.forEach { proj ->
            proj.getTasksByName("check", false).also {
                this@register.dependsOn(it)
            }
        }
    }

    register("ktlintFormat") {
        description = "Форматирование кода ktlint для всех подпроектов"
        group = "formatting"
        subprojects.forEach { proj ->
            proj.getTasksByName("ktlintFormat", false).also {
                this@register.dependsOn(it)
            }
        }
    }

    register("ktlintCheck") {
        description = "Проверка кода ktlint для всех подпроектов"
        group = "verification"
        subprojects.forEach { proj ->
            proj.getTasksByName("ktlintCheck", false).also {
                this@register.dependsOn(it)
            }
        }
    }
}
