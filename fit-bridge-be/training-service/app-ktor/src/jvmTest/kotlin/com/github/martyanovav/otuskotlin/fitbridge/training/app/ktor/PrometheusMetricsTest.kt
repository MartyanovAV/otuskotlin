package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrometheusMetricsTest {
    @Test
    fun `metrics route exposes jvm metrics over http`() =
        testApplication {
            application {
                moduleJvm(AppSettings())
            }

            val response = client.get("/metrics")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("jvm_memory_used_bytes"))
        }
}
