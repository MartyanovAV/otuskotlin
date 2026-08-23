plugins {
    id("build-kmp")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                implementation(project(":core:core-common"))
                implementation(project(":core:core-biz"))
                implementation(project(":training-plan:tp-common"))
                implementation(project(":core:core-repo"))
                implementation(project(":training-plan:tp-repo-common"))
                implementation(project(":stubs"))
                implementation(libs.cor)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.coroutines.test)
                implementation(project(":training-plan:tp-repo-tests"))
                implementation(project(":training-plan:tp-repo-stubs"))
                implementation(project(":training-plan:tp-repo-inmemory"))
                implementation(project(":client-card:cc-repo-inmemory"))
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}
