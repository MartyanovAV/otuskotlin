package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

private const val LIVENESS_RESPONSE =
    """{"status":"UP","service":"training-service","check":"liveness"}"""
private const val READINESS_RESPONSE =
    """{"status":"UP","service":"training-service","check":"readiness"}"""

fun Route.healthRoutes() {
    get("/health/live") {
        call.respondText(LIVENESS_RESPONSE, ContentType.Application.Json, HttpStatusCode.OK)
    }
    get("/health/ready") {
        call.respondText(READINESS_RESPONSE, ContentType.Application.Json, HttpStatusCode.OK)
    }
}
