package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanActivateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanActivateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanActivateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCompleteObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCompleteRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCompleteResponse
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
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.CircuitItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.SupersetItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkoutDifficulty
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.CircuitItem as CircuitItemV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ExerciseItem as ExerciseItemV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ExerciseSet as ExerciseSetV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PlanItem as PlanItemV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.SupersetItem as SupersetItemV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanStatus as TrainingPlanStatusV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.WorkoutDifficulty as WorkoutDifficultyV2

// ─── From Transport ──────────────────────────────────────────────────────────

fun TrainingPlanCreateRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }

fun TrainingPlanReadRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }

fun TrainingPlanUpdateRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }

fun TrainingPlanArchiveRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }

fun TrainingPlanCompleteRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }

fun TrainingPlanActivateRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }

fun TrainingPlanSearchRequest.fromTransport(): TrainingPlanContext = TrainingPlanContext().apply { fromTransport(this@fromTransport) }

fun TrainingPlanContext.toTransport(): Any =
    when (command) {
        TrainingPlanCommand.CREATE -> toTransportTrainingPlanCreate()
        TrainingPlanCommand.READ -> toTransportTrainingPlanRead()
        TrainingPlanCommand.UPDATE -> toTransportTrainingPlanUpdate()
        TrainingPlanCommand.ARCHIVE -> toTransportTrainingPlanArchive()
        TrainingPlanCommand.ACTIVATE -> toTransportTrainingPlanActivate()
        TrainingPlanCommand.COMPLETE -> toTransportTrainingPlanComplete()
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

fun TrainingPlanContext.fromTransport(request: TrainingPlanActivateRequest) {
    command = TrainingPlanCommand.ACTIVATE
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

fun TrainingPlanContext.fromTransport(request: TrainingPlanCompleteRequest) {
    command = TrainingPlanCommand.COMPLETE
    fromTransportBase(request.requestId, request.debug)
    trainingPlanRequest = request.trainingPlan.toInternal()
}

fun TrainingPlanContext.fromTransport(request: TrainingPlanSearchRequest) {
    command = TrainingPlanCommand.SEARCH
    fromTransportBase(request.requestId, request.debug)
    trainingPlanFilter = request.trainingPlanFilter.toInternal()
    trainingPlansResponse =
        Page(
            pageNumber = trainingPlanFilter.pageNumber,
            pageSize = trainingPlanFilter.pageSize,
        )
}

// ─── To Transport ────────────────────────────────────────────────────────────

fun TrainingPlanContext.toTransportTrainingPlanCreate() =
    TrainingPlanCreateResponse(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
        errors = errors.toTransportErrors(),
        trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
    )

fun TrainingPlanContext.toTransportTrainingPlanRead() =
    TrainingPlanReadResponse(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
        errors = errors.toTransportErrors(),
        trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
    )

fun TrainingPlanContext.toTransportTrainingPlanUpdate() =
    TrainingPlanUpdateResponse(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
        errors = errors.toTransportErrors(),
        trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
    )

fun TrainingPlanContext.toTransportTrainingPlanArchive() =
    TrainingPlanArchiveResponse(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
        errors = errors.toTransportErrors(),
        trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
    )

fun TrainingPlanContext.toTransportTrainingPlanActivate() =
    TrainingPlanActivateResponse(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
        errors = errors.toTransportErrors(),
        trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
    )

fun TrainingPlanContext.toTransportTrainingPlanComplete() =
    TrainingPlanCompleteResponse(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
        errors = errors.toTransportErrors(),
        trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
    )

fun TrainingPlanContext.toTransportTrainingPlanSearch() =
    TrainingPlanSearchResponse(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
        errors = errors.toTransportErrors(),
        trainingPlans = trainingPlansResponse.items.mapNotNull { it.toTransportTrainingPlan() }.takeIf { it.isNotEmpty() },
        totalSize = trainingPlansResponse.totalSize,
        pageNumber = trainingPlansResponse.pageNumber.takeIf { it > 0 },
        pageSize = trainingPlansResponse.pageSize.takeIf { it > 0 },
    )

fun TrainingPlan.toTransportTrainingPlan(): TrainingPlanResponseObject? {
    if (this == TrainingPlan()) return null
    return TrainingPlanResponseObject(
        id = id.takeIf { it != TrainingPlanId.NONE }?.asString(),
        title = title.takeIf { it.isNotBlank() },
        clientCardId = clientCardId.takeIf { it != ClientCardId.NONE }?.asString(),
        status =
            when (status) {
                TrainingPlanStatus.NONE -> null
                TrainingPlanStatus.DRAFT -> TrainingPlanStatusV2.DRAFT
                TrainingPlanStatus.ACTIVE -> TrainingPlanStatusV2.ACTIVE
                TrainingPlanStatus.ARCHIVED -> TrainingPlanStatusV2.ARCHIVED
                TrainingPlanStatus.COMPLETED -> TrainingPlanStatusV2.COMPLETED
            },
        version = version,
        createdAt = createdAt.takeIf { it.isNotBlank() },
        updatedAt = updatedAt.takeIf { it.isNotBlank() },
        lock = lock.takeIf { it != TrainingPlanLock.NONE }?.asString(),
        completedAt = completedAt.takeIf { it.isNotBlank() },
        difficulty = difficulty.takeIf { it != WorkoutDifficulty.NONE }?.toTransportWorkoutDifficulty(),
        coachComment = coachComment.takeIf { it.isNotBlank() },
        planItems = planItems.map { it.toTransportPlanItem() }
    )
}

private fun PlanItem.toTransportPlanItem(): PlanItemV2 =
    when (this) {
        is ExerciseItem ->
            ExerciseItemV2(
                id = this.id,
                title = this.title,
                description = this.description.takeIf { it.isNotBlank() },
                exerciseId = this.exerciseId,
                sets =
                    this.sets.map {
                        ExerciseSetV2(
                            reps = it.reps,
                            weight = it.weight,
                            weightUnit = it.weightUnit,
                            durationSeconds = it.durationSeconds
                        )
                    },
                restBetweenSetsSeconds = this.restBetweenSetsSeconds
            )
        is CircuitItem ->
            CircuitItemV2(
                id = this.id,
                title = this.title,
                description = this.description.takeIf { it.isNotBlank() },
                rounds = this.rounds,
                items = this.items.map { it.toTransportPlanItem() },
                restBetweenRoundsSeconds = this.restBetweenRoundsSeconds
            )
        is SupersetItem ->
            SupersetItemV2(
                id = this.id,
                title = this.title,
                description = this.description.takeIf { it.isNotBlank() },
                items = this.items.map { it.toTransportPlanItem() },
                restBetweenSetsSeconds = this.restBetweenSetsSeconds
            )
    }

// ─── Private: Request DTO to Internal ────────────────────────────────────────

private fun PlanItemV2.toInternal(): PlanItem =
    when (this) {
        is ExerciseItemV2 ->
            ExerciseItem(
                id = this.id.orEmpty(),
                title = this.title.orEmpty(),
                description = this.description.orEmpty(),
                exerciseId = this.exerciseId.orEmpty(),
                sets =
                    this.sets?.map {
                        ExerciseSet(
                            reps = it.reps.orEmpty(),
                            weight = it.weight.orEmpty(),
                            weightUnit = it.weightUnit.orEmpty(),
                            durationSeconds = it.durationSeconds ?: 0
                        )
                    } ?: emptyList(),
                restBetweenSetsSeconds = this.restBetweenSetsSeconds ?: 0
            )
        is CircuitItemV2 ->
            CircuitItem(
                id = this.id.orEmpty(),
                title = this.title.orEmpty(),
                description = this.description.orEmpty(),
                rounds = this.rounds ?: 1,
                items = this.items?.map { it.toInternal() } ?: emptyList(),
                restBetweenRoundsSeconds = this.restBetweenRoundsSeconds ?: 0
            )
        is SupersetItemV2 ->
            SupersetItem(
                id = this.id.orEmpty(),
                title = this.title.orEmpty(),
                description = this.description.orEmpty(),
                items = this.items?.map { it.toInternal() } ?: emptyList(),
                restBetweenSetsSeconds = this.restBetweenSetsSeconds ?: 0
            )
    }

private fun TrainingPlanCreateObject?.toInternal() =
    TrainingPlan(
        title = this?.title.orEmpty(),
        clientCardId = this?.clientCardId.toClientCardId(),
        status = this?.status.toTrainingPlanStatus(),
        planItems = this?.planItems?.map { it.toInternal() } ?: emptyList()
    )

private fun TrainingPlanReadObject?.toInternal() =
    TrainingPlan(
        id = this?.id.toTrainingPlanId()
    )

private fun TrainingPlanUpdateObject?.toInternal() =
    TrainingPlan(
        id = this?.id.toTrainingPlanId(),
        title = this?.title.orEmpty(),
        lock = TrainingPlanLock(this?.lock.orEmpty()),
        planItems = this?.planItems?.map { it.toInternal() } ?: emptyList()
    )

private fun TrainingPlanArchiveObject?.toInternal() =
    TrainingPlan(
        id = this?.id.toTrainingPlanId(),
        lock = TrainingPlanLock(this?.lock.orEmpty())
    )

private fun TrainingPlanActivateObject?.toInternal() =
    TrainingPlan(
        id = this?.id.toTrainingPlanId(),
        lock = TrainingPlanLock(this?.lock.orEmpty())
    )

private fun TrainingPlanCompleteObject?.toInternal() =
    TrainingPlan(
        id = this?.id.toTrainingPlanId(),
        lock = TrainingPlanLock(this?.lock.orEmpty()),
        completedAt = this?.completedAt.orEmpty(),
        difficulty = this?.difficulty.toWorkoutDifficulty(),
        coachComment = this?.coachComment.orEmpty()
    )

private fun TrainingPlanSearchFilter?.toInternal() =
    TrainingPlanFilter(
        clientCardId = this?.clientCardId.toClientCardId(),
        status = this?.status.toTrainingPlanStatus(),
        searchString = this?.searchString.orEmpty(),
        pageNumber = this?.pageNumber ?: 1,
        pageSize = this?.pageSize ?: 10,
    )

private fun TrainingPlanStatusV2?.toTrainingPlanStatus() =
    when (this) {
        TrainingPlanStatusV2.DRAFT -> TrainingPlanStatus.DRAFT
        TrainingPlanStatusV2.ACTIVE -> TrainingPlanStatus.ACTIVE
        TrainingPlanStatusV2.ARCHIVED -> TrainingPlanStatus.ARCHIVED
        TrainingPlanStatusV2.COMPLETED -> TrainingPlanStatus.COMPLETED
        null -> TrainingPlanStatus.NONE
    }

private fun WorkoutDifficultyV2?.toWorkoutDifficulty() =
    when (this) {
        WorkoutDifficultyV2.EASY -> WorkoutDifficulty.EASY
        WorkoutDifficultyV2.NORMAL -> WorkoutDifficulty.NORMAL
        WorkoutDifficultyV2.HARD -> WorkoutDifficulty.HARD
        WorkoutDifficultyV2.MAX -> WorkoutDifficulty.MAX
        null -> WorkoutDifficulty.NONE
    }

private fun WorkoutDifficulty.toTransportWorkoutDifficulty() =
    when (this) {
        WorkoutDifficulty.EASY -> WorkoutDifficultyV2.EASY
        WorkoutDifficulty.NORMAL -> WorkoutDifficultyV2.NORMAL
        WorkoutDifficulty.HARD -> WorkoutDifficultyV2.HARD
        WorkoutDifficulty.MAX -> WorkoutDifficultyV2.MAX
        WorkoutDifficulty.NONE -> null
    }
