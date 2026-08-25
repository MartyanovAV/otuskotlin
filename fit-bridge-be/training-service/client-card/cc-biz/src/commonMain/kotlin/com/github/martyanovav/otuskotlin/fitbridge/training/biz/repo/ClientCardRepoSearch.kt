package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseOk

fun ICorChainDsl<ClientCardContext>.clientCardRepoSearch(title: String) =
    worker {
        this.title = title
        description = "Поиск карточек клиентов в БД по фильтру"
        on { state == State.RUNNING }
        handle {
            val request =
                DbClientCardFilterRequest(
                    searchString = clientCardFilterValidated.searchString,
                    status = clientCardFilterValidated.status,
                    ownerUserId = principal.userId,
                    pageNumber = clientCardFilterValidated.pageNumber,
                    pageSize = clientCardFilterValidated.pageSize,
                )
            when (val result = clientCardRepo.searchClientCards(request)) {
                is DbClientCardsResponseOk -> {
                    clientCardsRepoDone = result.data.items.toMutableList()
                    clientCardsResponse =
                        clientCardsResponse.copy(
                            totalSize = result.data.totalSize,
                            pageNumber = result.data.pageNumber,
                            pageSize = result.data.pageSize,
                        )
                }
                is DbClientCardsResponseErr -> fail(result.errors)
            }
        }
    }
