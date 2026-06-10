plugins {
    id("build-jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(kotlin("stdlib"))
    implementation(projects.apiV1Jackson)
    implementation(projects.common)

    testImplementation(kotlin("test-junit"))
}
