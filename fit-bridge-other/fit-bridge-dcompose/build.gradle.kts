plugins {
    base
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

val resourcesZip = tasks.register<Zip>("resourcesZip") {
    archiveClassifier.set("resources")
    from("dcompose")
}

publishing {
    repositories {
        maven {
            name = "LocalRepo"
            url = uri("${rootProject.projectDir}/build/repo")
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "ru.otus.otuskotlin.fitbridge"
            artifactId = "dcompose"
            version = "1.0"

            artifact(resourcesZip) {
                classifier = "resources"
                extension = "zip"
            }
        }
    }
}
