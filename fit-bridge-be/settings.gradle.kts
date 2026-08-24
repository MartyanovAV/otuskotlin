rootProject.name = "fit-bridge-be"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    includeBuild("../build-plugin")
    plugins {
        id("build-jvm") apply false
        id("build-kmp") apply false
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("training-service")
include(":fit-bridge-e2e-be")
include(":fit-bridge-e2e-fe")
include(":fit-bridge-stack")
