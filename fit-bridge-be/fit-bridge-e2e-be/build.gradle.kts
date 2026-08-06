plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))

    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.okhttp)
    testImplementation(libs.ktor.client.websockets)
    testImplementation(libs.kotlinx.serialization.json)

    testImplementation(libs.logback)
    testImplementation(libs.kermit)
}

tasks.test {
    useJUnitPlatform()
}
