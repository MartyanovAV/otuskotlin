package com.github.martyanovav.otuskotlin.fitbridge.api.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileCreateOrUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileCreateOrUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugStubs
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class RequestV2SerializationTest {
    private val request: TrainerProfileCreateOrUpdateRequest = TrainerProfileCreateOrUpdateRequest(
        debug = Debug(
            mode = RequestDebugMode.STUB,
            stub = RequestDebugStubs.BAD_PUBLIC_NAME,
        ),
        trainerProfile = TrainerProfileCreateOrUpdateObject(
            publicName = "Trainer John"
        ),
    )

    @Test
    fun serialize() {
        val json = apiV2Mapper.encodeToString(IRequest.serializer(), request)
        assertContains(json, Regex("\"publicName\":\\s*\"Trainer John\""))
        assertContains(json, Regex("\"mode\":\\s*\"stub\""))
        assertContains(json, Regex("\"stub\":\\s*\"badPublicName\""))
        assertContains(json, Regex("\"requestType\":\\s*\"trainerProfile\\.createOrUpdate\""))
    }

    @Test
    fun deserialize() {
        val json = apiV2Mapper.encodeToString(IRequest.serializer(), request)
        val obj = apiV2Mapper.decodeFromString(IRequest.serializer(), json) as TrainerProfileCreateOrUpdateRequest
        assertEquals(request, obj)
    }

    @Test
    fun deserializeNaked() {
        val jsonString = """
            {"requestType": "trainerProfile.createOrUpdate", "trainerProfile": null}
        """.trimIndent()
        val obj = apiV2Mapper.decodeFromString(IRequest.serializer(), jsonString) as TrainerProfileCreateOrUpdateRequest
        assertEquals(null, obj.trainerProfile)
    }
}
