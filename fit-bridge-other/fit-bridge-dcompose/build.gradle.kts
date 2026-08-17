plugins {
    base
    id("maven-publish")
}

val resourceKind = Attribute.of("fitbridge.resource.kind", String::class.java)
val deployDirectory = rootProject.layout.projectDirectory.dir("../deploy")
val e2eComposeFile = layout.projectDirectory.file("compose/docker-compose.yml")

val resourcesZip = tasks.register<Zip>("resourcesZip") {
    group = "build"
    description = "Упаковка Docker Compose ресурсов в переиспользуемый ZIP-артефакт"
    archiveClassifier.set("resources")
    archiveExtension.set("zip")
    from(e2eComposeFile) {
        rename { "docker-compose.yml" }
    }
    from(deployDirectory) {
        include("volumes/postgres-init/**")
        include("volumes/envoy/**")
        include("volumes/keycloak/import/**")
    }
}

val requiredResources = listOf(
    "docker-compose.yml",
    "volumes/postgres-init/init.sql",
    "volumes/envoy/envoy.yaml",
    "volumes/keycloak/import/fit-bridge-realm.json",
    "volumes/keycloak/import/fit-bridge-users-0.json",
)

val verifyResourcesZip = tasks.register("verifyResourcesZip") {
    group = "verification"
    description = "Проверка обязательных файлов Docker Compose resource-артефакта"
    dependsOn(resourcesZip)

    val archiveFile = resourcesZip.flatMap { it.archiveFile }
    inputs.file(archiveFile)

    doLast {
        val entries = mutableSetOf<String>()
        zipTree(archiveFile.get().asFile).visit {
            if (!isDirectory) {
                entries += relativePath.pathString
            }
        }

        val missing = requiredResources.filterNot(entries::contains)
        if (missing.isNotEmpty()) {
            throw GradleException("Docker Compose resource archive is missing: ${missing.joinToString()}")
        }
    }
}

// Добавляем артефакт в стандартную конфигурацию runtime,
// чтобы includeBuild мог его сопоставить при поиске зависимости
configurations {
    create("runtimeElements") {
        isCanBeResolved = false
        isCanBeConsumed = true
        attributes.attribute(resourceKind, "docker-compose")
        outgoing.artifact(resourcesZip)
    }
}

// Публикация
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            artifact(resourcesZip) {
                classifier = "resources"
                extension = "zip"
            }
        }
    }
}

tasks.named("check") {
    dependsOn(verifyResourcesZip)
}
