package com.github.martyanovav.otuskotlin.fitbridge.api.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.*
import sun.security.util.Debug
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class RequestV1SerializationTest {
    private val request = TrainingPlanCreateRequest(
        requestType = "trainingPlan.create",
        debug = Debug(
            mode = RequestDebugMode.STUB,
            stub = RequestDebugStubs.BAD_PLAN_TITLE,
        ),
        trainingPlan = TrainingPlanCreateObject(
            title = "plan title",
            planItems = listOf(PlanItem(itemRef = "week1.day1", title = "Day 1")),
        ),
    )

    @Test
    fun serialize() {
        val json = apiV1Mapper.writeValueAsString(request)

        assertContains(json, Regex("\"title\":\\s*\"plan title\""))
        assertContains(json, Regex("\"mode\":\\s*\"stub\""))
        assertContains(json, Regex("\"stub\":\\s*\"badPlanTitle\""))
        assertContains(json, Regex("\"requestType\":\\s*\"trainingPlan\\.create\""))
    }

    @Test
    fun deserialize() {
        val json = apiV1Mapper.writeValueAsString(request)
        val obj = apiV1Mapper.readValue(json, IRequest::class.java) as TrainingPlanCreateRequest

        assertEquals(request, obj)
    }

    @Test
    fun deserializeNaked() {
        val jsonString = """
            {"requestType": "trainingPlan.create", "trainingPlan": null}
        """.trimIndent()
        val obj = apiV1Mapper.readValue(jsonString, TrainingPlanCreateRequest::class.java)

        assertEquals(null, obj.trainingPlan)
    }
}
