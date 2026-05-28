plugins {
    id("build-kmp")
}

group = rootProject.group
version = rootProject.version

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.fitBridgeApiV2Kmp)
                implementation(projects.fitBridgeCommon)
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
