plugins {
    id("build-kmp")
}
kotlin {
    jvm { }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                api(project(":core:core-repo"))
                api(project(":core:core-common"))

                implementation(libs.db.postgres)
                implementation(libs.db.hikari)
                implementation(libs.db.exposed.core)
                implementation(libs.db.exposed.jdbc)
                implementation(libs.db.exposed.json)
                implementation(libs.db.exposed.java.time)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(libs.testcontainers.core)
                implementation(libs.testcontainers.postgres)
                implementation(project(":core:core-repo-tests"))
                implementation(project(":client-card:cc-common"))
                implementation(project(":client-card:cc-repo-pg"))
                implementation(project(":client-card:cc-repo-tests"))
                implementation(project(":training-plan:tp-common"))
                implementation(project(":training-plan:tp-repo-pg"))
                implementation(project(":training-plan:tp-repo-tests"))
            }
        }
    }
}
