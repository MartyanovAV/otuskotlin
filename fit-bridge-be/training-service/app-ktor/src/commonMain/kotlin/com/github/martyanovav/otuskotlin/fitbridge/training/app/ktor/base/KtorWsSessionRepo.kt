package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base

import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSession
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSessionRepo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class KtorWsSessionRepo : IFBWsSessionRepo {
    private val mutex = Mutex()
    private val sessions = mutableSetOf<IFBWsSession>()

    override suspend fun add(session: IFBWsSession) {
        mutex.withLock { sessions.add(session) }
    }

    override suspend fun clearAll() {
        mutex.withLock { sessions.clear() }
    }

    override suspend fun remove(session: IFBWsSession) {
        mutex.withLock { sessions.remove(session) }
    }

    override suspend fun <T> sendAll(obj: T) {
        val snapshot = mutex.withLock { sessions.toList() }
        snapshot.forEach { it.send(obj) }
    }
}
