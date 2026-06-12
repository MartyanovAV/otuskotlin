plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.testcontainers.core)
    
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.okhttp)
    testImplementation(libs.ktor.client.websockets)
    testImplementation("org.apache.kafka:kafka-clients:3.4.0")

    testImplementation(libs.logback)
    testImplementation(libs.kermit)
}

tasks.test {
    useJUnitPlatform()
}
