package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2RequestDeserialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.InitResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v2.fromTransport
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v2.toTransport
import com.github.martyanovav.otuskotlin.fitbridge.training.api.log1.mapper.toLog
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionV2
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.executePipeline
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Error as ApiError

suspend fun WebSocketSession.wsHandlerV2(appSettings: AppSettings) {
    val wsSession = KtorWsSessionV2(this)
    val sessions = appSettings.wsSessionsV2
    sessions.add(wsSession)

    try {
        appSettings.processor.exec(
            ClientCardContext(command = FBCommandBase.INIT, wsSession = wsSession),
        )
        wsSession.send(InitResponse(apiVersion = "v2", result = ResponseResult.SUCCESS))
        for (frame in incoming) {
            val response =
                when (frame) {
                    is Frame.Text ->
                        try {
                            frame.readText().processV2(appSettings, wsSession)
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
        apiVersion = "v2",
        result = ResponseResult.ERROR,
        errors = listOf(ApiError(code = code, group = "websocket", message = message)),
    )

private suspend inline fun <reified Q : IRequest, C : IFBContext> processWsReq(
    request: Q,
    wsSession: KtorWsSessionV2,
    appSettings: AppSettings,
    logId: String,
    crossinline makeCtx: () -> C,
    crossinline fromTransport: suspend C.(Q) -> Unit,
    crossinline toTransport: suspend C.() -> IResponse,
    crossinline toLog: (C, String) -> Any
): IResponse {
    val logger = appSettings.corSettings.loggerProvider.logger(logId)
    return executePipeline(
        getContext = { makeCtx().apply { this.wsSession = wsSession } },
        logger = logger,
        logId = logId,
        receive = { fromTransport(request) },
        exec = { appSettings.processor.exec(this) },
        respond = { toTransport() },
        toLog = { toLog(it, logId) }
    )
}

private suspend fun String.processV2(
    appSettings: AppSettings,
    wsSession: KtorWsSessionV2,
): IResponse {
    val request = apiV2RequestDeserialize<IRequest>(this)
    return when (request) {
        is ClientCardCreateRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-clientCard-create", {
                    ClientCardContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is ClientCardReadRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-clientCard-read", {
                    ClientCardContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is ClientCardUpdateRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-clientCard-update", {
                    ClientCardContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is ClientCardArchiveRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-clientCard-archive", {
                    ClientCardContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is ClientCardSearchRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-clientCard-search", {
                    ClientCardContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanCreateRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-trainingPlan-create", {
                    TrainingPlanContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanReadRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-trainingPlan-read", {
                    TrainingPlanContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanUpdateRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-trainingPlan-update", {
                    TrainingPlanContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanArchiveRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-trainingPlan-archive", {
                    TrainingPlanContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanSearchRequest ->
            processWsReq(
                request, wsSession, appSettings, "ws-trainingPlan-search", {
                    TrainingPlanContext()
                }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        else -> error("Unsupported v2 WebSocket request: ${request::class.simpleName}")
    }
}
