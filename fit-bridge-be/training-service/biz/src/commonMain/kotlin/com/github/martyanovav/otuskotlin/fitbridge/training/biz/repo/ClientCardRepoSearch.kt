package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseOk

fun ICorChainDsl<IFBContext>.clientCardRepoSearch(title: String) =
    worker {
        this.title = title
        description = "Поиск карточек клиентов в БД по фильтру"
        on { state == State.RUNNING }
        handle {
            val ctx = this@handle as ClientCardContext
            val request =
                DbClientCardFilterRequest(
                    searchString = ctx.clientCardFilterValidated.searchString,
                    status = ctx.clientCardFilterValidated.status,
                    ownerUserId = ctx.principal.userId,
                    pageNumber = ctx.clientCardFilterValidated.pageNumber,
                    pageSize = ctx.clientCardFilterValidated.pageSize,
                )
            when (val result = ctx.clientCardRepo.searchClientCards(request)) {
                is DbClientCardsResponseOk -> ctx.clientCardsRepoDone = result.data.toMutableList()
                is DbClientCardsResponseErr -> fail(result.errors)
            }
        }
    }
