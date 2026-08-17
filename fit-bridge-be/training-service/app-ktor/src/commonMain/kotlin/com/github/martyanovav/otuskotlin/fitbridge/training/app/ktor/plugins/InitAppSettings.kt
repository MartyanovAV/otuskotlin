package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import io.ktor.server.application.Application

fun Application.initAppSettings(): AppSettings = AppSettings()
