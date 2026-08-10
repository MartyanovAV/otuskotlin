package com.github.martyanovav.otuskotlin.fitbridge.e2e.scenarios.v2

import com.github.martyanovav.otuskotlin.fitbridge.e2e.FitBridgeE2eClient
import com.github.martyanovav.otuskotlin.fitbridge.e2e.WithFitBridgeStack
import com.github.martyanovav.otuskotlin.fitbridge.e2e.assertSuccess
import com.github.martyanovav.otuskotlin.fitbridge.e2e.assertValidationErrors
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithFitBridgeStack
class ScenarioClientCardV2 {
    private val client by lazy(::FitBridgeE2eClient)

    @BeforeAll
    fun checkStack() = runBlocking {
        client.requireHealthy("/health/training/ready")
    }

    @AfterAll
    fun closeClient() = client.close()

    @Test
    fun `create client card through training container`() = runBlocking {
        assertCardOperation(
            operation = "create",
            payload = """"clientCard":{"displayName":"E2E Client","note":"E2E request"}""",
        )
        Unit
    }

    @Test
    fun `read client card through training container`() = runBlocking {
        assertCardOperation(
            operation = "read",
            payload = """"clientCard":{"id":"00000000-0000-0000-0000-000000000101"}""",
        )
        Unit
    }

    @Test
    fun `update client card through training container`() = runBlocking {
        assertCardOperation(
            operation = "update",
            payload = """"clientCard":{"id":"00000000-0000-0000-0000-000000000101","lock":"stub-lock-client-card","displayName":"E2E Client"}""",
        )
        Unit
    }

    @Test
    fun `archive client card through training container`() = runBlocking {
        val card = assertCardOperation(
            operation = "archive",
            payload = """"clientCard":{"id":"00000000-0000-0000-0000-000000000101","lock":"stub-lock-client-card"}""",
        )
        assertEquals("ARCHIVED", card["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `search client cards through training container`() = runBlocking {
        val requestId = "e2e-client-card-search-v2"
        val response = client.post(
            "/v2/clientCard/search",
            request("search", requestId, """"clientCardFilter":{"pageSize":10,"pageNumber":1}"""),
        )

        val json = response.assertSuccess("clientCard.search", requestId)
        val cards = requireNotNull(json["clientCards"]?.jsonArray) { response.body }
        assertTrue(cards.isNotEmpty(), response.body)
        assertEquals("Анна Смирнова", cards.first().jsonObject["displayName"]?.jsonPrimitive?.content)
    }

    @Test
    fun `create rejects blank display name`() = runBlocking {
        val requestId = "e2e-client-card-create-validation-v2"
        val response = client.post(
            "/v2/clientCard/create",
            validationRequest(
                operation = "create",
                requestId = requestId,
                payload = """"clientCard":{"displayName":"   "}""",
            ),
        )

        response.assertValidationErrors(
            "clientCard.create",
            requestId,
            "validation-displayName-empty",
        )
        Unit
    }

    @Test
    fun `search rejects invalid pagination`() = runBlocking {
        val requestId = "e2e-client-card-search-validation-v2"
        val response = client.post(
            "/v2/clientCard/search",
            validationRequest(
                operation = "search",
                requestId = requestId,
                payload = """"clientCardFilter":{"pageSize":101,"pageNumber":0}""",
            ),
        )

        response.assertValidationErrors(
            "clientCard.search",
            requestId,
            "validation-pageNumber-outOfRange",
            "validation-pageSize-outOfRange",
        )
        Unit
    }

    private suspend fun assertCardOperation(
        operation: String,
        payload: String,
    ) = run {
        val requestId = "e2e-client-card-$operation-v2"
        val response = client.post(
            "/v2/clientCard/$operation",
            request(operation, requestId, payload),
        )
        val json = response.assertSuccess("clientCard.$operation", requestId)
        val card = requireNotNull(json["clientCard"]?.jsonObject) { response.body }
        assertEquals("Анна Смирнова", card["displayName"]?.jsonPrimitive?.content)
        card
    }

    private fun request(operation: String, requestId: String, payload: String) =
        """
            {
              "requestType": "clientCard.$operation",
              "requestId": "$requestId",
              $payload,
              "debug": {"mode": "stub", "stub": "success"}
            }
        """.trimIndent()

    private fun validationRequest(operation: String, requestId: String, payload: String) =
        """
            {
              "requestType": "clientCard.$operation",
              "requestId": "$requestId",
              $payload,
              "debug": {"mode": "test"}
            }
        """.trimIndent()
}
