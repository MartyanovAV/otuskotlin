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
                implementation(project(":client-card:cc-common"))
                implementation(project(":core:core-repo"))
                implementation(project(":client-card:cc-repo-common"))
                implementation(project(":stubs"))
                implementation(libs.cor)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.coroutines.test)
                implementation(project(":client-card:cc-repo-tests"))
                implementation(project(":client-card:cc-repo-stubs"))
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
