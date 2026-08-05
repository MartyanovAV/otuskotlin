package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initAppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initPlugins
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v2.v2Training
import io.ktor.server.application.*
import io.ktor.server.routing.*

import io.ktor.serialization.kotlinx.json.*

fun Application.module(appSettings: AppSettings = initAppSettings()) {
    initPlugins(appSettings)
    routing {
        route("v2") {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(appSettings.json)
            }
            v2Training(appSettings)
        }
    }
}
