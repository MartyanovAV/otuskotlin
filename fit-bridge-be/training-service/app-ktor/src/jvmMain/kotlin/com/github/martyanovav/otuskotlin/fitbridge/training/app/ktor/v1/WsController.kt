package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1RequestDeserialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.InitResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.fromTransport
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.toTransport
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionV1
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.executePipeline
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Error as ApiError

suspend fun WebSocketSession.wsHandlerV1(appSettings: AppSettings) {
    val wsSession = KtorWsSessionV1(this)
    val sessions = appSettings.wsSessionsV1
    sessions.add(wsSession)

    try {
        appSettings.processor.exec(
            ClientCardContext(command = FBCommandBase.INIT, wsSession = wsSession),
        )
        wsSession.send(InitResponse(result = ResponseResult.SUCCESS))
        for (frame in incoming) {
            val response =
                when (frame) {
                    is Frame.Text ->
                        try {
                            frame.readText().processV1(appSettings, wsSession)
                        } catch (_: Exception) {
                            wsError("invalid-request", "Invalid or unsupported WebSocket request")
                        }
                    is Frame.Binary -> wsError("unsupported-frame", "Only text JSON frames are supported")
                    else -> continue
                }
            wsSession.send(response)
        }
    } finally {
        appSettings.processor.exec(
            ClientCardContext(command = FBCommandBase.FINISH, wsSession = wsSession),
        )
        sessions.remove(wsSession)
    }
}

private fun wsError(
    code: String,
    message: String,
): IResponse =
    InitResponse(
        result = ResponseResult.ERROR,
        errors = listOf(ApiError(code = code, group = "websocket", message = message)),
    )

private suspend inline fun <reified Q : IRequest, C : IFBContext> processWsReq(
    request: Q,
    wsSession: KtorWsSessionV1,
    appSettings: AppSettings,
    crossinline makeCtx: () -> C,
    crossinline fromTransport: suspend C.(Q) -> Unit,
    crossinline toTransport: suspend C.() -> IResponse
): IResponse = executePipeline(
    getContext = { makeCtx().apply { this.wsSession = wsSession } },
    clazz = Q::class,
    receive = { fromTransport(request) },
    exec = { appSettings.processor.exec(this) },
    respond = { toTransport() },
    toLog = { /* toLog */ }
)

private suspend fun String.processV1(
    appSettings: AppSettings,
    wsSession: KtorWsSessionV1,
): IResponse {
    val request = apiV1RequestDeserialize<IRequest>(this)
    return when (request) {
        is ClientCardCreateRequest -> processWsReq(
            request, wsSession, appSettings, { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is ClientCardReadRequest -> processWsReq(
            request, wsSession, appSettings, { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is ClientCardUpdateRequest -> processWsReq(
            request, wsSession, appSettings, { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is ClientCardArchiveRequest -> processWsReq(
            request, wsSession, appSettings, { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is ClientCardSearchRequest -> processWsReq(
            request, wsSession, appSettings, { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is TrainingPlanCreateRequest -> processWsReq(
            request, wsSession, appSettings, { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is TrainingPlanReadRequest -> processWsReq(
            request, wsSession, appSettings, { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is TrainingPlanUpdateRequest -> processWsReq(
            request, wsSession, appSettings, { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is TrainingPlanArchiveRequest -> processWsReq(
            request, wsSession, appSettings, { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        is TrainingPlanSearchRequest -> processWsReq(
            request, wsSession, appSettings, { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
        )
        else -> error("Unsupported v1 WebSocket request: ${request::class.simpleName}")
    }
}
