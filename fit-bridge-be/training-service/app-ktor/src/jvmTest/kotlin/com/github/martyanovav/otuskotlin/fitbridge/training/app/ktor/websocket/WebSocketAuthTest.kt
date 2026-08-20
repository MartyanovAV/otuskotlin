package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.websocket

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.AUTH_HEADER
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.moduleJvm
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class WebSocketAuthTest {
    @Test
    fun v1WebSocketPropagatesPrincipal() = assertWebSocketPrincipal("v1")

    @Test
    fun v2WebSocketPropagatesPrincipal() = assertWebSocketPrincipal("v2")

    private fun assertWebSocketPrincipal(version: String) =
        testApplication {
            application { moduleJvm(AppSettings()) }
            val client = createClient { install(WebSockets) }

            client.webSocket(
                urlString = "/$version/ws",
                request = { header(AUTH_HEADER, principalHeader("user-$version")) },
            ) {
                withTimeout(3.seconds) { incoming.receive() }
                val apiVersion = if (version == "v2") "\"apiVersion\":\"v2\"," else ""
                val request =
                    """
                    {
                      "requestType": "clientCard.create",
                      $apiVersion
                      "requestId": "ws-auth-$version",
                      "clientCard": {"displayName": "WS Auth $version"}
                    }
                    """.trimIndent()

                send(Frame.Text(request))

                val response = withTimeout(3.seconds) { (incoming.receive() as Frame.Text).readText() }
                assertEquals("success", resultOf(response))
            }
        }

    @OptIn(ExperimentalEncodingApi::class)
    private fun principalHeader(userId: String): String {
        val payload = """{"sub":"$userId","given_name":"Test","groups":["TRAINER"]}"""
        return Base64.UrlSafe.encode(payload.encodeToByteArray()).trimEnd('=')
    }

    private fun resultOf(response: String): String =
        Json.parseToJsonElement(response).jsonObject.getValue("result").jsonPrimitive.content
}
