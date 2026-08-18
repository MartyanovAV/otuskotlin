package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId

data class DbTrainingPlanIdRequest(
    val id: TrainingPlanId,
) {
    constructor(plan: TrainingPlan) : this(plan.id)
}
