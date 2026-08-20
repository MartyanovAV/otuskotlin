package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand

fun ICorChainDsl<IFBContext>.initClientCardUserIds(title: String) =
    worker {
        this.title = title
        description = "Инициализация владельца и автора ClientCard"
        on { state == State.RUNNING && command == ClientCardCommand.CREATE }
        handle {
            val ctx = this@handle as ClientCardContext
            ctx.clientCardValidated.ownerUserId = ctx.principal.userId
            ctx.clientCardValidated.createdByUserId = ctx.principal.userId
        }
    }

fun ICorChainDsl<IFBContext>.initTrainingPlanUserIds(title: String) =
    worker {
        this.title = title
        description = "Инициализация владельца и автора TrainingPlan"
        on { state == State.RUNNING && command == TrainingPlanCommand.CREATE }
        handle {
            val ctx = this@handle as TrainingPlanContext
            ctx.trainingPlanValidated.ownerUserId = ctx.principal.userId
            ctx.trainingPlanValidated.createdByUserId = ctx.principal.userId
        }
    }
