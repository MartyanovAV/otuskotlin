plugins {
    id("build-kmp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common"))
                implementation(project(":stubs"))
                implementation(libs.cor)

                // DB
                implementation(project(":repo-common"))
                implementation(project(":repo-inmemory"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)

                // DB
                implementation(project(":repo-tests"))
                implementation(project(":repo-stubs"))
                implementation(project(":repo-inmemory"))
            }
        }
    }
}
