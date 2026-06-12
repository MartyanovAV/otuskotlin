package com.github.martyanovav.otuskotlin.fitbridge.e2e.be.docker

import com.github.martyanovav.otuskotlin.fitbridge.e2e.be.base.AbstractDockerCompose

object KafkaDockerCompose : AbstractDockerCompose(
    "kafka_1", 9091, "docker-compose-kafka.yml"
)
