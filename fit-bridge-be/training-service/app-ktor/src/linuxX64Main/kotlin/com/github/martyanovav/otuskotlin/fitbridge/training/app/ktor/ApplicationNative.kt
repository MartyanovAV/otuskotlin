package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import io.ktor.server.application.applicationEnvironment
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.embeddedServer
import io.ktor.server.config.yaml.YamlConfigLoader

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
