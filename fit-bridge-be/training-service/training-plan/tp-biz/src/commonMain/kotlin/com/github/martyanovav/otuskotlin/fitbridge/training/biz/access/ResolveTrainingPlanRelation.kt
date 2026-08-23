package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.permissions.FtcPrincipalRelation

fun ICorChainDsl<TrainingPlanContext>.resolveTrainingPlanRelation(title: String) =
    worker {
        this.title = title
        description = "Вычисление отношения пользователя к плану тренировок"
        on { principal.userId.isNotBlank() }
        handle {
            val ctx = this@handle
            ctx.principalRelation =
                when {
                    // For CREATE, we will trust repo checks or validate clientCard separately
                    ctx.command == TrainingPlanCommand.CREATE -> FtcPrincipalRelation.NEW
                    ctx.trainingPlanRepoRead.ownerUserId == ctx.principal.userId -> FtcPrincipalRelation.OWNER
                    else -> FtcPrincipalRelation.NONE
                }
        }
    }
