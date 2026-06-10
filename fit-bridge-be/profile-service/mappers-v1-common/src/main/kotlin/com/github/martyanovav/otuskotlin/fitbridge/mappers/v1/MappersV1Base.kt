package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Error
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.TrainerId
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.profile.common.stubs.Stubs
import kotlin.time.Instant

fun String?.toTrainerId() = this?.let { TrainerId(it) } ?: TrainerId.NONE

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
    RequestDebugStubs.BAD_PUBLIC_NAME -> Stubs.BAD_PUBLIC_NAME
    null -> Stubs.NONE
    else -> Stubs.NONE
}

fun IFBContext.fromTransportBase(reqId: String?, debug: Debug?) {
    requestId = reqId.toRequestId()
    workMode = debug.transportToWorkMode()
    stubCase = debug.transportToStubCase()
}

fun List<FBError>.toTransportErrors(): List<Error>? = this
    .map { it.toTransport() }
    .takeIf { it.isNotEmpty() }

fun FBError.toTransport() = Error(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() }
)

