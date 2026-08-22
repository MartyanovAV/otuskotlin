plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

val resourceKind = Attribute.of("fitbridge.resource.kind", String::class.java)
val resourcesFromLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes.attribute(resourceKind, "docker-compose")
}

dependencies {
    resourcesFromLib(libs.fit.bridge.dcompose)

    testImplementation(kotlin("test-junit5"))

    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.okhttp)
    testImplementation(libs.ktor.client.websockets)
    testImplementation(libs.kotlinx.serialization.json)

    testImplementation(libs.logback)
    testImplementation(libs.kermit)
    testImplementation(libs.testcontainers.core)
}

val extractLibResources = tasks.register<Sync>("extractLibResources") {
    from(resourcesFromLib.elements.map { files -> files.map(::zipTree) })
    into(layout.buildDirectory.dir("dcompose"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    dependsOn(rootProject.tasks.named("buildImages"))
    dependsOn(extractLibResources)
}
