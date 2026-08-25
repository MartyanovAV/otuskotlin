package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk

fun ICorChainDsl<ClientCardContext>.clientCardRepoUpdate(title: String) =
    worker {
        this.title = title
        description = "Обновление карточки клиента в БД"
        on { state == State.RUNNING }
        handle {
            val request = DbClientCardRequest(clientCardRepoPrepare)
            when (val result = clientCardRepo.updateClientCard(request)) {
                is DbClientCardResponseOk -> clientCardRepoDone = result.data
                is DbClientCardResponseErr -> fail(result.errors)
                is DbClientCardResponseErrWithData -> {
                    fail(result.errors)
                    clientCardRepoDone = result.data
                }
            }
        }
    }
