package com.github.martyanovav.otuskotlin.fitbridge.training.common

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.FbLoggerProvider
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan

data class TrainingPlanCorSettings(
    val loggerProvider: FbLoggerProvider = FbLoggerProvider(),
    val repoTrainingPlanStub: IRepoTrainingPlan = IRepoTrainingPlan.NONE,
    val repoTrainingPlanTest: IRepoTrainingPlan = IRepoTrainingPlan.NONE,
    val repoTrainingPlanProd: IRepoTrainingPlan = IRepoTrainingPlan.NONE,
    val repoClientCardStub: IRepoClientCard = IRepoClientCard.NONE,
    val repoClientCardTest: IRepoClientCard = IRepoClientCard.NONE,
    val repoClientCardProd: IRepoClientCard = IRepoClientCard.NONE,
)
