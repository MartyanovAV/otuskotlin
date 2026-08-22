package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan

sealed interface IDbTrainingPlanResponse : IDbResponse<TrainingPlan>

data class DbTrainingPlanResponseOk(
    val data: TrainingPlan,
) : IDbTrainingPlanResponse

data class DbTrainingPlanResponseErr(
    val errors: List<FBError> = emptyList(),
) : IDbTrainingPlanResponse {
    constructor(err: FBError) : this(listOf(err))
}

data class DbTrainingPlanResponseErrWithData(
    val data: TrainingPlan,
    val errors: List<FBError> = emptyList(),
) : IDbTrainingPlanResponse {
    constructor(plan: TrainingPlan, err: FBError) : this(plan, listOf(err))
}
