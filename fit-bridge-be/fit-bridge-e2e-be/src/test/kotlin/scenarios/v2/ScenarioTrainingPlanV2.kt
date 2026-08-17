package com.github.martyanovav.otuskotlin.fitbridge.e2e.scenarios.v2

import com.github.martyanovav.otuskotlin.fitbridge.e2e.FitBridgeE2eClient
import com.github.martyanovav.otuskotlin.fitbridge.e2e.WithFitBridgeStack
import com.github.martyanovav.otuskotlin.fitbridge.e2e.assertSuccess
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
class ScenarioTrainingPlanV2 {
    private val client by lazy(::FitBridgeE2eClient)

    @BeforeAll
    fun checkStack() = runBlocking {
        client.requireHealthy("/health/training/ready")
    }

    @AfterAll
    fun closeClient() = client.close()

    @Test
    fun `create training plan through training container`() = runBlocking {
        assertPlanOperation(
            operation = "create",
            payload = """"trainingPlan":{"title":"E2E plan","clientCardId":"00000000-0000-0000-0000-000000000101","planItems":[{"itemType":"EXERCISE","id":"00000000-0000-0000-0000-000000000301","title":"Squats"}]}""",
        )
        Unit
    }

    @Test
    fun `read training plan through training container`() = runBlocking {
        assertPlanOperation(
            operation = "read",
            payload = """"trainingPlan":{"id":"00000000-0000-0000-0000-000000000201"}""",
        )
        Unit
    }

    @Test
    fun `update training plan through training container`() = runBlocking {
        assertPlanOperation(
            operation = "update",
            payload = """"trainingPlan":{"id":"00000000-0000-0000-0000-000000000201","lock":"stub-lock-training-plan","title":"E2E plan","planItems":[{"itemType":"EXERCISE","id":"00000000-0000-0000-0000-000000000301","title":"Squats"}]}""",
        )
        Unit
    }

    @Test
    fun `archive training plan through training container`() = runBlocking {
        val plan = assertPlanOperation(
            operation = "archive",
            payload = """"trainingPlan":{"id":"00000000-0000-0000-0000-000000000201","lock":"stub-lock-training-plan"}""",
        )
        assertEquals("ARCHIVED", plan["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `search training plans through training container`() = runBlocking {
        val requestId = "e2e-training-plan-search-v2"
        val response = client.post(
            "/v2/trainingPlan/search",
            request("search", requestId, """"trainingPlanFilter":{"pageSize":10,"pageNumber":1}"""),
        )

        val json = response.assertSuccess("trainingPlan.search", requestId)
        val plans = requireNotNull(json["trainingPlans"]?.jsonArray) { response.body }
        assertTrue(plans.isNotEmpty(), response.body)
        assertEquals("Базовая тренировка", plans.first().jsonObject["title"]?.jsonPrimitive?.content)
    }

    private suspend fun assertPlanOperation(
        operation: String,
        payload: String,
    ) = run {
        val requestId = "e2e-training-plan-$operation-v2"
        val response = client.post(
            "/v2/trainingPlan/$operation",
            request(operation, requestId, payload),
        )
        val json = response.assertSuccess("trainingPlan.$operation", requestId)
        val plan = requireNotNull(json["trainingPlan"]?.jsonObject) { response.body }
        assertEquals("Базовая тренировка", plan["title"]?.jsonPrimitive?.content)
        plan
    }

    private fun request(operation: String, requestId: String, payload: String) =
        """
            {
              "requestType": "trainingPlan.$operation",
              "requestId": "$requestId",
              $payload,
              "debug": {"mode": "stub", "stub": "success"}
            }
        """.trimIndent()
}
