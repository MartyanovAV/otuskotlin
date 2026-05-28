package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Error
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileReadOwnResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.common.FBContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfile

fun FBContext.toTransport(): IResponse = when (val cmd = command) {
    FBCommand.TRAINER_PROFILE_READ_OWN -> toTransportTrainerProfileReadOwn()
    FBCommand.TRAINER_PROFILE_CREATE_OR_UPDATE -> toTransportTrainerProfileCreateOrUpdate()
    // Add other cases when implemented
    else -> throw IllegalArgumentException("Unsupported command $cmd")
}

private fun FBContext.toTransportTrainerProfileReadOwn() = TrainerProfileReadOwnResponse(
    requestId = this.requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainerProfile = trainerProfileResponse.toTransport()
)

private fun FBContext.toTransportTrainerProfileCreateOrUpdate() = TrainerProfileCreateOrUpdateResponse(
    requestId = this.requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainerProfile = trainerProfileResponse.toTransport()
)

private fun TrainerProfile.toTransport(): TrainerProfileResponseObject? {
    if (this == TrainerProfile()) return null
    return TrainerProfileResponseObject(
        id = id.asString().takeIf { it.isNotBlank() },
        publicName = publicName.takeIf { it.isNotBlank() },
        specialization = specialization.takeIf { it.isNotBlank() }
    )
}

private fun List<FBError>.toTransportErrors(): List<Error>? = this
    .map { it.toTransport() }
    .toList()
    .takeIf { it.isNotEmpty() }

private fun FBError.toTransport() = Error(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() }
)
