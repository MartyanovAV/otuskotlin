plugins {
    id("build-jvm")
    id("maven-publish")
}

val specsZip = tasks.register<Zip>("specsZip") {
    description = "Упаковка спецификаций в Zip-архив"
    archiveBaseName.set("training-specs")
    archiveClassifier.set("spec")
    archiveExtension.set("zip")
    from("specs")
}

val specsConfiguration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(specsConfiguration.name, specsZip)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "training-specs"
            version = project.version.toString()

            artifact(specsZip) {
                classifier = "spec"
                extension = "zip"
            }
        }
    }
}
