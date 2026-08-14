package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchFilter
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.CircuitItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.SupersetItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PlanItem as PlanItemV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ExerciseItem as ExerciseItemV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.CircuitItem as CircuitItemV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.SupersetItem as SupersetItemV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ExerciseSet as ExerciseSetV1
import java.util.UUID
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanStatus as TrainingPlanStatusV1

// ─── From Transport ──────────────────────────────────────────────────────────

fun TrainingPlanCreateRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }
fun TrainingPlanReadRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }
fun TrainingPlanUpdateRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }
fun TrainingPlanArchiveRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }
fun TrainingPlanSearchRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }

fun TrainingPlanContext.toTransport(): Any = when (command) {
    TrainingPlanCommand.CREATE -> toTransportTrainingPlanCreate()
    TrainingPlanCommand.READ -> toTransportTrainingPlanRead()
    TrainingPlanCommand.UPDATE -> toTransportTrainingPlanUpdate()
    TrainingPlanCommand.ARCHIVE -> toTransportTrainingPlanArchive()
    TrainingPlanCommand.SEARCH -> toTransportTrainingPlanSearch()
    FBCommandBase.NONE -> toTransportInit()
    FBCommandBase.INIT -> toTransportInit()
    else -> throw IllegalArgumentException("Unsupported training plan command $command")
}

fun TrainingPlanContext.fromTransport(request: TrainingPlanCreateRequest) {
    command = TrainingPlanCommand.CREATE
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

fun TrainingPlanContext.fromTransport(request: TrainingPlanReadRequest) {
    command = TrainingPlanCommand.READ
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

fun TrainingPlanContext.fromTransport(request: TrainingPlanUpdateRequest) {
    command = TrainingPlanCommand.UPDATE
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

fun TrainingPlanContext.fromTransport(request: TrainingPlanArchiveRequest) {
    command = TrainingPlanCommand.ARCHIVE
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

fun TrainingPlanContext.fromTransport(request: TrainingPlanSearchRequest) {
    command = TrainingPlanCommand.SEARCH
    fromTransportBase(request.requestId, request.debug)
    trainingPlanFilter = request.trainingPlanFilter.toInternal()
    trainingPlansResponse = com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page(
        pageNumber = trainingPlanFilter.pageNumber,
        pageSize = trainingPlanFilter.pageSize,
    )
}

// ─── To Transport ────────────────────────────────────────────────────────────

internal fun TrainingPlanContext.toTransportTrainingPlanCreate() = TrainingPlanCreateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanRead() = TrainingPlanReadResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanUpdate() = TrainingPlanUpdateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanArchive() = TrainingPlanArchiveResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

internal fun TrainingPlanContext.toTransportTrainingPlanSearch() = TrainingPlanSearchResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
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
        status = when (status) {
            TrainingPlanStatus.NONE -> null
            TrainingPlanStatus.ACTIVE -> TrainingPlanStatusV1.ACTIVE
            TrainingPlanStatus.ARCHIVED -> TrainingPlanStatusV1.ARCHIVED
        },
        version = version,
        createdAt = createdAt.takeIf { it.isNotBlank() },
        updatedAt = updatedAt.takeIf { it.isNotBlank() },
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
        } ?: emptyList(),
        restBetweenSetsSeconds = this.restBetweenSetsSeconds ?: 0
    )
    is CircuitItemV1 -> CircuitItem(
        id = this.id.toString(),
        title = this.title,
        description = this.description.orEmpty(),
        rounds = this.rounds ?: 1,
        items = this.items?.map { it.toInternal() } ?: emptyList(),
        restBetweenRoundsSeconds = this.restBetweenRoundsSeconds ?: 0
    )
    is SupersetItemV1 -> SupersetItem(
        id = this.id.toString(),
        title = this.title,
        description = this.description.orEmpty(),
        items = this.items?.map { it.toInternal() } ?: emptyList(),
        restBetweenSetsSeconds = this.restBetweenSetsSeconds ?: 0
    )
    else -> throw IllegalArgumentException("Unknown plan item type")
}

private fun TrainingPlanCreateObject?.toInternal() = TrainingPlan(
    title = this?.title.orEmpty(),
    clientCardId = this?.clientCardId.toClientCardId(),
    planItems = this?.planItems?.map { it.toInternal() } ?: emptyList()
)

private fun TrainingPlanReadObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId()
)

private fun TrainingPlanUpdateObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId(),
    title = this?.title.orEmpty(),
    lock = this?.lock.orEmpty(),
    planItems = this?.planItems?.map { it.toInternal() } ?: emptyList()
)

private fun TrainingPlanArchiveObject?.toInternal() = TrainingPlan(
    id = this?.id.toTrainingPlanId(),
    lock = this?.lock.orEmpty()
)

private fun TrainingPlanSearchFilter?.toInternal() = TrainingPlanFilter(
    clientCardId = this?.clientCardId.toClientCardId(),
    status = this?.status.toTrainingPlanStatus(),
    searchString = this?.searchString.orEmpty(),
    pageNumber = this?.pageNumber ?: 1,
    pageSize = this?.pageSize ?: 10,
)

private fun TrainingPlanStatusV1?.toTrainingPlanStatus() = when (this) {
    TrainingPlanStatusV1.ACTIVE -> TrainingPlanStatus.ACTIVE
    TrainingPlanStatusV1.ARCHIVED -> TrainingPlanStatus.ARCHIVED
    null -> TrainingPlanStatus.NONE
}
