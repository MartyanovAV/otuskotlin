package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock

abstract class BaseInitTrainingPlans(private val op: String) : IInitObjects<TrainingPlan> {
    fun createInitTestModel(
        suf: String,
        clientCardId: ClientCardId = ClientCardId("cc-123"),
    ) = TrainingPlan(
        id = TrainingPlanId("tp-repo-$op-$suf"),
        clientCardId = clientCardId,
        ownerId = "owner-123",
        title = "$suf stub",
        lock = TrainingPlanLock("lock-$op-$suf"),
        planItems =
            listOf(
                ExerciseItem(
                    id = "exercise-$op-$suf",
                    title = "Test Exercise",
                    description = "Test exercise for $suf",
                    exerciseId = "ex-$suf",
                ),
            ),
    )
}
