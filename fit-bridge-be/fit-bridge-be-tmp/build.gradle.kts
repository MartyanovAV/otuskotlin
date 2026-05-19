plugins {
    id("build-jvm")
}

repositories {
    maven {
        name = "LocalRepo"
        url = uri("${rootProject.projectDir}/../fit-bridge-other/build/repo")
    }
}

val resourcesFromLib by configurations.creating

dependencies {
    implementation(libs.kotlinx.datetime)
    resourcesFromLib("ru.otus.otuskotlin.fitbridge:dcompose:1.0:resources@zip")
}

tasks.register<Copy>("extractLibResources") {
    from(zipTree(resourcesFromLib.singleFile))
    into(layout.buildDirectory.dir("dcompose"))
}

tasks["build"].dependsOn("extractLibResources")
