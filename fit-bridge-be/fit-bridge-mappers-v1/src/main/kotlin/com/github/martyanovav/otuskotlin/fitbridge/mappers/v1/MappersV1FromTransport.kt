package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.*
import com.github.martyanovav.otuskotlin.fitbridge.common.FBContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.*
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.exceptions.UnknownRequestClass

fun FBContext.fromTransport(request: IRequest) {
    when (request) {
        is TrainerProfileReadOwnRequest -> fromTransport(request)
        is TrainerProfileCreateOrUpdateRequest -> fromTransport(request)
        // Add other cases here when they are implemented
        else -> throw UnknownRequestClass(request.javaClass)
    }
}

private fun String?.toTrainerId() = this?.let { TrainerId(it) } ?: TrainerId.NONE

private fun String?.toRequestId() = this?.let { RequestId(it) } ?: RequestId.NONE

private fun Debug?.transportToWorkMode(): WorkMode = when (this?.mode) {
    RequestDebugMode.PROD -> WorkMode.PROD
    RequestDebugMode.TEST -> WorkMode.TEST
    RequestDebugMode.STUB -> WorkMode.STUB
    null -> WorkMode.PROD
}

private fun Debug?.transportToStubCase(): Stubs = when (this?.stub) {
    RequestDebugStubs.SUCCESS -> Stubs.SUCCESS
    RequestDebugStubs.NOT_FOUND -> Stubs.NOT_FOUND
    RequestDebugStubs.BAD_ID -> Stubs.BAD_ID
    RequestDebugStubs.BAD_PUBLIC_NAME -> Stubs.BAD_PUBLIC_NAME
    RequestDebugStubs.BAD_PLAN_TITLE -> Stubs.BAD_PLAN_TITLE
    RequestDebugStubs.CANNOT_ARCHIVE -> Stubs.CANNOT_ARCHIVE
    null -> Stubs.NONE
    else -> Stubs.NONE
}

fun FBContext.fromTransport(request: TrainerProfileReadOwnRequest) {
    command = FBCommand.TRAINER_PROFILE_READ_OWN
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun FBContext.fromTransport(request: TrainerProfileCreateOrUpdateRequest) {
    command = FBCommand.TRAINER_PROFILE_CREATE_OR_UPDATE
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    trainerProfileRequest = request.trainerProfile?.let {
        TrainerProfile(
            publicName = it.publicName.orEmpty(),
            specialization = it.specialization.orEmpty()
        )
    } ?: TrainerProfile()
}
