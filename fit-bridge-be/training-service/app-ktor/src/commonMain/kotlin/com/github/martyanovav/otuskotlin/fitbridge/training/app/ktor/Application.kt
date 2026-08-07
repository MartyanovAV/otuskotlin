package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initAppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initPlugins
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v2.v2Training
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v2.wsHandlerV2
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun Application.module(appSettings: AppSettings = initAppSettings()) {
    initPlugins(appSettings)
    install(WebSockets) {
        pingPeriodMillis = 15_000
        timeoutMillis = 15_000
        maxFrameSize = 64 * 1024L
        masking = false
    }
    routing {
        healthRoutes()
        route("v2") {
            install(ContentNegotiation) {
                json(appSettings.json)
            }
            v2Training(appSettings)
            webSocket("ws") {
                wsHandlerV2(appSettings)
            }
        }
    }
}
