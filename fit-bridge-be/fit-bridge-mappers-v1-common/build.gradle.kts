plugins {
    id("build-jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(kotlin("stdlib"))
    implementation(projects.fitBridgeApiV1Jackson)
    implementation(projects.fitBridgeCommon)

    testImplementation(kotlin("test-junit"))
}
