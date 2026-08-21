package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders

fun Application.initPlugins() {
    install(DefaultHeaders)
}
