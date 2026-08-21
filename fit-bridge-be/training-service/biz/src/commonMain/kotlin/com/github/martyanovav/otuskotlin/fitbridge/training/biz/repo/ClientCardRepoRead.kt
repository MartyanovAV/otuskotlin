package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk

fun ICorChainDsl<IFBContext>.clientCardRepoRead(title: String) =
    worker {
        this.title = title
        description = "Чтение карточки клиента из БД"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle as ClientCardContext
            val request = DbClientCardIdRequest(ctx.clientCardValidated)
            when (val result = ctx.clientCardRepo.readClientCard(request)) {
                is DbClientCardResponseOk -> ctx.clientCardRepoRead = result.data
                is DbClientCardResponseErr -> fail(result.errors)
                is DbClientCardResponseErrWithData -> {
                    fail(result.errors)
                    ctx.clientCardRepoRead = result.data
                }
            }
        }
    }
