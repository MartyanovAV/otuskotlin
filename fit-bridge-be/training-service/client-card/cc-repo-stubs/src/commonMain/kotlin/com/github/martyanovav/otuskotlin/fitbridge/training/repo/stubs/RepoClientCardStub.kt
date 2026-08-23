package com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbClientCardResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbClientCardsResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.stubs.ClientCardStub

class RepoClientCardStub : IRepoClientCard {
    override suspend fun createClientCard(rq: DbClientCardRequest): IDbClientCardResponse {
        return DbClientCardResponseOk(data = ClientCardStub.get())
    }

    override suspend fun readClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse {
        return DbClientCardResponseOk(data = ClientCardStub.get())
    }

    override suspend fun updateClientCard(rq: DbClientCardRequest): IDbClientCardResponse {
        return DbClientCardResponseOk(data = ClientCardStub.get())
    }

    override suspend fun archiveClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse {
        return DbClientCardResponseOk(data = ClientCardStub.get())
    }

    override suspend fun searchClientCards(rq: DbClientCardFilterRequest): IDbClientCardsResponse {
        return DbClientCardsResponseOk(
            data =
                Page(
                    items = ClientCardStub.getList(),
                    totalSize = ClientCardStub.getList().size,
                    pageNumber = rq.pageNumber,
                    pageSize = rq.pageSize,
                ),
        )
    }
}
