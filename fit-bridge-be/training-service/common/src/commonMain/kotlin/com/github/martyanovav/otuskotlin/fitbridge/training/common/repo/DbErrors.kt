package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock

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

fun errorEmptyClientCardLock(id: ClientCardId) =
    DbClientCardResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-empty-lock",
            group = ERROR_GROUP_REPO,
            field = "lock",
            message = "ClientCard lock is empty for id: ${id.asString()}",
        ),
    )

fun errorEmptyTrainingPlanLock(id: TrainingPlanId) =
    DbTrainingPlanResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-empty-lock",
            group = ERROR_GROUP_REPO,
            field = "lock",
            message = "TrainingPlan lock is empty for id: ${id.asString()}",
        ),
    )

fun errorRepoConcurrencyClientCard(card: ClientCard, oldLock: ClientCardLock) =
    DbClientCardResponseErrWithData(
        card = card,
        err =
            FBError(
                code = "$ERROR_GROUP_REPO-concurrency",
                group = ERROR_GROUP_REPO,
                field = "lock",
                message = "ClientCard concurrent modification detected for id: ${card.id.asString()}",
            ),
    )

fun errorRepoConcurrencyTrainingPlan(plan: TrainingPlan, oldLock: TrainingPlanLock) =
    DbTrainingPlanResponseErrWithData(
        plan = plan,
        err =
            FBError(
                code = "$ERROR_GROUP_REPO-concurrency",
                group = ERROR_GROUP_REPO,
                field = "lock",
                message = "TrainingPlan concurrent modification detected for id: ${plan.id.asString()}",
            ),
    )

fun errorRepoDbClientCard(e: Throwable) =
    DbClientCardResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-db-error",
            group = ERROR_GROUP_REPO,
            field = "db",
            message = "Database error occurred",
            exception = e,
        ),
    )

fun errorRepoDbTrainingPlan(e: Throwable) =
    DbTrainingPlanResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-db-error",
            group = ERROR_GROUP_REPO,
            field = "db",
            message = "Database error occurred",
            exception = e,
        ),
    )
