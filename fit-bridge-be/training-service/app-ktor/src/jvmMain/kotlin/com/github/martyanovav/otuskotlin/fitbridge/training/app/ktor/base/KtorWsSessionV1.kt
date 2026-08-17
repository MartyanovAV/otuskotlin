package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1ResponseSerialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send

data class KtorWsSessionV1(
    private val session: WebSocketSession,
) : IFBWsSession {
    override suspend fun <T> send(obj: T) {
        require(obj is IResponse) { "V1 WebSocket session accepts only v1 responses" }
        session.send(Frame.Text(apiV1ResponseSerialize(obj)))
    }
}
