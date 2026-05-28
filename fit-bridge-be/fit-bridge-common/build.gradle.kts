plugins {
    id("build-kmp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
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
