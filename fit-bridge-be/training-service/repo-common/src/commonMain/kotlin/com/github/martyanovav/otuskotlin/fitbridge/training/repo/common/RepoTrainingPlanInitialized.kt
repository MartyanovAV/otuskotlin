package com.github.martyanovav.otuskotlin.fitbridge.training.repo.common

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan

class RepoTrainingPlanInitialized(
    private val repo: IRepoTrainingPlanInitializable,
    initObjects: Collection<TrainingPlan> = emptyList(),
) : IRepoTrainingPlanInitializable by repo {
    @Suppress("unused")
    val initializedObjects: List<TrainingPlan> = save(initObjects).toList()
}
