package com.github.martyanovav.otuskotlin.fitbridge.e2e.be.docker

import com.github.martyanovav.otuskotlin.fitbridge.e2e.be.base.AbstractDockerCompose

object KtorDockerCompose : AbstractDockerCompose(
    "app-ktor_1", 8080, "docker-compose-ktor.yml"
)
