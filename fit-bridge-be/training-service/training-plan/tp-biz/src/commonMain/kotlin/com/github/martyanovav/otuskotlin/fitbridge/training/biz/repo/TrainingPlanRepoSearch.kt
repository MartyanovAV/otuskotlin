package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseOk

fun ICorChainDsl<TrainingPlanContext>.trainingPlanRepoSearch(title: String) =
    worker {
        this.title = title
        description = "Поиск тренировочных планов в БД по фильтру"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle
            val request =
                DbTrainingPlanFilterRequest(
                    clientCardId = ctx.trainingPlanFilterValidated.clientCardId,
                    searchString = ctx.trainingPlanFilterValidated.searchString,
                    status = ctx.trainingPlanFilterValidated.status,
                    ownerUserId = ctx.principal.userId,
                    pageNumber = ctx.trainingPlanFilterValidated.pageNumber,
                    pageSize = ctx.trainingPlanFilterValidated.pageSize,
                )
            when (val result = ctx.trainingPlanRepo.searchTrainingPlans(request)) {
                is DbTrainingPlansResponseOk -> {
                    ctx.trainingPlansRepoDone = result.data.items.toMutableList()
                    ctx.trainingPlansResponse =
                        ctx.trainingPlansResponse.copy(
                            totalSize = result.data.totalSize,
                            pageNumber = result.data.pageNumber,
                            pageSize = result.data.pageSize,
                        )
                }
                is DbTrainingPlansResponseErr -> fail(result.errors)
            }
        }
    }
