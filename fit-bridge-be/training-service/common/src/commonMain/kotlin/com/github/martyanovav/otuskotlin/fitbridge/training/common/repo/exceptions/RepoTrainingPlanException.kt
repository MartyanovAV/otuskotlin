package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.exceptions

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId

open class RepoTrainingPlanException(
    @Suppress("unused")
    val trainingPlanId: TrainingPlanId,
    msg: String,
) : RepoException(msg)
