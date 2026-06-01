package com.github.martyanovav.otuskotlin.fitbridge.api.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ResponseV2SerializationTest {
    private val response: TrainingPlanCreateResponse = TrainingPlanCreateResponse(
        result = ResponseResult.SUCCESS,
        trainingPlan = TrainingPlanResponseObject(
            title = "plan title",
            planItems = listOf(ExerciseItem(id = "550e8400-e29b-41d4-a716-446655440000", title = "Day 1")),
        ),
    )

    @Test
    fun serialize() {
        val json = apiV2Mapper.encodeToString(IResponse.serializer(), response)

        println(json)

        assertContains(json, Regex("\"title\":\\s*\"plan title\""))
        assertContains(json, Regex("\"responseType\":\\s*\"trainingPlan\\.create\""))
    }

    @Test
    fun deserialize() {
        val json = apiV2Mapper.encodeToString(IResponse.serializer(), response)
        val obj = apiV2Mapper.decodeFromString(IResponse.serializer(), json) as TrainingPlanCreateResponse

        assertEquals(response, obj)
    }
}
