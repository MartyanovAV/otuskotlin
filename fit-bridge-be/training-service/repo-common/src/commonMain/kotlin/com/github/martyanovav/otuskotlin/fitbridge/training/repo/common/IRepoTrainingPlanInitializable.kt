package com.github.martyanovav.otuskotlin.fitbridge.training.repo.common

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan

interface IRepoTrainingPlanInitializable : IRepoTrainingPlan {
    fun save(plans: Collection<TrainingPlan>): Collection<TrainingPlan>
}
