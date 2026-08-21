plugins {
    id("build-kmp")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm { }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":common"))
                api(project(":repo-common"))

                implementation(libs.coroutines.core)
                implementation(libs.uuid)
                implementation(libs.db.postgres)
                implementation(libs.db.hikari)
                implementation(libs.db.exposed.core)
                implementation(libs.db.exposed.jdbc)
                implementation(libs.db.exposed.json)
                implementation(libs.db.exposed.java.time)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(project(":repo-tests"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(libs.testcontainers.postgres)
                implementation(libs.testcontainers.core)
            }
        }
    }
}
