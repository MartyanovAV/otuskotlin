package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.CompletionStatusResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicLinkResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanClosePublicLinkObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanClosePublicLinkRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanClosePublicLinkResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanGeneratePublicLinkObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanGeneratePublicLinkRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanGeneratePublicLinkResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadCompletionStatusObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadCompletionStatusRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadCompletionStatusResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.CompletionStatusInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.CircuitItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.SupersetItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.ExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.PublicLinkInfo
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PlanItem as PlanItemV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ExerciseItem as ExerciseItemV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.CircuitItem as CircuitItemV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.SupersetItem as SupersetItemV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ExerciseSet as ExerciseSetV1
import java.util.UUID
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlanCommand
import java.net.URI

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
        clientCardId = clientCardId.takeIf { it != ClientCardId.NONE }?.asString(),
        planItems = planItems.map { it.toTransportPlanItem() }
    )
}

private fun PlanItem.toTransportPlanItem(): PlanItemV1 = when(this) {
    is ExerciseItem -> ExerciseItemV1(
        id = UUID.fromString(this.id),
        title = this.title,
        description = this.description.takeIf { it.isNotBlank() },
        exerciseId = this.exerciseId,
        sets = this.sets.map {
            ExerciseSetV1(
                reps = it.reps,
                weight = it.weight,
                weightUnit = it.weightUnit,
                durationSeconds = it.durationSeconds
            )
        },
        restBetweenSetsSeconds = this.restBetweenSetsSeconds
    )
    is CircuitItem -> CircuitItemV1(
        id = UUID.fromString(this.id),
        title = this.title,
        description = this.description.takeIf { it.isNotBlank() },
        rounds = this.rounds,
        items = this.items.map { it.toTransportPlanItem() },
        restBetweenRoundsSeconds = this.restBetweenRoundsSeconds
    )
    is SupersetItem -> SupersetItemV1(
        id = UUID.fromString(this.id),
        title = this.title,
        description = this.description.takeIf { it.isNotBlank() },
        items = this.items.map { it.toTransportPlanItem() },
        restBetweenSetsSeconds = this.restBetweenSetsSeconds
    )
}

internal fun PublicLinkInfo.toTransportPublicLink(): PublicLinkResponseObject? {
    if (this == PublicLinkInfo()) return null
    return PublicLinkResponseObject(
        publicUrl = publicUrl.takeIf { it.isNotBlank() }?.let { URI(it) },
        publicToken = publicToken.takeIf { it.isNotBlank() },
        expiresAt = expiresAt.takeIf { it != kotlin.time.Instant.DISTANT_PAST }?.toString()
    )
}

internal fun CompletionStatusInfo.toTransportCompletionStatus(): CompletionStatusResponseObject? {
    if (this == CompletionStatusInfo()) return null
    return CompletionStatusResponseObject(
        trainingPlanId = trainingPlanId.takeIf { it != TrainingPlanId.NONE }?.asString()
    )
}

// ─── Private: Request DTO to Internal ────────────────────────────────────────

private fun PlanItemV1.toInternal(): PlanItem = when(this) {
    is ExerciseItemV1 -> ExerciseItem(
        id = this.id.toString(),
        title = this.title,
        description = this.description.orEmpty(),
        exerciseId = this.exerciseId.orEmpty(),
        sets = this.sets?.map {
            ExerciseSet(
                reps = it.reps.orEmpty(),
                weight = it.weight.orEmpty(),
                weightUnit = it.weightUnit.orEmpty(),
                durationSeconds = it.durationSeconds ?: 0
            )
        }?.toMutableList() ?: mutableListOf(),
        restBetweenSetsSeconds = this.restBetweenSetsSeconds ?: 0
    )
    is CircuitItemV1 -> CircuitItem(
        id = this.id.toString(),
        title = this.title,
        description = this.description.orEmpty(),
        rounds = this.rounds ?: 1,
        items = this.items?.map { it.toInternal() }?.toMutableList() ?: mutableListOf(),
        restBetweenRoundsSeconds = this.restBetweenRoundsSeconds ?: 0
    )
    is SupersetItemV1 -> SupersetItem(
        id = this.id.toString(),
        title = this.title,
        description = this.description.orEmpty(),
        items = this.items?.map { it.toInternal() }?.toMutableList() ?: mutableListOf(),
        restBetweenSetsSeconds = this.restBetweenSetsSeconds ?: 0
    )
    else -> throw IllegalArgumentException("Unknown plan item type")
}

private fun TrainingPlanCreateObject?.toInternal() = TrainingPlan(
    title = this?.title.orEmpty(),
    clientCardId = this?.clientCardId.toClientCardId(),
    planItems = this?.planItems?.map { it.toInternal() }?.toMutableList() ?: mutableListOf()
)

private fun TrainingPlanReadObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId()
)

private fun TrainingPlanUpdateObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId(),
    title = this?.title.orEmpty(),
    lock = this?.lock.orEmpty(),
    planItems = this?.planItems?.map { it.toInternal() }?.toMutableList() ?: mutableListOf()
)

private fun TrainingPlanArchiveObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId(),
    lock = this?.lock.orEmpty()
)

private fun TrainingPlanGeneratePublicLinkObject?.toInternalId() = this?.id.toTrainingPlanId()

private fun TrainingPlanClosePublicLinkObject?.toInternalId() = this?.id.toTrainingPlanId()

private fun TrainingPlanReadCompletionStatusObject?.toInternalId() = this?.id.toTrainingPlanId()
