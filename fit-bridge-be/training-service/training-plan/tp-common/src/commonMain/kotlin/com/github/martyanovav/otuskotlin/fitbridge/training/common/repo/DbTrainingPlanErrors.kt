package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock

fun errorNotFoundTrainingPlan(id: TrainingPlanId) =
    DbTrainingPlanResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-not-found",
            group = ERROR_GROUP_REPO,
            field = "id",
            message = "TrainingPlan with ID: ${id.asString()} is not Found",
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

fun errorEmptyTrainingPlanLock(id: TrainingPlanId) =
    DbTrainingPlanResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-empty-lock",
            group = ERROR_GROUP_REPO,
            field = "lock",
            message = "TrainingPlan lock is empty for id: ${id.asString()}",
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

fun errorInvalidTrainingPlanStatus(plan: TrainingPlan) =
    DbTrainingPlanResponseErrWithData(
        plan = plan,
        err =
            FBError(
                code = "$ERROR_GROUP_REPO-invalid-status",
                group = ERROR_GROUP_REPO,
                field = "status",
                message = "TrainingPlan with ID: ${plan.id.asString()} must be ACTIVE",
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
