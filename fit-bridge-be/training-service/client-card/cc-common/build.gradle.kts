plugins {
    id("build-kmp")
    alias(libs.plugins.kotlinx.serialization)
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.datetime)
                api("com.github.martyanovav.otuskotlin.fitbridge.libs:fit-bridge-lib-logging-common")
                implementation(libs.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                api(project(":core:core-common"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
