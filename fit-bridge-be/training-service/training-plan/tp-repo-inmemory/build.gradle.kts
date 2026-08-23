plugins {
    id("build-kmp")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                api(project(":core:core-repo"))
                api(project(":training-plan:tp-repo-common"))
                api(project(":core:core-common"))
                api(project(":training-plan:tp-common"))

                implementation(libs.db.cache4k)
                implementation(libs.uuid)
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
            }
        }
    }
}
