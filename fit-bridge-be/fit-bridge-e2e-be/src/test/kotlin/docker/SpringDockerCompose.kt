package com.github.martyanovav.otuskotlin.fitbridge.e2e.be.docker

import com.github.martyanovav.otuskotlin.fitbridge.e2e.be.base.AbstractDockerCompose

object SpringDockerCompose : AbstractDockerCompose(
    "app-spring_1", 8080, "docker-compose-spring.yml"
)
