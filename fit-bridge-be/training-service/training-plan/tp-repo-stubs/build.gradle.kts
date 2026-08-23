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
                api(project(":training-plan:tp-repo-common"))
                api(project(":core:core-common"))
                api(project(":training-plan:tp-common"))
            }
        }
    }
}
