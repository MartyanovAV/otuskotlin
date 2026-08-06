package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2Mapper
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionRepo
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.TrainingProcessor
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import kotlinx.serialization.json.Json

data class AppSettings(
    val json: Json = apiV2Mapper,
    val corSettings: CorSettings =
        CorSettings(
            wsSessionsV1 = KtorWsSessionRepo(),
            wsSessionsV2 = KtorWsSessionRepo(),
        ),
    val processor: TrainingProcessor = TrainingProcessor(corSettings),
) {
    val wsSessionsV1 get() = corSettings.wsSessionsV1
    val wsSessionsV2 get() = corSettings.wsSessionsV2
}
