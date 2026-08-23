plugins {
    id("build-kmp")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                implementation(project(":stubs"))
                api(project(":core:core-repo"))
                api(project(":client-card:cc-repo-common"))
                api(project(":core:core-common"))
                api(project(":client-card:cc-common"))
            }
        }
    }
}
