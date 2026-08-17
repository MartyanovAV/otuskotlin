package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

import io.ktor.server.config.yaml.*

fun main() {
    val conf = YamlConfigLoader().load("./application.yaml") ?: throw RuntimeException("Cannot read application.yaml")
    println(conf)

    val appEnv = applicationEnvironment {
        config = conf
    }

    embeddedServer(
        factory = CIO, environment = appEnv, configure = {
            this.connectors.add(EngineConnectorBuilder().apply {
                host = conf.host
                port = conf.port
            })
        }) {
        module()
    }.start(true)
}
