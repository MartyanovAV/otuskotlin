package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk

fun ICorChainDsl<TrainingPlanContext>.trainingPlanRepoComplete(title: String) =
    worker {
        this.title = title
        description = "Завершение тренировочного плана в БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle as TrainingPlanContext
            val request = DbTrainingPlanRequest(ctx.trainingPlanRepoPrepare)
            when (val result = ctx.trainingPlanRepo.completeTrainingPlan(request)) {
                is DbTrainingPlanResponseOk -> ctx.trainingPlanRepoDone = result.data
                is DbTrainingPlanResponseErr -> fail(result.errors)
                is DbTrainingPlanResponseErrWithData -> {
                    fail(result.errors)
                    ctx.trainingPlanRepoDone = result.data
                }
            }
        }
    }
