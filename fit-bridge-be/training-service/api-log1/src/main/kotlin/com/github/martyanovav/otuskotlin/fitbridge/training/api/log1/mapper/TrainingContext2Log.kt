package com.github.martyanovav.otuskotlin.fitbridge.training.api.log1.mapper

import com.github.martyanovav.otuskotlin.fitbridge.api.log1.models.*
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.*
import java.time.Instant

fun TrainingPlanContext.toLog(logId: String) = CommonLogModel(
    messageTime = Instant.now().toString(),
    logId = logId,
    source = "fit-bridge-training",
    training = toTrainingLog(),
    errors = errors.map { it.toLog() },
)

fun ClientCardContext.toLog(logId: String) = CommonLogModel(
    messageTime = Instant.now().toString(),
    logId = logId,
    source = "fit-bridge-training",
    training = toTrainingLog(),
    errors = errors.map { it.toLog() },
)

private fun TrainingPlanContext.toTrainingLog(): TrainingLogModel? {
    val planNone = TrainingPlan()
    return TrainingLogModel(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        operation = command.takeIf { it != TrainingPlanCommand.NONE }?.let { TrainingLogOperation.valueOf((it as Enum<*>).name) },
        requestTrainingPlan = trainingPlanRequest.takeIf { it != planNone }?.toLog(),
        responseTrainingPlan = trainingPlanResponse.takeIf { it != planNone }?.toLog(),
        responseTrainingPlans = trainingPlansResponse.items.takeIf { it.isNotEmpty() }?.filter { it != planNone }?.map { it.toLog() },
        requestFilter = trainingPlanFilter.takeIf { it != TrainingPlanFilter() }?.toLog(),
    ).takeIf { it != TrainingLogModel() }
}

private fun ClientCardContext.toTrainingLog(): TrainingLogModel? {
    val cardNone = ClientCard()
    return TrainingLogModel(
        requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
        operation = command.takeIf { it != ClientCardCommand.NONE }?.let { TrainingLogOperation.valueOf((it as Enum<*>).name) },
        requestClientCard = clientCardRequest.takeIf { it != cardNone }?.toLog(),
        responseClientCard = clientCardResponse.takeIf { it != cardNone }?.toLog(),
        responseClientCards = clientCardsResponse.items.takeIf { it.isNotEmpty() }?.filter { it != cardNone }?.map { it.toLog() },
        requestFilter = clientCardFilter.takeIf { it != ClientCardFilter() }?.toLog(),
    ).takeIf { it != TrainingLogModel() }
}

private fun TrainingPlanFilter.toLog() = TrainingFilterLog(
    searchString = searchString.takeIf { it.isNotBlank() },
    clientId = null,
    trainerId = null,
    cardId = null
)

private fun ClientCardFilter.toLog() = TrainingFilterLog(
    searchString = searchString.takeIf { it.isNotBlank() },
    clientId = null,
    trainerId = null,
    cardId = null
)

private fun FBError.toLog() = ErrorLogModel(
    message = message.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    code = code.takeIf { it.isNotBlank() },
    level = level.name,
)

private fun TrainingPlan.toLog() = TrainingPlanLog(
    id = id.takeIf { it != TrainingPlanId.NONE }?.asString(),
    cardId = clientCardId.takeIf { it != ClientCardId.NONE }?.asString(),
    title = title.takeIf { it.isNotBlank() }
)

private fun ClientCard.toLog() = ClientCardLog(
    id = id.takeIf { it != ClientCardId.NONE }?.asString(),
    trainerId = trainerId.takeIf { it.isNotBlank() },
    goals = note.takeIf { it.isNotBlank() },
)
