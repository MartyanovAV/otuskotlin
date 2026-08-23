plugins {
    id("build-jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(kotlin("stdlib"))
    implementation(projects.apiV1Jackson)
    implementation(project(":core:core-common"))
    implementation(project(":client-card:cc-common"))
    implementation(project(":training-plan:tp-common"))

    testImplementation(kotlin("test-junit"))
}
