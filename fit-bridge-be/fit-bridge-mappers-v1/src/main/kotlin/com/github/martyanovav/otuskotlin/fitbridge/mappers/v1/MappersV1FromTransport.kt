package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardListRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.DashboardGetTrainerSummaryRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicPlanMarkCompletionRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicPlanOpenByTokenRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileReadOwnRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanClosePublicLinkRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanGeneratePublicLinkRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadCompletionStatusRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.common.FBContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.CompletionMarkRequest
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.exceptions.UnknownRequestClass

fun FBContext.fromTransport(request: IRequest) {
    when (request) {
        is TrainerProfileReadOwnRequest -> fromTransport(request)
        is TrainerProfileCreateOrUpdateRequest -> fromTransport(request)
        is ClientCardCreateRequest -> fromTransport(request)
        is ClientCardReadRequest -> fromTransport(request)
        is ClientCardUpdateRequest -> fromTransport(request)
        is ClientCardArchiveRequest -> fromTransport(request)
        is ClientCardListRequest -> fromTransport(request)
        is TrainingPlanCreateRequest -> fromTransport(request)
        is TrainingPlanReadRequest -> fromTransport(request)
        is TrainingPlanUpdateRequest -> fromTransport(request)
        is TrainingPlanArchiveRequest -> fromTransport(request)
        is TrainingPlanGeneratePublicLinkRequest -> fromTransport(request)
        is TrainingPlanClosePublicLinkRequest -> fromTransport(request)
        is TrainingPlanReadCompletionStatusRequest -> fromTransport(request)
        is DashboardGetTrainerSummaryRequest -> fromTransport(request)
        is PublicPlanOpenByTokenRequest -> fromTransport(request)
        is PublicPlanMarkCompletionRequest -> fromTransport(request)
        else -> throw UnknownRequestClass(request.javaClass)
    }
}

private fun String?.toTrainerId() = this?.let { TrainerId(it) } ?: TrainerId.NONE

private fun String?.toClientCardId() = this?.let { ClientCardId(it) } ?: ClientCardId.NONE

private fun String?.toTrainingPlanId() = this?.let { TrainingPlanId(it) } ?: TrainingPlanId.NONE

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

fun FBContext.fromTransport(request: ClientCardCreateRequest) {
    command = FBCommand.CLIENT_CARD_CREATE
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    clientCardRequest = ClientCard(
        displayName = request.clientCard?.displayName.orEmpty(),
        note = request.clientCard?.note.orEmpty()
    )
}

fun FBContext.fromTransport(request: ClientCardReadRequest) {
    command = FBCommand.CLIENT_CARD_READ
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    clientCardRequest = ClientCard(
        id = request.clientCard?.id.toClientCardId()
    )
}

fun FBContext.fromTransport(request: ClientCardUpdateRequest) {
    command = FBCommand.CLIENT_CARD_UPDATE
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    clientCardRequest = ClientCard(
        id = request.clientCard?.id.toClientCardId(),
        displayName = request.clientCard?.displayName.orEmpty(),
        note = request.clientCard?.note.orEmpty(),
        lock = request.clientCard?.lock.orEmpty()
    )
}

fun FBContext.fromTransport(request: ClientCardArchiveRequest) {
    command = FBCommand.CLIENT_CARD_ARCHIVE
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    clientCardRequest = ClientCard(
        id = request.clientCard?.id.toClientCardId(),
        lock = request.clientCard?.lock.orEmpty()
    )
}

fun FBContext.fromTransport(request: ClientCardListRequest) {
    command = FBCommand.CLIENT_CARD_LIST
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun FBContext.fromTransport(request: TrainingPlanCreateRequest) {
    command = FBCommand.TRAINING_PLAN_CREATE
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    trainingPlanRequest = TrainingPlan(
        title = request.trainingPlan?.title.orEmpty(),
        clientCardId = request.trainingPlan?.clientCardId.toClientCardId(),
        planItems = request.trainingPlan?.planItems?.map {
            PlanItem(
                itemRef = it.itemRef,
                title = it.title,
                description = it.description.orEmpty()
            )
        }?.toMutableList() ?: mutableListOf()
    )
}

fun FBContext.fromTransport(request: TrainingPlanReadRequest) {
    command = FBCommand.TRAINING_PLAN_READ
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan?.id.toTrainingPlanId()
    )
}

fun FBContext.fromTransport(request: TrainingPlanUpdateRequest) {
    command = FBCommand.TRAINING_PLAN_UPDATE
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan?.id.toTrainingPlanId(),
        title = request.trainingPlan?.title.orEmpty(),
        lock = request.trainingPlan?.lock.orEmpty(),
        planItems = request.trainingPlan?.planItems?.map {
            PlanItem(
                itemRef = it.itemRef,
                title = it.title,
                description = it.description.orEmpty()
            )
        }?.toMutableList() ?: mutableListOf()
    )
}

fun FBContext.fromTransport(request: TrainingPlanArchiveRequest) {
    command = FBCommand.TRAINING_PLAN_ARCHIVE
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan?.id.toTrainingPlanId(),
        lock = request.trainingPlan?.lock.orEmpty()
    )
}

fun FBContext.fromTransport(request: TrainingPlanGeneratePublicLinkRequest) {
    command = FBCommand.TRAINING_PLAN_GENERATE_PUBLIC_LINK
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan?.id.toTrainingPlanId()
    )
    expiresAtRequest = request.trainingPlan?.expiresAt?.let { kotlin.time.Instant.parse(it) } ?: kotlin.time.Instant.DISTANT_PAST
}

fun FBContext.fromTransport(request: TrainingPlanClosePublicLinkRequest) {
    command = FBCommand.TRAINING_PLAN_CLOSE_PUBLIC_LINK
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan?.id.toTrainingPlanId()
    )
}

fun FBContext.fromTransport(request: TrainingPlanReadCompletionStatusRequest) {
    command = FBCommand.TRAINING_PLAN_READ_COMPLETION_STATUS
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    trainingPlanRequest = TrainingPlan(
        id = request.trainingPlan?.id.toTrainingPlanId()
    )
}

fun FBContext.fromTransport(request: DashboardGetTrainerSummaryRequest) {
    command = FBCommand.DASHBOARD_GET_TRAINER_SUMMARY
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun FBContext.fromTransport(request: PublicPlanOpenByTokenRequest) {
    command = FBCommand.PUBLIC_PLAN_OPEN_BY_TOKEN
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    tokenRequest = request.token.orEmpty()
}

fun FBContext.fromTransport(request: PublicPlanMarkCompletionRequest) {
    command = FBCommand.PUBLIC_PLAN_MARK_COMPLETION
    requestId = request.requestId.toRequestId()
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
    tokenRequest = request.token.orEmpty()
    completionMarkRequest = CompletionMarkRequest(
        itemRef = request.completion?.itemRef.orEmpty(),
        status = request.completion?.status?.name.orEmpty(),
        completedAt = request.completion?.completedAt?.let { kotlin.time.Instant.parse(it) } ?: kotlin.time.Instant.DISTANT_PAST,
        clientComment = request.completion?.clientComment.orEmpty()
    )
}
