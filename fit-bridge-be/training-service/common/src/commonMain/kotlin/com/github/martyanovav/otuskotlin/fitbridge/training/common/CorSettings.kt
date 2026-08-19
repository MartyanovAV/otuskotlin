package com.github.martyanovav.otuskotlin.fitbridge.training.common

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.FbLoggerProvider
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSessionRepo

data class CorSettings(
    val loggerProvider: FbLoggerProvider = FbLoggerProvider(),
    val wsSessionsV1: IFBWsSessionRepo = IFBWsSessionRepo.NONE,
    val wsSessionsV2: IFBWsSessionRepo = IFBWsSessionRepo.NONE,
    val repoClientCardTest: IRepoClientCard = IRepoClientCard.NONE,
    val repoClientCardProd: IRepoClientCard = IRepoClientCard.NONE,
    val repoClientCardStub: IRepoClientCard = IRepoClientCard.NONE,
    val repoTrainingPlanTest: IRepoTrainingPlan = IRepoTrainingPlan.NONE,
    val repoTrainingPlanProd: IRepoTrainingPlan = IRepoTrainingPlan.NONE,
    val repoTrainingPlanStub: IRepoTrainingPlan = IRepoTrainingPlan.NONE,
)
