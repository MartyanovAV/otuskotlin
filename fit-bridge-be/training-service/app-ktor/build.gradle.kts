plugins {
    alias(libs.plugins.kotlinx.serialization)
    id("build-kmp")
    alias(libs.plugins.shadowJar)
    id("build-docker")
}

kotlin {
    jvm { }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":common"))
                implementation(project(":biz"))
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
                implementation(libs.ktor.server.websocket)

                implementation(libs.ktor.serialization.json)

                implementation(libs.coroutines.core)

                // DB
                implementation(project(":repo-stubs"))
                implementation(project(":repo-inmemory"))
                implementation(project(":repo-pg"))
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
                implementation(libs.ktor.server.metrics.micrometer)
                implementation(libs.micrometer.registry.prometheus)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.test)
                implementation(libs.coroutines.test)
                implementation(libs.ktor.client.websockets)

                // DB
                implementation(project(":repo-common"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                runtimeOnly(libs.prometheus.metrics.exposition.formats)
            }
        }
    }
}

docker {
    images.register("Jvm") {
        buildContext =
            project.layout.buildDirectory
                .dir("docker-jvm")
                .get()
                .toString()
        dockerFile = "Dockerfile"
        dependsOnTask = "jvmJar"
        imageName = "fitbridge-training-service"
        imageTag = "local"
    }
}

afterEvaluate {
    tasks {
        val shadowJar = named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class)
        shadowJar.configure {
            manifest {
                attributes["Main-Class"] =
                    "com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.ApplicationJvmKt"
            }
        }
        named("dockerBuildJvm", com.github.martyanovav.otuskotlin.plugin.DockerBuildTask::class) {
            dependsOn(shadowJar)
            group = "docker"
            doFirst {
                sync {
                    from("Dockerfile.jvm") { rename { "Dockerfile" } }
                    from(shadowJar.get().archiveFile.get()) { rename { "app.jar" } }
                    into(buildContext)
                }
            }
        }
    }
}
