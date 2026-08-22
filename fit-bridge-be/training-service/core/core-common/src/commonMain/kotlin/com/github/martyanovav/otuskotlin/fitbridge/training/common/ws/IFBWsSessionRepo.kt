package com.github.martyanovav.otuskotlin.fitbridge.training.common.ws

interface IFBWsSessionRepo {
    suspend fun add(session: IFBWsSession)

    suspend fun clearAll()

    suspend fun remove(session: IFBWsSession)

    suspend fun <T> sendAll(obj: T)

    companion object {
        val NONE =
            object : IFBWsSessionRepo {
                override suspend fun add(session: IFBWsSession) = Unit

                override suspend fun clearAll() = Unit

                override suspend fun remove(session: IFBWsSession) = Unit

                override suspend fun <T> sendAll(obj: T) = Unit
            }
    }
}
