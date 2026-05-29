package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileCreateOrUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileReadOwnRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileReadOwnResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainerProfileContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfileCommand

// ─── From Transport ──────────────────────────────────────────────────────────

internal fun TrainerProfileContext.fromTransport(request: TrainerProfileReadOwnRequest) {
    command = TrainerProfileCommand.READ_OWN
    fromTransportBase(request.requestId, request.debug)
}

internal fun TrainerProfileContext.fromTransport(request: TrainerProfileCreateOrUpdateRequest) {
    command = TrainerProfileCommand.CREATE_OR_UPDATE
    fromTransportBase(request.requestId, request.debug)
    trainerProfileRequest = request.trainerProfile?.let {
        TrainerProfile(
            publicName = it.publicName.orEmpty(),
            specialization = it.specialization.orEmpty()
        )
    } ?: TrainerProfile()
}

// ─── To Transport ────────────────────────────────────────────────────────────

internal fun TrainerProfileContext.toTransportTrainerProfileReadOwn() = TrainerProfileReadOwnResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainerProfile = trainerProfileResponse.toTransport()
)

internal fun TrainerProfileContext.toTransportTrainerProfileCreateOrUpdate() = TrainerProfileCreateOrUpdateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainerProfile = trainerProfileResponse.toTransport()
)

internal fun TrainerProfile.toTransport(): TrainerProfileResponseObject? {
    if (this == TrainerProfile()) return null
    return TrainerProfileResponseObject(
        id = id.takeIf { it != TrainerId.NONE }?.asString(),
        publicName = publicName.takeIf { it.isNotBlank() },
        specialization = specialization.takeIf { it.isNotBlank() }
    )
}
