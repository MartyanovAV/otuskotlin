package com.github.martyanovav.otuskotlin.fitbridge.training.common.ws

interface IFBWsSession {
    suspend fun <T> send(obj: T)

    companion object {
        val NONE =
            object : IFBWsSession {
                override suspend fun <T> send(obj: T) = Unit
            }
    }
}
