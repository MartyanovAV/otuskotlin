package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId

const val ERROR_GROUP_REPO = "repo"

fun errorNotFoundClientCard(id: ClientCardId) =
    DbClientCardResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-not-found",
            group = ERROR_GROUP_REPO,
            field = "id",
            message = "ClientCard with ID: ${id.asString()} is not Found",
        ),
    )

fun errorNotFoundTrainingPlan(id: TrainingPlanId) =
    DbTrainingPlanResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-not-found",
            group = ERROR_GROUP_REPO,
            field = "id",
            message = "TrainingPlan with ID: ${id.asString()} is not Found",
        ),
    )

val errorEmptyClientCardId =
    DbClientCardResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-empty-id",
            group = ERROR_GROUP_REPO,
            field = "id",
            message = "ClientCard Id must not be null or blank",
        ),
    )

val errorEmptyTrainingPlanId =
    DbTrainingPlanResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-empty-id",
            group = ERROR_GROUP_REPO,
            field = "id",
            message = "TrainingPlan Id must not be null or blank",
        ),
    )
