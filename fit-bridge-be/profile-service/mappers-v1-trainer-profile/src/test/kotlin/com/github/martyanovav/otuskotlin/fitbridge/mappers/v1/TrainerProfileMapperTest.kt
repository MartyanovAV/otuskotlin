package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileReadOwnRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileReadOwnResponse
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.TrainerProfileContext
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerProfileCommand
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerId
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerProfile
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainerProfileMapperTest {
    @Test
    fun `trainer profile create or update request maps to context`() {
        val request = TrainerProfileCreateOrUpdateRequest(
            requestType = "trainerProfile.createOrUpdate",
            requestId = "tp-create-1",
            debug = Debug(mode = RequestDebugMode.TEST),
            trainerProfile = TrainerProfileCreateOrUpdateObject(
                publicName = "Coach Anna",
                specialization = "Strength",
            ),
        )

        val context = request.fromTransport()

        assertEquals(TrainerProfileCommand.CREATE_OR_UPDATE, context.command)
        assertEquals(RequestId("tp-create-1"), context.requestId)
        assertEquals(WorkMode.TEST, context.workMode)
        assertEquals("Coach Anna", context.trainerProfileRequest.publicName)
        assertEquals("Strength", context.trainerProfileRequest.specialization)
    }

    @Test
    fun `trainer profile read own request maps to context`() {
        val request = TrainerProfileReadOwnRequest(
            requestType = "trainerProfile.readOwn",
            requestId = "tp-read-1",
            debug = Debug(mode = RequestDebugMode.STUB),
        )

        val context = request.fromTransport()

        assertEquals(TrainerProfileCommand.READ_OWN, context.command)
        assertEquals(RequestId("tp-read-1"), context.requestId)
        assertEquals(WorkMode.STUB, context.workMode)
    }

    @Test
    fun `trainer profile context maps to create or update response`() {
        val context = TrainerProfileContext(
            requestId = RequestId("tp-response-1"),
            command = TrainerProfileCommand.CREATE_OR_UPDATE,
            state = State.RUNNING,
            trainerProfileResponse = TrainerProfile(
                id = TrainerId("trainer-1"),
                publicName = "Coach Anna",
                specialization = "Strength",
            ),
        )

        val response = context.toTransport() as TrainerProfileCreateOrUpdateResponse

        assertEquals("tp-response-1", response.requestId)
        assertEquals("trainer-1", response.trainerProfile?.id)
        assertEquals("Coach Anna", response.trainerProfile?.publicName)
        assertEquals("Strength", response.trainerProfile?.specialization)
    }

    @Test
    fun `trainer profile context maps to read own response`() {
        val context = TrainerProfileContext(
            requestId = RequestId("tp-read-response-1"),
            command = TrainerProfileCommand.READ_OWN,
            state = State.RUNNING,
            trainerProfileResponse = TrainerProfile(publicName = "Coach Bob"),
        )

        val response = context.toTransport() as TrainerProfileReadOwnResponse

        assertEquals("tp-read-response-1", response.requestId)
        assertEquals("Coach Bob", response.trainerProfile?.publicName)
    }
}
