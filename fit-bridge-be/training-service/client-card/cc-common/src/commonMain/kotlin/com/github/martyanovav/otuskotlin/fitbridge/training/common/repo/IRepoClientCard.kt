package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

interface IRepoClientCard {
    suspend fun createClientCard(rq: DbClientCardRequest): IDbClientCardResponse

    suspend fun readClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse

    suspend fun updateClientCard(rq: DbClientCardRequest): IDbClientCardResponse

    suspend fun archiveClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse

    suspend fun searchClientCards(rq: DbClientCardFilterRequest): IDbClientCardsResponse

    companion object {
        val NONE =
            object : IRepoClientCard {
                override suspend fun createClientCard(rq: DbClientCardRequest): IDbClientCardResponse {
                    throw NotImplementedError("Must not be used")
                }

                override suspend fun readClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse {
                    throw NotImplementedError("Must not be used")
                }

                override suspend fun updateClientCard(rq: DbClientCardRequest): IDbClientCardResponse {
                    throw NotImplementedError("Must not be used")
                }

                override suspend fun archiveClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse {
                    throw NotImplementedError("Must not be used")
                }

                override suspend fun searchClientCards(rq: DbClientCardFilterRequest): IDbClientCardsResponse {
                    throw NotImplementedError("Must not be used")
                }
            }
    }
}
