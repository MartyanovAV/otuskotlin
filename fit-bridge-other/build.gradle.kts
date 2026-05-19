plugins {
    id("maven-publish")
}

group = "ru.otus.otuskotlin.fitbridge.tests"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}
