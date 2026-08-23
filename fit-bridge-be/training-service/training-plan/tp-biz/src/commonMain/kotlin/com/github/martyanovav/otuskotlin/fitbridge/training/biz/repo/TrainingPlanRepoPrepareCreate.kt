package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

fun ICorChainDsl<TrainingPlanContext>.trainingPlanRepoPrepareCreate(title: String) =
    worker {
        this.title = title
        description = "Подготовка тренировочного плана к сохранению в БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle as TrainingPlanContext
            ctx.trainingPlanRepoPrepare = ctx.trainingPlanValidated.deepCopy()
        }
    }
