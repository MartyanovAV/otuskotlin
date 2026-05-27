package com.github.martyanovav.otuskotlin.plugin

class DockerImageExtension {
    var imageName: String? = null
    var buildContext = "./"
    var dockerFile = "Dockerfile"
    var imageTag = "latest"
    var dependsOnTask: String? = null
    var buildArgs: Map<String, String> = mapOf()
    var noCache = false
}
