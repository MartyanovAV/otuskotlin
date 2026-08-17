group = "com.github.martyanovav.otuskotlin.fitbridge"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register("clean") {
        group = "build"
        gradle.includedBuilds.forEach {
            dependsOn(it.task(":clean"))
        }
    }
    register("buildInfra") { ->
        dependsOn(
            gradle.includedBuild("fit-bridge-other").task(":buildInfra")
        )
    }

    register("buildImages") {
        group = "build"
        description = "Build Docker images required by backend E2E tests"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":buildImages"))
    }

    register("e2eTests") { ->
        dependsOn(
            gradle.includedBuild("fit-bridge-be").task(":fit-bridge-e2e-be:test")
        )
    }

    register("build") {
        group = "build"
        dependsOn("check")
    }

    register("check") {
        group = "verification"
        dependsOn(gradle.includedBuild("fit-bridge-be").task(":check"))
    }
}
