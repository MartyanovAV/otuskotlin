package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthRoutesTest {
    @Test
    fun `liveness endpoint reports service is up`() =
        testApplication {
            application { module() }

            assertEquals(HttpStatusCode.OK, client.get("/health/live").status)
        }

    @Test
    fun `readiness endpoint reports service is ready when check passes`() =
        testApplication {
            application { module() }

            assertEquals(HttpStatusCode.OK, client.get("/health/ready").status)
        }

    @Test
    fun `readiness endpoint reports 503 when dependency check fails`() =
        testApplication {
            application {
                module(AppSettings(readyCheck = { false }))
            }

            val response = client.get("/health/ready")
            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }
}
