plugins {
    kotlin("jvm") apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
}

group = "com.github.martyanovav"
version = "0.0.1"

subprojects {
    repositories {
        mavenCentral()
    }

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
