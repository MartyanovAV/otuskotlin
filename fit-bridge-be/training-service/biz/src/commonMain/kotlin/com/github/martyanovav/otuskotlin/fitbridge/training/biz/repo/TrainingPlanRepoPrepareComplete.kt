package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus

fun ICorChainDsl<IFBContext>.trainingPlanRepoPrepareComplete(title: String) =
    worker {
        this.title = title
        description = "Подготовка данных тренировочного плана к завершению в БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle as TrainingPlanContext
            if (ctx.trainingPlanRepoRead.status != TrainingPlanStatus.ACTIVE) {
                ctx.fail(
                    FBError(
                        code = "invalid-status",
                        group = "business",
                        field = "status",
                        message = "Only an active training plan can be completed",
                    ),
                )
                return@handle
            }
            ctx.trainingPlanRepoPrepare =
                ctx.trainingPlanRepoRead.deepCopy().apply {
                    lock = ctx.trainingPlanValidated.lock
                    completedAt = ctx.trainingPlanValidated.completedAt
                    difficulty = ctx.trainingPlanValidated.difficulty
                    coachComment = ctx.trainingPlanValidated.coachComment
                }
        }
    }
