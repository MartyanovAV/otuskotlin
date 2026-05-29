package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardListResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.CompletionStatus
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.CompletionStatusResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.DashboardGetTrainerSummaryResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.DashboardSummaryResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Error
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PublicCompletionMarkResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PublicLinkResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PublicPlanMarkCompletionResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PublicPlanOpenByTokenResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.PublicPlanView
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileReadOwnResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainerProfileResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanClosePublicLinkResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanGeneratePublicLinkResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadCompletionStatusResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.common.FBContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.common.models.CompletionMarkInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.CompletionStatusInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.DashboardSummary
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.PublicLinkInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.PublicPlanViewInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlan
import kotlin.time.Instant

fun FBContext.toTransport(): IResponse = when (val cmd = command) {
    FBCommand.TRAINER_PROFILE_READ_OWN -> toTransportTrainerProfileReadOwn()
    FBCommand.TRAINER_PROFILE_CREATE_OR_UPDATE -> toTransportTrainerProfileCreateOrUpdate()
    FBCommand.CLIENT_CARD_CREATE -> toTransportClientCardCreate()
    FBCommand.CLIENT_CARD_READ -> toTransportClientCardRead()
    FBCommand.CLIENT_CARD_UPDATE -> toTransportClientCardUpdate()
    FBCommand.CLIENT_CARD_ARCHIVE -> toTransportClientCardArchive()
    FBCommand.CLIENT_CARD_LIST -> toTransportClientCardList()
    FBCommand.TRAINING_PLAN_CREATE -> toTransportTrainingPlanCreate()
    FBCommand.TRAINING_PLAN_READ -> toTransportTrainingPlanRead()
    FBCommand.TRAINING_PLAN_UPDATE -> toTransportTrainingPlanUpdate()
    FBCommand.TRAINING_PLAN_ARCHIVE -> toTransportTrainingPlanArchive()
    FBCommand.TRAINING_PLAN_GENERATE_PUBLIC_LINK -> toTransportTrainingPlanGeneratePublicLink()
    FBCommand.TRAINING_PLAN_CLOSE_PUBLIC_LINK -> toTransportTrainingPlanClosePublicLink()
    FBCommand.TRAINING_PLAN_READ_COMPLETION_STATUS -> toTransportTrainingPlanReadCompletionStatus()
    FBCommand.DASHBOARD_GET_TRAINER_SUMMARY -> toTransportDashboardGetTrainerSummary()
    FBCommand.PUBLIC_PLAN_OPEN_BY_TOKEN -> toTransportPublicPlanOpenByToken()
    FBCommand.PUBLIC_PLAN_MARK_COMPLETION -> toTransportPublicPlanMarkCompletion()
    else -> throw IllegalArgumentException("Unsupported command $cmd")
}

private fun FBContext.toTransportTrainerProfileReadOwn() = TrainerProfileReadOwnResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainerProfile = trainerProfileResponse.toTransport()
)

private fun FBContext.toTransportTrainerProfileCreateOrUpdate() = TrainerProfileCreateOrUpdateResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainerProfile = trainerProfileResponse.toTransport()
)

private fun FBContext.toTransportClientCardCreate() = ClientCardCreateResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

private fun FBContext.toTransportClientCardRead() = ClientCardReadResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

private fun FBContext.toTransportClientCardUpdate() = ClientCardUpdateResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

private fun FBContext.toTransportClientCardArchive() = ClientCardArchiveResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

private fun FBContext.toTransportClientCardList() = ClientCardListResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCards = clientCardsResponse.mapNotNull { it.toTransportClientCard() }.takeIf { it.isNotEmpty() }
)

private fun FBContext.toTransportTrainingPlanCreate() = TrainingPlanCreateResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

private fun FBContext.toTransportTrainingPlanRead() = TrainingPlanReadResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

private fun FBContext.toTransportTrainingPlanUpdate() = TrainingPlanUpdateResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

private fun FBContext.toTransportTrainingPlanArchive() = TrainingPlanArchiveResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

private fun FBContext.toTransportTrainingPlanGeneratePublicLink() = TrainingPlanGeneratePublicLinkResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    publicLink = publicLinkResponse.toTransportPublicLink()
)

