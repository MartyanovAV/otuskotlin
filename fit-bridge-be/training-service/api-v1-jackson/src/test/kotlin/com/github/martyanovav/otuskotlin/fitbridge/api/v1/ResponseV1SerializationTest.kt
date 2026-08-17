package com.github.martyanovav.otuskotlin.fitbridge.api.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanResponseObject
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ResponseV1SerializationTest {
    private val response =
        TrainingPlanCreateResponse(
            result = ResponseResult.SUCCESS,
            trainingPlan =
                TrainingPlanResponseObject(
                    title = "plan title",
                    planItems = listOf(ExerciseItem(id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), title = "Day 1")),
                ),
        )

    @Test
    fun serialize() {
        val json = apiV1Mapper.writeValueAsString(response)

        assertContains(json, Regex("\"title\":\\s*\"plan title\""))
        assertContains(json, Regex("\"responseType\":\\s*\"trainingPlan\\.create\""))
        assertEquals(1, Regex("\"responseType\"\\s*:").findAll(json).count())
    }

    @Test
    fun deserialize() {
        val json = apiV1Mapper.writeValueAsString(response)
        val obj = apiV1Mapper.readValue(json, IResponse::class.java) as TrainingPlanCreateResponse

        assertEquals(response, obj)
    }
}
