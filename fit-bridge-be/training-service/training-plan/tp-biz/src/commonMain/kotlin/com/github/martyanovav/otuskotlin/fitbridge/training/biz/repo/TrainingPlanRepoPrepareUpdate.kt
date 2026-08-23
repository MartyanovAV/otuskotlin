package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus

fun ICorChainDsl<TrainingPlanContext>.trainingPlanRepoPrepareUpdate(title: String) =
    worker {
        this.title = title
        description = "Подготовка данных тренировочного плана к обновлению в БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle
            if (ctx.trainingPlanRepoRead.status != TrainingPlanStatus.ACTIVE) {
                ctx.fail(
                    FBError(
                        code = "invalid-status",
                        group = "business",
                        field = "status",
                        message = "Completed or archived training plan cannot be updated",
                    ),
                )
                return@handle
            }
            ctx.trainingPlanRepoPrepare =
                ctx.trainingPlanRepoRead.deepCopy().apply {
                    this.title = ctx.trainingPlanValidated.title
                    planItems = ctx.trainingPlanValidated.planItems
                    version = ctx.trainingPlanValidated.version
                    lock = ctx.trainingPlanValidated.lock
                }
        }
    }
