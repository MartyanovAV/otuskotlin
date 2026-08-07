package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Error
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.InitResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import kotlin.time.Instant

fun String?.toClientCardId() = this?.let { ClientCardId(it) } ?: ClientCardId.NONE

fun String?.toTrainingPlanId() = this?.let { TrainingPlanId(it) } ?: TrainingPlanId.NONE

fun String?.toRequestId() = this?.let { RequestId(it) } ?: RequestId.NONE

fun String?.toInstant() = this?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST

fun Debug?.transportToWorkMode(): WorkMode = when (this?.mode) {
    RequestDebugMode.PROD -> WorkMode.PROD
    RequestDebugMode.TEST -> WorkMode.TEST
    RequestDebugMode.STUB -> WorkMode.STUB
    null -> WorkMode.PROD
}

fun Debug?.transportToStubCase(): Stubs = when (this?.stub) {
    RequestDebugStubs.SUCCESS -> Stubs.SUCCESS
    RequestDebugStubs.NOT_FOUND -> Stubs.NOT_FOUND
    RequestDebugStubs.BAD_ID -> Stubs.BAD_ID
    RequestDebugStubs.BAD_PLAN_TITLE -> Stubs.BAD_PLAN_TITLE
    RequestDebugStubs.CANNOT_ARCHIVE -> Stubs.CANNOT_ARCHIVE
    RequestDebugStubs.BAD_PUBLIC_NAME -> Stubs.NONE
    RequestDebugStubs.BAD_LOCK -> Stubs.NONE
    RequestDebugStubs.BAD_CLIENT_NAME -> Stubs.NONE
    RequestDebugStubs.BAD_PLAN_BODY -> Stubs.NONE
    RequestDebugStubs.FORBIDDEN -> Stubs.NONE
    RequestDebugStubs.VALIDATION_ERROR -> Stubs.NONE
    null -> Stubs.NONE
}

fun IFBContext.fromTransportBase(reqId: String?, debug: Debug?) {
    requestId = reqId.toRequestId()
    workMode = debug.transportToWorkMode()
    stubCase = debug.transportToStubCase()
}

fun IFBContext.toTransportInit() = InitResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors()
)

fun List<FBError>.toTransportErrors(): List<Error>? = this
    .map { it.toTransport() }
    .takeIf { it.isNotEmpty() }

fun FBError.toTransport() = Error(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() }
)

