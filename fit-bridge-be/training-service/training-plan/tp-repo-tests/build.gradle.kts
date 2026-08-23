plugins {
    id("build-kmp")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                api(project(":training-plan:tp-repo-common"))
                api(project(":core:core-common"))
                api(project(":training-plan:tp-common"))
                api(project(":core:core-repo-tests"))
                api(libs.coroutines.test)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}
