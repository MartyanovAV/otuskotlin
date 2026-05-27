rootProject.name = "otuskotlin"

pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
    }
}

//includeBuild("lessons")
includeBuild("fit-bridge-other")
includeBuild("fit-bridge-be")
