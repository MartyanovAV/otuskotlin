plugins {
    id("build-kmp")
    alias(libs.plugins.kotlinx.serialization)
}
kotlin {
    jvm { }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                api(project(":core:core-repo"))
                api(project(":core:core-repo-pg"))
                api(project(":training-plan:tp-repo-common"))
                api(project(":core:core-common"))
                api(project(":training-plan:tp-common"))

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
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.coroutines.test)
                implementation(project(":training-plan:tp-repo-tests"))
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(libs.testcontainers.postgres)
                implementation(libs.testcontainers.core)
            }
        }
    }
}
