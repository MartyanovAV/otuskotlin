package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbClientCardResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbClientCardsResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard

class RepoClientCardMock(
    private val invokeCreateClientCard: (DbClientCardRequest) -> IDbClientCardResponse = { DEFAULT_CC_SUCCESS_EMPTY_MOCK },
    private val invokeReadClientCard: (DbClientCardIdRequest) -> IDbClientCardResponse = { DEFAULT_CC_SUCCESS_EMPTY_MOCK },
    private val invokeUpdateClientCard: (DbClientCardRequest) -> IDbClientCardResponse = { DEFAULT_CC_SUCCESS_EMPTY_MOCK },
    private val invokeArchiveClientCard: (DbClientCardIdRequest) -> IDbClientCardResponse = { DEFAULT_CC_SUCCESS_EMPTY_MOCK },
    private val invokeSearchClientCards: (DbClientCardFilterRequest) -> IDbClientCardsResponse = { DEFAULT_CCS_SUCCESS_EMPTY_MOCK },
) : IRepoClientCard {
    override suspend fun createClientCard(rq: DbClientCardRequest): IDbClientCardResponse {
        return invokeCreateClientCard(rq)
    }

    override suspend fun readClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse {
        return invokeReadClientCard(rq)
    }

    override suspend fun updateClientCard(rq: DbClientCardRequest): IDbClientCardResponse {
        return invokeUpdateClientCard(rq)
    }

    override suspend fun archiveClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse {
        return invokeArchiveClientCard(rq)
    }

    override suspend fun searchClientCards(rq: DbClientCardFilterRequest): IDbClientCardsResponse {
        return invokeSearchClientCards(rq)
    }

    companion object {
        val DEFAULT_CC_SUCCESS_EMPTY_MOCK = DbClientCardResponseOk(ClientCard())
        val DEFAULT_CCS_SUCCESS_EMPTY_MOCK = DbClientCardsResponseOk(Page(emptyList()))
    }
}
