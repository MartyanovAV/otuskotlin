rootProject.name = "profile-service"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    includeBuild("../../build-plugin")
    plugins {
        id("build-jvm") apply false
        id("build-kmp") apply false
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":common")
include(":profile-specs")
project(":profile-specs").projectDir = file("specs")
include(":api-v1-jackson")
include(":api-v2-kmp")
include(":mappers-v1-common")
include(":mappers-v1-trainer-profile")
include(":mappers-v2-common")
include(":mappers-v2-trainer-profile")
include(":profile-swagger")
project(":profile-swagger").projectDir = file("swagger")
