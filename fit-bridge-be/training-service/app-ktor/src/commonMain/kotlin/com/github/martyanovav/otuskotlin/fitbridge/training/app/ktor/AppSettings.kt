package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2Mapper
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionRepo
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.ClientCardProcessor
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.TrainingPlanProcessor
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardCorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanCorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSessionRepo
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoClientCardInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoTrainingPlanInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoClientCardStub
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoTrainingPlanStub
import kotlinx.serialization.json.Json

data class AppSettings(
    val json: Json = apiV2Mapper,
    val ccCorSettings: ClientCardCorSettings =
        ClientCardCorSettings(
            repoClientCardTest = RepoClientCardInMemory(),
            repoClientCardProd = RepoClientCardInMemory(),
            repoClientCardStub = RepoClientCardStub(),
        ),
    val tpCorSettings: TrainingPlanCorSettings =
        TrainingPlanCorSettings(
            repoTrainingPlanTest = RepoTrainingPlanInMemory(),
            repoTrainingPlanProd = RepoTrainingPlanInMemory(),
            repoTrainingPlanStub = RepoTrainingPlanStub(),
            repoClientCardTest = ccCorSettings.repoClientCardTest,
            repoClientCardProd = ccCorSettings.repoClientCardProd,
            repoClientCardStub = ccCorSettings.repoClientCardStub,
        ),
    val ccProcessor: ClientCardProcessor = ClientCardProcessor(ccCorSettings),
    val tpProcessor: TrainingPlanProcessor = TrainingPlanProcessor(tpCorSettings),
    val wsSessionsV1: IFBWsSessionRepo = KtorWsSessionRepo(),
    val wsSessionsV2: IFBWsSessionRepo = KtorWsSessionRepo(),
)
