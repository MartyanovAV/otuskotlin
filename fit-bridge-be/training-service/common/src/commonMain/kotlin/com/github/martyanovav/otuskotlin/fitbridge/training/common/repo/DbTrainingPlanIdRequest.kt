package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock

data class DbTrainingPlanIdRequest(
    val id: TrainingPlanId,
    val lock: TrainingPlanLock = TrainingPlanLock.NONE,
) {
    @Deprecated("Use constructor with explicit lock to avoid empty lock errors")
    constructor(id: TrainingPlanId) : this(id, TrainingPlanLock.NONE)

    constructor(plan: TrainingPlan) : this(plan.id, plan.lock)
}
