plugins {
    id("build-kmp")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                api("com.github.martyanovav.otuskotlin.fitbridge.libs:fit-bridge-lib-cor")
                api(project(":core:core-common"))
                api(project(":core:core-repo"))
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
