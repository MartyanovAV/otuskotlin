package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

fun ICorChainDsl<IFBContext>.trainingPlanRepoPrepareUpdate(title: String) =
    worker {
        this.title = title
        description = "Подготовка данных тренировочного плана к обновлению в БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle as TrainingPlanContext
            ctx.trainingPlanRepoPrepare =
                ctx.trainingPlanRepoRead.deepCopy().apply {
                    this.title = ctx.trainingPlanValidated.title
                    planItems = ctx.trainingPlanValidated.planItems
                    version = ctx.trainingPlanValidated.version
                    lock = ctx.trainingPlanValidated.lock
                }
        }
    }
