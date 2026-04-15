rootProject.name = "otuskotlin"

pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
    }
}

includeBuild("lessons")
includeBuild("fitness-crm-be")
