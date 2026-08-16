plugins {
    base
    id("maven-publish")
}

val resourcesZip = tasks.register<Zip>("resourcesZip") {
    description = "Упаковка канонической deploy-конфигурации в Zip-архив"
    archiveClassifier.set("resources")
    archiveExtension.set("zip")
    from(rootProject.file("../deploy"))
}

// Добавляем артефакт в стандартную конфигурацию runtime,
// чтобы includeBuild мог его сопоставить при поиске зависимости
configurations {
    create("runtimeElements") {
        isCanBeResolved = false
        isCanBeConsumed = true
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
