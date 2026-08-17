plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("build-jvm") {
            id = "build-jvm"
            implementationClass = "com.github.martyanovav.otuskotlin.plugin.BuildPluginJvm"
        }
        register("build-kmp") {
            id = "build-kmp"
            implementationClass = "com.github.martyanovav.otuskotlin.plugin.BuildPluginMultiplatform"
        }
        register("build-docker") {
            id = "build-docker"
            implementationClass = "com.github.martyanovav.otuskotlin.plugin.DockerPlugin"
        }
        register("build-ktlint") {
            id = "build-ktlint"
            implementationClass = "com.github.martyanovav.otuskotlin.plugin.BuildPluginKtlint"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // enable Ktlint formatting
//    add("detektPlugins", libs.plugin.detektFormatting)
    implementation(libs.plugin.ktlint)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.plugin.kotlin)
//    implementation(libs.plugin.dokka)
    implementation(libs.plugin.binaryCompatibilityValidator)
//    implementation(libs.plugin.mavenPublish)
}
