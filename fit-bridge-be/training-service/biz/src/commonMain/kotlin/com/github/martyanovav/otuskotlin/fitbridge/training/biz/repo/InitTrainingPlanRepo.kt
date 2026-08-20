package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.exceptions.FbDbNotConfiguredException
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.errorSystem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan

fun ICorChainDsl<IFBContext>.initTrainingPlanRepo(title: String) =
    worker {
        this.title = title
        description = "Вычисление рабочего репозитория для тренировочных планов"
        handle {
            val ctx = this@handle as TrainingPlanContext
            ctx.trainingPlanRepo =
                when {
                    ctx.workMode == WorkMode.TEST -> ctx.corSettings.repoTrainingPlanTest
                    ctx.workMode == WorkMode.STUB -> ctx.corSettings.repoTrainingPlanStub
                    else -> ctx.corSettings.repoTrainingPlanProd
                }
            ctx.clientCardRepo =
                when {
                    ctx.workMode == WorkMode.TEST -> ctx.corSettings.repoClientCardTest
                    ctx.workMode == WorkMode.STUB -> ctx.corSettings.repoClientCardStub
                    else -> ctx.corSettings.repoClientCardProd
                }
            if (ctx.workMode != WorkMode.STUB &&
                (ctx.trainingPlanRepo == IRepoTrainingPlan.NONE || ctx.clientCardRepo == IRepoClientCard.NONE)
            ) {
                fail(
                    errorSystem(
                        violationCode = "dbNotConfigured",
                        e = FbDbNotConfiguredException(ctx.workMode),
                    ),
                )
            }
        }
    }
