package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId

abstract class BaseInitTrainingPlans(private val op: String) : IInitObjects<TrainingPlan> {
    fun createInitTestModel(
        suf: String,
        clientCardId: ClientCardId = ClientCardId("cc-repo-$op-owner"),
    ) = TrainingPlan(
        id = TrainingPlanId("tp-repo-$op-$suf"),
        clientCardId = clientCardId,
        ownerId = "owner-123",
        title = "$suf stub",
    )
}
