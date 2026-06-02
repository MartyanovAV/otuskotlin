package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanSearchFilter
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanSearchResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.CircuitItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.SupersetItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.ExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PlanItem as PlanItemV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ExerciseItem as ExerciseItemV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.CircuitItem as CircuitItemV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.SupersetItem as SupersetItemV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ExerciseSet as ExerciseSetV2
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlanFilter
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlanCommand

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

internal fun TrainingPlanContext.fromTransport(request: TrainingPlanSearchRequest) {
    command = TrainingPlanCommand.SEARCH
    fromTransportBase(request.requestId, request.debug)
    trainingPlanFilter = request.trainingPlanFilter.toInternal()
    trainingPlansResponse = com.github.martyanovav.otuskotlin.fitbridge.common.models.Page(
        pageNumber = trainingPlanFilter.pageNumber,
        pageSize = trainingPlanFilter.pageSize,
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

internal fun TrainingPlanContext.toTransportTrainingPlanSearch() = TrainingPlanSearchResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlans = trainingPlansResponse.items.mapNotNull { it.toTransportTrainingPlan() }.takeIf { it.isNotEmpty() },
    totalSize = trainingPlansResponse.totalSize,
    pageNumber = trainingPlansResponse.pageNumber.takeIf { it > 0 },
    pageSize = trainingPlansResponse.pageSize.takeIf { it > 0 },
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

private fun PlanItem.toTransportPlanItem(): PlanItemV2 = when(this) {
    is ExerciseItem -> ExerciseItemV2(
        id = this.id,
        title = this.title,
        description = this.description.takeIf { it.isNotBlank() },
        exerciseId = this.exerciseId,
        sets = this.sets.map {
            ExerciseSetV2(
                reps = it.reps,
                weight = it.weight,
                weightUnit = it.weightUnit,
                durationSeconds = it.durationSeconds
            )
        },
        restBetweenSetsSeconds = this.restBetweenSetsSeconds
    )
    is CircuitItem -> CircuitItemV2(
        id = this.id,
        title = this.title,
        description = this.description.takeIf { it.isNotBlank() },
        rounds = this.rounds,
        items = this.items.map { it.toTransportPlanItem() },
        restBetweenRoundsSeconds = this.restBetweenRoundsSeconds
    )
    is SupersetItem -> SupersetItemV2(
        id = this.id,
        title = this.title,
        description = this.description.takeIf { it.isNotBlank() },
        items = this.items.map { it.toTransportPlanItem() },
        restBetweenSetsSeconds = this.restBetweenSetsSeconds
    )
}

// ─── Private: Request DTO to Internal ────────────────────────────────────────

private fun PlanItemV2.toInternal(): PlanItem = when(this) {
    is ExerciseItemV2 -> ExerciseItem(
        id = this.id.orEmpty(),
        title = this.title.orEmpty(),
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
    is CircuitItemV2 -> CircuitItem(
        id = this.id.orEmpty(),
        title = this.title.orEmpty(),
        description = this.description.orEmpty(),
        rounds = this.rounds ?: 1,
        items = this.items?.map { it.toInternal() }?.toMutableList() ?: mutableListOf(),
        restBetweenRoundsSeconds = this.restBetweenRoundsSeconds ?: 0
    )
    is SupersetItemV2 -> SupersetItem(
        id = this.id.orEmpty(),
        title = this.title.orEmpty(),
        description = this.description.orEmpty(),
        items = this.items?.map { it.toInternal() }?.toMutableList() ?: mutableListOf(),
        restBetweenSetsSeconds = this.restBetweenSetsSeconds ?: 0
    )
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

private fun TrainingPlanSearchFilter?.toInternal() = TrainingPlanFilter(
    clientCardId = this?.clientCardId.toClientCardId(),
    status = this?.status?.value.orEmpty(),
    searchString = this?.searchString.orEmpty(),
    pageNumber = this?.pageNumber ?: 1,
    pageSize = this?.pageSize ?: 10,
)
