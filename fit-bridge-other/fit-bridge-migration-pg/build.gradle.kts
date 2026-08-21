plugins {
    id("build-docker")
}

val trainingBuildContext = project.layout.buildDirectory.dir("docker-training").get().toString()

docker {
    images.register("Training") {
        buildContext = trainingBuildContext
        imageName = "fit-bridge-migration-pg-training"
        imageTag = "${project.version}"
        dependsOnTask = "copyTrainingLiquibase"
    }
}

group = "com.github.martyanovav.otuskotlin.fitbridge.migration"
version = "0.1.0"

tasks.register<Copy>("copyTrainingLiquibase") {
    description = "Копирование Liquibase-файлов миграций training-service в build context"
    from("src/main/liquibase/training")
    from("src/main/docker/Dockerfile")
    into(trainingBuildContext)
}

tasks.register("buildImages") {
    description = "Сборка Docker-образа миграций Training Service"
    group = "build"
    dependsOn("dockerBuildTraining")
}
