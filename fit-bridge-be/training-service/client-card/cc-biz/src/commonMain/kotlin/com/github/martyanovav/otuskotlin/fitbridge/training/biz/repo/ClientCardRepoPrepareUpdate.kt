package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

fun ICorChainDsl<ClientCardContext>.clientCardRepoPrepareUpdate(title: String) =
    worker {
        this.title = title
        description = "Подготовка данных карточки клиента к обновлению в БД"
        on { state == State.RUNNING }
        handle {
            clientCardRepoPrepare =
                clientCardRepoRead.deepCopy().apply {
                    displayName = clientCardValidated.displayName
                    note = clientCardValidated.note
                    lock = clientCardValidated.lock
                }
        }
    }
