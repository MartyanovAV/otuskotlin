package com.github.martyanovav.otuskotlin.fitbridge.api.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileResponseObject
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ResponseV1SerializationTest {
    private val response = TrainerProfileCreateOrUpdateResponse(
        responseType = "trainerProfile.createOrUpdate",
        result = ResponseResult.SUCCESS,
        trainerProfile = TrainerProfileResponseObject(
            publicName = "Trainer John",
        ),
    )

    @Test
    fun serialize() {
        val json = apiV1Mapper.writeValueAsString(response)
        assertContains(json, Regex("\"publicName\":\\s*\"Trainer John\""))
        assertContains(json, Regex("\"responseType\":\\s*\"trainerProfile\\.createOrUpdate\""))
    }

    @Test
    fun deserialize() {
        val json = apiV1Mapper.writeValueAsString(response)
        val obj = apiV1Mapper.readValue(json, IResponse::class.java) as TrainerProfileCreateOrUpdateResponse
        assertEquals(response, obj)
    }
}
