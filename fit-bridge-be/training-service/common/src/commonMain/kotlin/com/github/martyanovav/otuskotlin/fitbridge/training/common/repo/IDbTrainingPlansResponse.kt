package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan

sealed interface IDbTrainingPlansResponse : IDbResponse<Page<TrainingPlan>>

data class DbTrainingPlansResponseOk(
    val data: Page<TrainingPlan>,
) : IDbTrainingPlansResponse

@Suppress("unused")
data class DbTrainingPlansResponseErr(
    val errors: List<FBError> = emptyList(),
) : IDbTrainingPlansResponse {
    constructor(err: FBError) : this(listOf(err))
}
