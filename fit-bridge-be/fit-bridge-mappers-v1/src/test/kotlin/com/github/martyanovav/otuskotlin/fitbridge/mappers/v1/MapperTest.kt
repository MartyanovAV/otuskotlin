package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainerProfileContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfileCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
import org.junit.Test
import kotlin.test.assertEquals

class MapperTest {

    @Test
    fun fromTransport() {
        val req = TrainerProfileCreateOrUpdateRequest(
            requestType = "trainerProfile.createOrUpdate",
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

        val context = req.fromTransport() as TrainerProfileContext

        assertEquals(Stubs.SUCCESS, context.stubCase)
        assertEquals(WorkMode.STUB, context.workMode)
        assertEquals("Trainer John", context.trainerProfileRequest.publicName)
        assertEquals("Yoga", context.trainerProfileRequest.specialization)
    }

    @Test
    fun toTransport() {
        val context = TrainerProfileContext(
            requestId = RequestId("1234"),
            command = TrainerProfileCommand.CREATE_OR_UPDATE,
            trainerProfileResponse = TrainerProfile(
                publicName = "Trainer John",
                specialization = "Yoga"
            ),
            state = State.RUNNING
        ).apply {
            addError(
                FBError(
                    code = "err",
                    group = "request",
                    field = "publicName",
                    message = "wrong name"
                )
            )
        }

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
