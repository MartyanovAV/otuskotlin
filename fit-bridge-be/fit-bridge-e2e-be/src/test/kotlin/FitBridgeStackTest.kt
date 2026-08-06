package com.github.martyanovav.otuskotlin.fitbridge.e2e

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
}
