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

// Включает вот такую конструкцию
// implementation(projects.m2l5Gradle.sub1.ssub1)
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

//include(":fit-bridge-be-tmp")
include(":fit-bridge-api-v1-jackson")
include(":fit-bridge-api-v2-kmp")
include(":fit-bridge-common")
include(":fit-bridge-mappers-v1-common")
include(":fit-bridge-mappers-v1-client-card")
include(":fit-bridge-mappers-v1-trainer-profile")
include(":fit-bridge-mappers-v1-training-plan")
include(":fit-bridge-mappers-v2-common")
include(":fit-bridge-mappers-v2-client-card")
include(":fit-bridge-mappers-v2-trainer-profile")
include(":fit-bridge-mappers-v2-training-plan")
