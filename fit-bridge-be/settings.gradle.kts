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
