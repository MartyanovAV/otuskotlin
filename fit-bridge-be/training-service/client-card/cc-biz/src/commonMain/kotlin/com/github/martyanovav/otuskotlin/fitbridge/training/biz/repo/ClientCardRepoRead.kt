package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk

fun ICorChainDsl<ClientCardContext>.clientCardRepoRead(title: String) =
    worker {
        this.title = title
        description = "Чтение карточки клиента из БД"
        on { state == State.RUNNING }
        handle {
            val request = DbClientCardIdRequest(clientCardValidated)
            when (val result = clientCardRepo.readClientCard(request)) {
                is DbClientCardResponseOk -> clientCardRepoRead = result.data
                is DbClientCardResponseErr -> fail(result.errors)
                is DbClientCardResponseErrWithData -> {
                    fail(result.errors)
                    clientCardRepoRead = result.data
                }
            }
        }
    }
