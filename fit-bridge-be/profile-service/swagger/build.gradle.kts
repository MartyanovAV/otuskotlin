plugins {
    id("build-jvm")
    id("build-docker")
}

val dockerDir = project.layout.buildDirectory.dir("docker-swagger").get().toString()

docker {
    images.register("Swagger") {
        buildContext = dockerDir
        dockerFile = "Dockerfile"
        dependsOnTask = "extractLibSpecs"
        imageName = project.name
        imageTag = "${project.version}"
    }
}

val specsFromLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    specsFromLib(project(path = ":profile-specs", configuration = "specsConfiguration"))
}

tasks {
    register<Copy>("extractLibSpecs") {
        description = "Подготовка директории для Dockerfile"
        dependsOn(specsFromLib)
        from(specsFromLib.elements.map { it.map { file -> zipTree(file) } })
        from("Dockerfile", "generate-config.sh")
        into(dockerDir)
    }

    register("buildImages") {
        description = "Сборка докер-образов"
        group = "build"
        dependsOn("dockerBuildSwagger")
    }
}
