package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.*
import com.github.martyanovav.otuskotlin.fitbridge.common.FBContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.*
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
import kotlin.test.Test
import kotlin.test.assertEquals

class MapperTest {

    @Test
    fun fromTransport() {
        val req = TrainerProfileCreateOrUpdateRequest(
            requestId = "12345",
            debug = Debug(
                mode = RequestDebugMode.STUB,
                stub = RequestDebugStubs.SUCCESS
            ),
            trainerProfile = TrainerProfileCreateOrUpdateObject(
                publicName = "Trainer John",
                specialization = "Yoga"
            )
        )

        val context = FBContext()
        context.fromTransport(req)

        assertEquals(Stubs.SUCCESS, context.stubCase)
        assertEquals(WorkMode.STUB, context.workMode)
        assertEquals("Trainer John", context.trainerProfileRequest.publicName)
        assertEquals("Yoga", context.trainerProfileRequest.specialization)
    }

    @Test
    fun toTransport() {
        val context = FBContext(
            requestId = RequestId("1234"),
            command = FBCommand.TRAINER_PROFILE_CREATE_OR_UPDATE,
            trainerProfileResponse = TrainerProfile(
                publicName = "Trainer John",
                specialization = "Yoga"
            ),
            errors = mutableListOf(
                FBError(
                    code = "err",
                    group = "request",
                    field = "publicName",
                    message = "wrong name"
                )
            ),
            state = State.RUNNING
        )

        val req = context.toTransport() as TrainerProfileCreateOrUpdateResponse

        assertEquals("1234", req.requestId)
        assertEquals("Trainer John", req.trainerProfile?.publicName)
        assertEquals("Yoga", req.trainerProfile?.specialization)
        assertEquals(1, req.errors?.size)
        assertEquals("err", req.errors?.firstOrNull()?.code)
        assertEquals("request", req.errors?.firstOrNull()?.group)
        assertEquals("publicName", req.errors?.firstOrNull()?.field)
        assertEquals("wrong name", req.errors?.firstOrNull()?.message)
    }
}
