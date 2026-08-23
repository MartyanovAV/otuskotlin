package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode

fun ICorChainDsl<ClientCardContext>.prepareRepoResult(title: String) =
    worker {
        this.title = title
        description = "Подготовка результата в репозитории"
        on { workMode != WorkMode.STUB }
        handle {
            clientCardResponse = clientCardRepoDone
            clientCardsResponse = clientCardsResponse.copy(items = clientCardsRepoDone)
            state =
                when (state) {
                    State.RUNNING -> State.FINISHING
                    else -> state
                }
        }
    }
