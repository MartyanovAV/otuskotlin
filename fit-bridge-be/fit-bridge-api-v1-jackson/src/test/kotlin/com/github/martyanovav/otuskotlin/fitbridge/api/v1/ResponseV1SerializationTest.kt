package com.github.martyanovav.otuskotlin.fitbridge.api.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.*
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ResponseV1SerializationTest {
    private val response = TrainingPlanCreateResponse(
        result = ResponseResult.SUCCESS,
        trainingPlan = TrainingPlanResponseObject(
            title = "plan title",
            planItems = listOf(PlanItem(itemRef = "week1.day1", title = "Day 1")),
        ),
    )

    @Test
    fun serialize() {
        val json = apiV1Mapper.writeValueAsString(response)

        assertContains(json, Regex("\"title\":\\s*\"plan title\""))
        assertContains(json, Regex("\"responseType\":\\s*\"trainingPlan\\.create\""))
    }

    @Test
    fun deserialize() {
        val json = apiV1Mapper.writeValueAsString(response)
        val obj = apiV1Mapper.readValue(json, IResponse::class.java) as TrainingPlanCreateResponse

        assertEquals(response, obj)
    }
}
