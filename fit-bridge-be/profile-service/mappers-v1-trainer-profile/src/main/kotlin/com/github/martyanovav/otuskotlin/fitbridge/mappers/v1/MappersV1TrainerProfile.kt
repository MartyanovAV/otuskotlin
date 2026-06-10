package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileReadOwnRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileReadOwnResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.TrainerProfileContext
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerId
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerProfileCommand

// ─── From Transport ──────────────────────────────────────────────────────────

fun TrainerProfileReadOwnRequest.fromTransport(): TrainerProfileContext = TrainerProfileContext().apply { fromTransport(this@fromTransport) }
fun TrainerProfileCreateOrUpdateRequest.fromTransport(): TrainerProfileContext = TrainerProfileContext().apply { fromTransport(this@fromTransport) }

fun TrainerProfileContext.toTransport(): Any = when (command) {
    TrainerProfileCommand.READ_OWN -> toTransportTrainerProfileReadOwn()
    TrainerProfileCommand.CREATE_OR_UPDATE -> toTransportTrainerProfileCreateOrUpdate()
    else -> throw IllegalArgumentException("Unsupported trainer profile command $command")
}

internal fun TrainerProfileContext.fromTransport(request: TrainerProfileReadOwnRequest) {
    command = TrainerProfileCommand.READ_OWN
    fromTransportBase(request.requestId, request.debug)
}

internal fun TrainerProfileContext.fromTransport(request: TrainerProfileCreateOrUpdateRequest) {
    command = TrainerProfileCommand.CREATE_OR_UPDATE
    fromTransportBase(request.requestId, request.debug)
    trainerProfileRequest = request.trainerProfile.toInternal()
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

// ─── Private: Request DTO to Internal ────────────────────────────────────────

private fun TrainerProfileCreateOrUpdateObject?.toInternal() = this?.let {
    TrainerProfile(
        publicName = it.publicName.orEmpty(),
        specialization = it.specialization.orEmpty()
    )
} ?: TrainerProfile()
