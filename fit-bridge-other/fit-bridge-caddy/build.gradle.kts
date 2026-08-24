plugins {
    id("build-docker")
}

group = "com.github.martyanovav.otuskotlin.fitbridge.edge"
version = "0.1.0"

// Caddy is a static binary, not a JVM artefact, so the build context only
// needs the Dockerfile and the two Caddyfile templates. The image tag
// mirrors the training-service convention (`fitbridge-*:local`) so
// Testcontainers, CI and local docker compose share the same reference.
//
// The sources live outside the included build at <repo>/deploy/caddy.
val workspaceRoot = rootProject.projectDir.parent
val caddyBuildContext = project.layout.buildDirectory.dir("docker-caddy").get().toString()

docker {
    images.register("Caddy") {
        buildContext = caddyBuildContext
        imageName = "fitbridge-caddy"
        imageTag = "local"
        dependsOnTask = "copyCaddySources"
    }
}

tasks.register<Copy>("copyCaddySources") {
    description = "Копирование Dockerfile и Caddyfile в build context"
    from(fileTree("$workspaceRoot/deploy/caddy") {
        include("Dockerfile")
        include("Caddyfile.local")
        include("Caddyfile.prod.template")
    })
    into(caddyBuildContext)
}

tasks.register("buildImages") {
    description = "Сборка Docker-образа Caddy edge proxy"
    group = "build"
    dependsOn("dockerBuildCaddy")
}
