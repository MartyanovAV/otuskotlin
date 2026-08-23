package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode

fun ICorChainDsl<TrainingPlanContext>.prepareRepoResult(title: String) =
    worker {
        this.title = title
        description = "Подготовка результата в репозитории"
        on { workMode != WorkMode.STUB }
        handle {
            trainingPlanResponse = trainingPlanRepoDone
            trainingPlansResponse = trainingPlansResponse.copy(items = trainingPlansRepoDone)
            state =
                when (state) {
                    State.RUNNING -> State.FINISHING
                    else -> state
                }
        }
    }
