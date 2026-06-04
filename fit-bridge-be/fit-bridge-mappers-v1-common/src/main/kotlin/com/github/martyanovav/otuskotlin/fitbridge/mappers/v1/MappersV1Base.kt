package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Error
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
import kotlin.time.Instant

// ─── ID conversion helpers ───────────────────────────────────────────────────

fun String?.toTrainerId() = this?.let { TrainerId(it) } ?: TrainerId.NONE

fun String?.toClientCardId() = this?.let { ClientCardId(it) } ?: ClientCardId.NONE

fun String?.toTrainingPlanId() = this?.let { TrainingPlanId(it) } ?: TrainingPlanId.NONE

fun String?.toRequestId() = this?.let { RequestId(it) } ?: RequestId.NONE

fun String?.toInstant() = this?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST

// ─── Debug helpers ───────────────────────────────────────────────────────────

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
    RequestDebugStubs.BAD_PUBLIC_NAME -> Stubs.BAD_PUBLIC_NAME
    RequestDebugStubs.BAD_PLAN_TITLE -> Stubs.BAD_PLAN_TITLE
    RequestDebugStubs.CANNOT_ARCHIVE -> Stubs.CANNOT_ARCHIVE
    null -> Stubs.NONE
    else -> Stubs.NONE
}

// ─── Base context setup ──────────────────────────────────────────────────────

fun IFBContext.fromTransportBase(reqId: String?, debug: Debug?) {
    requestId = reqId.toRequestId()
    workMode = debug.transportToWorkMode()
    stubCase = debug.transportToStubCase()
}

// ─── Error conversion ────────────────────────────────────────────────────────

fun List<FBError>.toTransportErrors(): List<Error>? = this
    .map { it.toTransport() }
    .takeIf { it.isNotEmpty() }

fun FBError.toTransport() = Error(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() }
)

