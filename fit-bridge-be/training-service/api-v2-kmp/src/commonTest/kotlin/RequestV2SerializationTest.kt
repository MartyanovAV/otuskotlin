package com.github.martyanovav.otuskotlin.fitbridge.api.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateRequest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class RequestV2SerializationTest {
    private val request: TrainingPlanCreateRequest =
        TrainingPlanCreateRequest(
            debug =
                Debug(
                    mode = RequestDebugMode.STUB,
                    stub = RequestDebugStubs.BAD_PLAN_TITLE,
                ),
            trainingPlan =
                TrainingPlanCreateObject(
                    title = "plan title",
                    clientCardId = "client-123",
                    planItems = listOf(ExerciseItem(id = "550e8400-e29b-41d4-a716-446655440000", title = "Day 1")),
                ),
        )

    @Test
    fun serialize() {
        val json = apiV2Mapper.encodeToString(IRequest.serializer(), request)

        println(json)

        assertContains(json, Regex("\"title\":\\s*\"plan title\""))
        assertContains(json, Regex("\"mode\":\\s*\"stub\""))
        assertContains(json, Regex("\"stub\":\\s*\"badPlanTitle\""))
        assertContains(json, Regex("\"requestType\":\\s*\"trainingPlan\\.create\""))
    }

    @Test
    fun deserialize() {
        val json = apiV2Mapper.encodeToString(IRequest.serializer(), request)
        val obj = apiV2Mapper.decodeFromString(IRequest.serializer(), json) as TrainingPlanCreateRequest

        assertEquals(request, obj)
    }

    @Test
    fun deserializeNaked() {
        val jsonString =
            """
            {"requestType": "trainingPlan.create", "trainingPlan": null}
            """.trimIndent()
        val obj = apiV2Mapper.decodeFromString(IRequest.serializer(), jsonString) as TrainingPlanCreateRequest

        assertEquals(null, obj.trainingPlan)
    }
}
