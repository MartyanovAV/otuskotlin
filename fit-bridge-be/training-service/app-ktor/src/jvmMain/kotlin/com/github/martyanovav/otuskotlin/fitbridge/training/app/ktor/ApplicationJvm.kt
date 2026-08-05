package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initAppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v1.v1Training
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*

fun main(args: Array<String>): Unit = io.ktor.server.tomcat.jakarta.EngineMain.main(args)

@Suppress("unused") // Referenced in application.yaml
fun Application.moduleJvm(appSettings: AppSettings = initAppSettings()) {
    module(appSettings)
    routing {
        route("v1") {
            install(ContentNegotiation) {
                jackson {
                    setConfig(com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1Mapper.serializationConfig)
                    setConfig(com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1Mapper.deserializationConfig)
                }
            }
            v1Training(appSettings)
        }
    }
}
