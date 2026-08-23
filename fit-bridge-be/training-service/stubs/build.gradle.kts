plugins {
    id("build-kmp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:core-common"))
                implementation(project(":client-card:cc-common"))
                implementation(project(":training-plan:tp-common"))
            }
        }
    }
}
