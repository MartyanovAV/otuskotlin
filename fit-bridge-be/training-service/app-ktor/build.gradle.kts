plugins {
    alias(libs.plugins.kotlinx.serialization)
    id("build-kmp")
    alias(libs.plugins.shadowJar)
    id("build-docker")
}

kotlin {
    jvm {  }
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries {
            executable {
                entryPoint = "com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":common"))
                implementation(project(":api-v2-kmp"))
                implementation(project(":mappers-v2-common"))
                implementation(project(":mappers-v2-client-card"))
                implementation(project(":mappers-v2-training-plan"))

                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.cors)
                implementation(libs.ktor.server.yaml)
                implementation(libs.ktor.server.negotiation)
                implementation(libs.ktor.server.headers.default)
                implementation(libs.ktor.server.headers.caching)
                implementation(libs.ktor.server.headers.response)

                implementation(libs.ktor.serialization.json)

                implementation(libs.coroutines.core)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(project(":api-log1"))
                implementation(project(":api-v1-jackson"))
                implementation(project(":mappers-v1-common"))
                implementation(project(":mappers-v1-client-card"))
                implementation(project(":mappers-v1-training-plan"))
                implementation(libs.ktor.server.tomcat.jakarta)
                implementation(libs.ktor.server.calllogging)
                implementation(libs.ktor.serialization.jackson)
                implementation("com.github.martyanovav.otuskotlin.fitbridge.libs:fit-bridge-lib-logging-common")
                implementation("com.github.martyanovav.otuskotlin.fitbridge.libs:fit-bridge-lib-logging-logback")
                implementation(libs.logback)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.test)
                implementation(libs.coroutines.test)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

docker {
    images.register("Jvm") {
        buildContext = project.layout.buildDirectory.dir("docker-jvm").get().toString()
        dockerFile = "Dockerfile"
        dependsOnTask = "jvmJar"
        imageName = "${rootProject.name}-jvm"
        imageTag = "${project.version}"
    }

    images.register("LinuxX64") {
        buildContext = project.layout.buildDirectory.dir("docker-linuxx64").get().toString()
        dockerFile = "Dockerfile"
        dependsOnTask = "linkReleaseExecutableLinuxX64"
        imageName = "${rootProject.name}-x64"
        imageTag = "${project.version}"
    }
}

afterEvaluate {
    tasks {
        val shadowJar = named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class)
        named("dockerBuildJvm", com.github.martyanovav.otuskotlin.plugin.DockerBuildTask::class) {
            dependsOn(shadowJar)
            group = "docker"
            doFirst {
                copy {
                    from("Dockerfile.jvm") { rename { "Dockerfile" } }
                    from(shadowJar.get().archiveFile.get())
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    into(buildContext)
                }
            }
        }

        val linuxX64ProcessResources = named("linuxX64ProcessResources", org.gradle.language.jvm.tasks.ProcessResources::class)
        named("dockerBuildLinuxX64", com.github.martyanovav.otuskotlin.plugin.DockerBuildTask::class) {
            dependsOn("linkReleaseExecutableLinuxX64")
            dependsOn(linuxX64ProcessResources)
            group = "docker"
            doFirst {
                copy {
                    from("Dockerfile")
                    from(getByName("linkReleaseExecutableLinuxX64").outputs)
                    from(linuxX64ProcessResources.get().outputs)
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    into(buildContext)
                }
            }
        }
    }
}
