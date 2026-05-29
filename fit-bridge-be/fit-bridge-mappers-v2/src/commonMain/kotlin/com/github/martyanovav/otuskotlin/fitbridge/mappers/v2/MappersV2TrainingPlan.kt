package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.CompletionStatusResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PublicLinkResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanClosePublicLinkObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanClosePublicLinkRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanClosePublicLinkResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanGeneratePublicLinkObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanGeneratePublicLinkRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanGeneratePublicLinkResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadCompletionStatusObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadCompletionStatusRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadCompletionStatusResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.CompletionStatusInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.PublicLinkInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlanCommand
import kotlin.time.Instant

// ─── From Transport ──────────────────────────────────────────────────────────

internal fun TrainingPlanContext.fromTransport(request: TrainingPlanCreateRequest) {
    command = TrainingPlanCommand.CREATE
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

internal fun TrainingPlanContext.fromTransport(request: TrainingPlanReadRequest) {
    command = TrainingPlanCommand.READ
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

internal fun TrainingPlanContext.fromTransport(request: TrainingPlanUpdateRequest) {
    command = TrainingPlanCommand.UPDATE
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

internal fun TrainingPlanContext.fromTransport(request: TrainingPlanArchiveRequest) {
    command = TrainingPlanCommand.ARCHIVE
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

internal fun TrainingPlanContext.fromTransport(request: TrainingPlanGeneratePublicLinkRequest) {
    command = TrainingPlanCommand.GENERATE_PUBLIC_LINK
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan.toInternalId()
    )
    expiresAtRequest = request.trainingPlan?.expiresAt.toInstant()
}

internal fun TrainingPlanContext.fromTransport(request: TrainingPlanClosePublicLinkRequest) {
    command = TrainingPlanCommand.CLOSE_PUBLIC_LINK
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan.toInternalId()
    )
}

internal fun TrainingPlanContext.fromTransport(request: TrainingPlanReadCompletionStatusRequest) {
    command = TrainingPlanCommand.READ_COMPLETION_STATUS
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan.toInternalId()
    )
}

// ─── To Transport ────────────────────────────────────────────────────────────

internal fun TrainingPlanContext.toTransportTrainingPlanCreate() = TrainingPlanCreateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanRead() = TrainingPlanReadResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanUpdate() = TrainingPlanUpdateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanArchive() = TrainingPlanArchiveResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanGeneratePublicLink() = TrainingPlanGeneratePublicLinkResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    publicLink = publicLinkResponse.toTransportPublicLink()
)

internal fun TrainingPlanContext.toTransportTrainingPlanClosePublicLink() = TrainingPlanClosePublicLinkResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanReadCompletionStatus() = TrainingPlanReadCompletionStatusResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    completionStatus = completionStatusResponse.toTransportCompletionStatus()
)

internal fun TrainingPlan.toTransportTrainingPlan(): TrainingPlanResponseObject? {
    if (this == TrainingPlan()) return null
    return TrainingPlanResponseObject(
        id = id.takeIf { it != TrainingPlanId.NONE }?.asString(),
        title = title.takeIf { it.isNotBlank() },
        clientCardId = clientCardId.takeIf { it != ClientCardId.NONE }?.asString()
    )
}

internal fun PublicLinkInfo.toTransportPublicLink(): PublicLinkResponseObject? {
    if (this == PublicLinkInfo()) return null
    return PublicLinkResponseObject(
        publicUrl = publicUrl.takeIf { it.isNotBlank() },
        publicToken = publicToken.takeIf { it.isNotBlank() },
        expiresAt = expiresAt.takeIf { it != Instant.DISTANT_PAST }?.toString()
    )
}

internal fun CompletionStatusInfo.toTransportCompletionStatus(): CompletionStatusResponseObject? {
    if (this == CompletionStatusInfo()) return null
    return CompletionStatusResponseObject(
        trainingPlanId = trainingPlanId.takeIf { it != TrainingPlanId.NONE }?.asString()
    )
}

// ─── Private: Request DTO to Internal ────────────────────────────────────────

private fun TrainingPlanCreateObject?.toInternal() = TrainingPlan(
    title = this?.title.orEmpty(),
    clientCardId = this?.clientCardId.toClientCardId(),
    planItems = this?.planItems?.map {
        PlanItem(
            itemRef = it.itemRef,
            title = it.title,
            description = it.description.orEmpty()
        )
    }?.toMutableList() ?: mutableListOf()
)

private fun TrainingPlanReadObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId()
)

private fun TrainingPlanUpdateObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId(),
    title = this?.title.orEmpty(),
    lock = this?.lock.orEmpty(),
    planItems = this?.planItems?.map {
        PlanItem(
            itemRef = it.itemRef,
            title = it.title,
            description = it.description.orEmpty()
        )
    }?.toMutableList() ?: mutableListOf()
)

private fun TrainingPlanArchiveObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId(),
    lock = this?.lock.orEmpty()
)

private fun TrainingPlanGeneratePublicLinkObject?.toInternalId() = this?.id.toTrainingPlanId()

private fun TrainingPlanClosePublicLinkObject?.toInternalId() = this?.id.toTrainingPlanId()

private fun TrainingPlanReadCompletionStatusObject?.toInternalId() = this?.id.toTrainingPlanId()
