plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "com.github.martyanovav.otuskotlin"
version = "0.0.1"

subprojects {
    repositories {
        mavenCentral()
    }
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register("e2eTests") {
        dependsOn(project(":fit-bridge-e2e-be").tasks.named("test"))
    }
}
