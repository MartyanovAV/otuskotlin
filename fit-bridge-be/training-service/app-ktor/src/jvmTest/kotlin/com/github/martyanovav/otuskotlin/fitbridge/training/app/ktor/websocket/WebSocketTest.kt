package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.websocket

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1RequestSerialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1ResponseDeserialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2RequestSerialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2ResponseDeserialize
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.module
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.moduleJvm
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadRequest as ClientCardReadRequestV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadResponse as ClientCardReadResponseV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug as DebugV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IResponse as IResponseV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.InitResponse as InitResponseV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode as RequestDebugModeV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugStubs as RequestDebugStubsV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult as ResponseResultV1
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Debug as DebugV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse as IResponseV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.InitResponse as InitResponseV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugMode as RequestDebugModeV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.RequestDebugStubs as RequestDebugStubsV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult as ResponseResultV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadRequest as TrainingPlanReadRequestV2
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadResponse as TrainingPlanReadResponseV2

class WebSocketTest {
    @Test
    fun v1WebSocketReturnsInitAndClientCardResponse() =
        testApplication {
            application { moduleJvm() }
            val client = createClient { install(WebSockets) }

            client.webSocket("/v1/ws") {
                val init =
                    withTimeout(3.seconds) {
                        apiV1ResponseDeserialize<IResponseV1>((incoming.receive() as Frame.Text).readText())
                    }
                assertIs<InitResponseV1>(init)

                send(Frame.Text("not-json"))
                val invalid =
                    withTimeout(3.seconds) {
                        apiV1ResponseDeserialize<IResponseV1>((incoming.receive() as Frame.Text).readText())
                    }
                assertIs<InitResponseV1>(invalid)
                assertEquals(ResponseResultV1.ERROR, invalid.result)
                assertEquals("invalid-request", invalid.errors?.firstOrNull()?.code)

                val request =
                    ClientCardReadRequestV1(
                        requestType = "clientCard.read",
                        requestId = "training-ws-v1",
                        debug = DebugV1(mode = RequestDebugModeV1.STUB, stub = RequestDebugStubsV1.SUCCESS),
                    )
                send(Frame.Text(apiV1RequestSerialize(request)))

                val response =
                    withTimeout(3.seconds) {
                        apiV1ResponseDeserialize<IResponseV1>((incoming.receive() as Frame.Text).readText())
                    }
                assertIs<ClientCardReadResponseV1>(response)
                assertEquals("training-ws-v1", response.requestId)
                assertEquals(ResponseResultV1.SUCCESS, response.result)
                assertEquals("Анна Смирнова", response.clientCard?.displayName)
            }
        }

    @Test
    fun v2WebSocketReturnsInitAndTrainingPlanResponse() =
        testApplication {
            application { module() }
            val client = createClient { install(WebSockets) }

            client.webSocket("/v2/ws") {
                val init =
                    withTimeout(3.seconds) {
                        apiV2ResponseDeserialize<IResponseV2>((incoming.receive() as Frame.Text).readText())
                    }
                assertIs<InitResponseV2>(init)

                send(Frame.Binary(true, byteArrayOf(1, 2, 3)))
                val invalid =
                    withTimeout(3.seconds) {
                        apiV2ResponseDeserialize<IResponseV2>((incoming.receive() as Frame.Text).readText())
                    }
                assertIs<InitResponseV2>(invalid)
                assertEquals(ResponseResultV2.ERROR, invalid.result)
                assertEquals("unsupported-frame", invalid.errors?.firstOrNull()?.code)

                val request =
                    TrainingPlanReadRequestV2(
                        requestId = "training-ws-v2",
                        apiVersion = "v2",
                        debug = DebugV2(mode = RequestDebugModeV2.STUB, stub = RequestDebugStubsV2.SUCCESS),
                    )
                send(Frame.Text(apiV2RequestSerialize(request)))

                val response =
                    withTimeout(3.seconds) {
                        apiV2ResponseDeserialize<IResponseV2>((incoming.receive() as Frame.Text).readText())
                    }
                assertIs<TrainingPlanReadResponseV2>(response)
                assertEquals("training-ws-v2", response.requestId)
                assertEquals(ResponseResultV2.SUCCESS, response.result)
                assertEquals("Базовая тренировка", response.trainingPlan?.title)
            }
        }
}
