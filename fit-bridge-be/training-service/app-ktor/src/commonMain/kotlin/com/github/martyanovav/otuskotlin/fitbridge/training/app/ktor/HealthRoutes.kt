package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

private const val LIVENESS_RESPONSE =
    """{"status":"UP","service":"training-service","check":"liveness"}"""
private const val READINESS_UP_RESPONSE =
    """{"status":"UP","service":"training-service","check":"readiness"}"""
private const val READINESS_DOWN_RESPONSE =
    """{"status":"DOWN","service":"training-service","check":"readiness"}"""

fun Route.healthRoutes(appSettings: AppSettings? = null) {
    get("/health/live") {
        call.respondText(LIVENESS_RESPONSE, ContentType.Application.Json, HttpStatusCode.OK)
    }
    get("/health/ready") {
        val isReady = runCatching {
            appSettings?.readyCheck?.invoke() ?: true
        }.getOrDefault(false)

        if (isReady) {
            call.respondText(READINESS_UP_RESPONSE, ContentType.Application.Json, HttpStatusCode.OK)
        } else {
            call.respondText(READINESS_DOWN_RESPONSE, ContentType.Application.Json, HttpStatusCode.ServiceUnavailable)
        }
    }
}
