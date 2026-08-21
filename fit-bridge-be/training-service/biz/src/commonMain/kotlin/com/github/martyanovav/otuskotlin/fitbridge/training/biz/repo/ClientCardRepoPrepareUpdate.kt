package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

fun ICorChainDsl<IFBContext>.clientCardRepoPrepareUpdate(title: String) =
    worker {
        this.title = title
        description = "Подготовка данных карточки клиента к обновлению в БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle as ClientCardContext
            ctx.clientCardRepoPrepare =
                ctx.clientCardRepoRead.deepCopy().apply {
                    displayName = ctx.clientCardValidated.displayName
                    note = ctx.clientCardValidated.note
                    lock = ctx.clientCardValidated.lock
                }
        }
    }
