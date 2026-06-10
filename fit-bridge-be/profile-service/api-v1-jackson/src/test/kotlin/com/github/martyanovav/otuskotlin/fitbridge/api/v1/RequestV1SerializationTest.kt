package com.github.martyanovav.otuskotlin.fitbridge.api.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateRequest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class RequestV1SerializationTest {
    private val request = TrainerProfileCreateOrUpdateRequest(
        requestType = "trainerProfile.createOrUpdate",
        debug = Debug(
            mode = RequestDebugMode.STUB,
            stub = RequestDebugStubs.BAD_PUBLIC_NAME,
        ),
        trainerProfile = TrainerProfileCreateOrUpdateObject(
            publicName = "Trainer John",
        ),
    )

    @Test
    fun serialize() {
        val json = apiV1Mapper.writeValueAsString(request)
        assertContains(json, Regex("\"publicName\":\\s*\"Trainer John\""))
        assertContains(json, Regex("\"requestType\":\\s*\"trainerProfile\\.createOrUpdate\""))
    }

    @Test
    fun deserialize() {
        val json = apiV1Mapper.writeValueAsString(request)
        val obj = apiV1Mapper.readValue(json, IRequest::class.java) as TrainerProfileCreateOrUpdateRequest
        assertEquals(request, obj)
    }
}
