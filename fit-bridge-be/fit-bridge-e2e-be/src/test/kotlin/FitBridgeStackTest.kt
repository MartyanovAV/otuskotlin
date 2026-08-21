package com.github.martyanovav.otuskotlin.fitbridge.e2e

import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@WithFitBridgeStack
class FitBridgeStackTest {
    @Test
    fun `gateway and training service are healthy`() = runBlocking {
        FitBridgeE2eClient().use { client ->
            client.requireHealthy(
                "/health",
                "/health/training/live",
                "/health/training/ready",
            )
        }
    }

    @Test
    fun `envoy rejects API request without access token`() = runBlocking {
        FitBridgeE2eClient().use { client ->
            val response = client.post(
                path = "/v2/clientCard/search",
                body = """{"requestType":"clientCard.search","clientCardFilter":{"pageSize":10,"pageNumber":1}}""",
                authenticated = false,
            )

            assertEquals(HttpStatusCode.Unauthorized, response.status, response.body)
        }
    }

    @Test
    fun `envoy owns browser cors policy`() = runBlocking {
        FitBridgeE2eClient().use { client ->
            val allowedOrigin = "http://localhost:5173"
            val path = "/v2/clientCard/search"

            val preflight = client.preflight(path, allowedOrigin)
            assertEquals(HttpStatusCode.OK, preflight.status, preflight.body)
            assertEquals(
                allowedOrigin,
                preflight.headers[HttpHeaders.AccessControlAllowOrigin],
                preflight.headers.toString(),
            )

            val actualResponse = client.post(
                path = path,
                body = """{"requestType":"clientCard.search","requestId":"cors-e2e","clientCardFilter":{"pageSize":10,"pageNumber":1}}""",
                origin = allowedOrigin,
            )
            assertEquals(HttpStatusCode.OK, actualResponse.status, actualResponse.body)
            assertEquals(
                allowedOrigin,
                actualResponse.headers[HttpHeaders.AccessControlAllowOrigin],
                actualResponse.headers.toString(),
            )

            val deniedPreflight = client.preflight(path, "https://untrusted.example")
            assertNull(
                deniedPreflight.headers[HttpHeaders.AccessControlAllowOrigin],
                deniedPreflight.headers.toString(),
            )
        }
    }
}
