package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode

fun ICorChainDsl<IFBContext>.prepareRepoResult(title: String) =
    worker {
        this.title = title
        description = "Подготовка данных для ответа клиенту на запрос"
        on { workMode != WorkMode.STUB }
        handle {
            when (val ctx = this@handle) {
                is ClientCardContext -> {
                    ctx.clientCardResponse = ctx.clientCardRepoDone
                    ctx.clientCardsResponse = ctx.clientCardsResponse.copy(items = ctx.clientCardsRepoDone)
                    ctx.state =
                        when (ctx.state) {
                            State.RUNNING -> State.FINISHING
                            else -> ctx.state
                        }
                }
                is TrainingPlanContext -> {
                    ctx.trainingPlanResponse = ctx.trainingPlanRepoDone
                    ctx.trainingPlansResponse = ctx.trainingPlansResponse.copy(items = ctx.trainingPlansRepoDone)
                    ctx.state =
                        when (ctx.state) {
                            State.RUNNING -> State.FINISHING
                            else -> ctx.state
                        }
                }
            }
        }
    }
