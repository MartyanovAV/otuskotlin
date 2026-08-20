package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2ResponseSerialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ws.IFBWsSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send

data class KtorWsSessionV2(
    private val session: WebSocketSession,
    val principal: AuthPrincipal,
) : IFBWsSession {
    override suspend fun <T> send(obj: T) {
        require(obj is IResponse) { "V2 WebSocket session accepts only v2 responses" }
        session.send(Frame.Text(apiV2ResponseSerialize(obj)))
    }
}