private fun FBContext.toTransportTrainingPlanClosePublicLink() = TrainingPlanClosePublicLinkResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    trainingPlan = trainingPlanResponse.toTransportTrainingPlan()
)

private fun FBContext.toTransportTrainingPlanReadCompletionStatus() = TrainingPlanReadCompletionStatusResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    completionStatus = completionStatusResponse.toTransportCompletionStatus()
)

private fun FBContext.toTransportDashboardGetTrainerSummary() = DashboardGetTrainerSummaryResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    summary = dashboardSummaryResponse.toTransportDashboardSummary()
)

private fun FBContext.toTransportPublicPlanOpenByToken() = PublicPlanOpenByTokenResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    publicPlan = publicPlanViewResponse.toTransportPublicPlanView()
)

private fun FBContext.toTransportPublicPlanMarkCompletion() = PublicPlanMarkCompletionResponse(
    requestId = requestId.asString().takeIf { it.isNotBlank() },
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    completion = completionMarkResponse.toTransportCompletionMark()
)

private fun TrainerProfile.toTransport(): TrainerProfileResponseObject? {
    if (this == TrainerProfile()) return null
    return TrainerProfileResponseObject(
        id = id.asString().takeIf { it.isNotBlank() },
        publicName = publicName.takeIf { it.isNotBlank() },
        specialization = specialization.takeIf { it.isNotBlank() }
    )
}

private fun ClientCard.toTransportClientCard(): ClientCardResponseObject? {
    if (this == ClientCard()) return null
    return ClientCardResponseObject(
        id = id.asString().takeIf { it.isNotBlank() },
        displayName = displayName.takeIf { it.isNotBlank() },
        note = note.takeIf { it.isNotBlank() },
        lock = lock.takeIf { it.isNotBlank() }
    )
}

private fun TrainingPlan.toTransportTrainingPlan(): TrainingPlanResponseObject? {
    if (this == TrainingPlan()) return null
    return TrainingPlanResponseObject(
        id = id.asString().takeIf { it.isNotBlank() },
        title = title.takeIf { it.isNotBlank() },
        clientCardId = clientCardId.asString().takeIf { it.isNotBlank() }
    )
}

private fun DashboardSummary.toTransportDashboardSummary(): DashboardSummaryResponseObject? {
    if (this == DashboardSummary()) return null
    return DashboardSummaryResponseObject(
        activeClientCards = activeClientCards.takeIf { it > 0 },
        archivedClientCards = archivedClientCards.takeIf { it > 0 },
        activeTrainingPlans = activeTrainingPlans.takeIf { it > 0 },
        activePublicLinks = activePublicLinks.takeIf { it > 0 }
    )
}

private fun PublicLinkInfo.toTransportPublicLink(): PublicLinkResponseObject? {
    if (this == PublicLinkInfo()) return null
    return PublicLinkResponseObject(
        publicUrl = publicUrl.takeIf { it.isNotBlank() },
        publicToken = publicToken.takeIf { it.isNotBlank() },
        expiresAt = expiresAt.takeIf { it != Instant.DISTANT_PAST }?.toString()
    )
}

private fun CompletionStatusInfo.toTransportCompletionStatus(): CompletionStatusResponseObject? {
    if (this == CompletionStatusInfo()) return null
    return CompletionStatusResponseObject(
        trainingPlanId = trainingPlanId.asString().takeIf { it.isNotBlank() }
    )
}

private fun PublicPlanViewInfo.toTransportPublicPlanView(): PublicPlanView? {
    if (this == PublicPlanViewInfo()) return null
    return PublicPlanView(
        trainerPublicName = trainerPublicName.takeIf { it.isNotBlank() },
        planTitle = planTitle.takeIf { it.isNotBlank() }
    )
}

private fun CompletionMarkInfo.toTransportCompletionMark(): PublicCompletionMarkResponseObject? {
    if (this == CompletionMarkInfo()) return null
    return PublicCompletionMarkResponseObject(
        itemRef = itemRef.takeIf { it.isNotBlank() },
        status = status.takeIf { it.isNotBlank() }?.let { CompletionStatus.valueOf(it) }
    )
}

private fun List<FBError>.toTransportErrors(): List<Error>? = this
    .map { it.toTransport() }
    .takeIf { it.isNotEmpty() }

private fun FBError.toTransport() = Error(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() }
)
