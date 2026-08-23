plugins {
    id("build-kmp")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                api(project(":core:core-repo"))
                api(project(":client-card:cc-repo-common"))
                api(project(":core:core-common"))
                api(project(":client-card:cc-common"))

                implementation(libs.db.cache4k)
                implementation(libs.uuid)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.coroutines.test)
                implementation(project(":client-card:cc-repo-tests"))
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}
