package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand

fun ICorChainDsl<TrainingPlanContext>.initTrainingPlanUserIds(title: String) =
    worker {
        this.title = title
        description = "Инициализация владельцев"
        on { state == State.RUNNING && command == TrainingPlanCommand.CREATE }
        handle {
            trainingPlanValidated.ownerUserId = principal.userId
            trainingPlanValidated.createdByUserId = principal.userId
        }
    }
