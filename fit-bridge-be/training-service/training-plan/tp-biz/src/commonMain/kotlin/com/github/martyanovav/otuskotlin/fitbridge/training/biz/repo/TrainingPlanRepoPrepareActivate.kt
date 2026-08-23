package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus

fun ICorChainDsl<TrainingPlanContext>.trainingPlanRepoPrepareActivate(title: String) =
    worker {
        this.title = title
        description = "Подготовка данных тренировочного плана к активации в БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle
            if (ctx.trainingPlanRepoRead.status != TrainingPlanStatus.DRAFT) {
                ctx.fail(
                    FBError(
                        code = "invalid-status",
                        group = "business",
                        field = "status",
                        message = "Only a draft training plan can be activated",
                    ),
                )
                return@handle
            }
            ctx.trainingPlanRepoPrepare =
                ctx.trainingPlanRepoRead.deepCopy().apply {
                    lock = ctx.trainingPlanValidated.lock
                    status = TrainingPlanStatus.ACTIVE
                }
        }
    }
