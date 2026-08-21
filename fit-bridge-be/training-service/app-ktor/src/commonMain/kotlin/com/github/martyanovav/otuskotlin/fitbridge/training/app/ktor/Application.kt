package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.AUTH_HEADER
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.jwt2principal
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initAppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initPlugins
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v2.v2Training
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v2.wsHandlerV2
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.header
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket

fun Application.module(appSettings: AppSettings = initAppSettings()) {
    initPlugins()
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
                wsHandlerV2(appSettings, call.request.header(AUTH_HEADER).jwt2principal())
            }
        }
    }
}
