rootProject.name = "training-service"

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

includeBuild("../../fit-bridge-libs")

include(":common")
include(":stubs")
include(":biz")
include(":training-specs")
project(":training-specs").projectDir = file("specs")
include(":api-v1-jackson")
include(":api-log1")
include(":api-v2-kmp")
include(":mappers-v1-common")
include(":mappers-v1-client-card")
include(":mappers-v1-training-plan")
include(":mappers-v2-common")
include(":mappers-v2-client-card")
include(":mappers-v2-training-plan")
include(":app-ktor")
include(":training-swagger")
project(":training-swagger").projectDir = file("swagger")

// DB
include(":repo-common")
include(":repo-inmemory")
include(":repo-stubs")
include(":repo-tests")
include(":repo-pg")
