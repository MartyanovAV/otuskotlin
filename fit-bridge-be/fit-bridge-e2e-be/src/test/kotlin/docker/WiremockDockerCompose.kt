package com.github.martyanovav.otuskotlin.fitbridge.e2e.be.docker

import com.github.martyanovav.otuskotlin.fitbridge.e2e.be.base.AbstractDockerCompose

object WiremockDockerCompose : AbstractDockerCompose(
    "app-wiremock", 8080, "docker-compose-wiremock.yml"
)
