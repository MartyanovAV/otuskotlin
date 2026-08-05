package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2Mapper
import kotlinx.serialization.json.Json

data class AppSettings(
    val json: Json = apiV2Mapper,
)
