package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk

fun ICorChainDsl<TrainingPlanContext>.trainingPlanRepoRead(title: String) =
    worker {
        this.title = title
        description = "Чтение тренировочного плана из БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle
            val request = DbTrainingPlanIdRequest(ctx.trainingPlanValidated)
            when (val result = ctx.trainingPlanRepo.readTrainingPlan(request)) {
                is DbTrainingPlanResponseOk -> ctx.trainingPlanRepoRead = result.data
                is DbTrainingPlanResponseErr -> fail(result.errors)
                is DbTrainingPlanResponseErrWithData -> {
                    fail(result.errors)
                    ctx.trainingPlanRepoRead = result.data
                }
            }
        }
    }
