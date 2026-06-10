package com.github.martyanovav.otuskotlin.fitbridge.api.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileResponseObject
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ResponseV2SerializationTest {
    private val response: TrainerProfileCreateOrUpdateResponse = TrainerProfileCreateOrUpdateResponse(
        result = ResponseResult.SUCCESS,
        trainerProfile = TrainerProfileResponseObject(
            publicName = "Trainer John"
        ),
    )

    @Test
    fun serialize() {
        val json = apiV2Mapper.encodeToString(IResponse.serializer(), response)
        assertContains(json, Regex("\"publicName\":\\s*\"Trainer John\""))
        assertContains(json, Regex("\"responseType\":\\s*\"trainerProfile\\.createOrUpdate\""))
    }

    @Test
    fun deserialize() {
        val json = apiV2Mapper.encodeToString(IResponse.serializer(), response)
        val obj = apiV2Mapper.decodeFromString(IResponse.serializer(), json) as TrainerProfileCreateOrUpdateResponse
        assertEquals(response, obj)
    }
}
