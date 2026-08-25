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
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanActivateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCompleteRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.logging.common.IFbLogWrapper
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v2.fromTransport
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v2.toTransport
import com.github.martyanovav.otuskotlin.fitbridge.training.api.log1.mapper.toLog
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionV2
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.executePipeline
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.Error as ApiError

suspend fun WebSocketSession.wsHandlerV2(
    appSettings: AppSettings,
    principal: AuthPrincipal,
) {
    val wsSession = KtorWsSessionV2(this, principal)
    val sessions = appSettings.wsSessionsV2
    sessions.add(wsSession)

    try {
        appSettings.ccProcessor.exec(
            ClientCardContext(command = FBCommandBase.INIT, wsSession = wsSession, principal = principal),
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
        appSettings.ccProcessor.exec(
            ClientCardContext(command = FBCommandBase.FINISH, wsSession = wsSession, principal = principal),
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
    logger: IFbLogWrapper,
    logId: String,
    crossinline makeCtx: () -> C,
    crossinline execProc: suspend (C) -> Unit,
    crossinline fromTransport: suspend C.(Q) -> Unit,
    crossinline toTransport: suspend C.() -> IResponse,
    crossinline toLog: (C, String) -> Any
): IResponse {
    return executePipeline(
        getContext = {
            makeCtx().apply {
                this.wsSession = wsSession
                principal = wsSession.principal
            }
        },
        logger = logger,
        logId = logId,
        receive = { fromTransport(request) },
        exec = { execProc(this) },
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
                request, wsSession, appSettings.ccCorSettings.loggerProvider.logger("ws-clientCard-create"), "ws-clientCard-create", {
                    ClientCardContext()
                }, { appSettings.ccProcessor.exec(it) }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is ClientCardReadRequest ->
            processWsReq(
                request, wsSession, appSettings.ccCorSettings.loggerProvider.logger("ws-clientCard-read"), "ws-clientCard-read", {
                    ClientCardContext()
                }, { appSettings.ccProcessor.exec(it) }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is ClientCardUpdateRequest ->
            processWsReq(
                request, wsSession, appSettings.ccCorSettings.loggerProvider.logger("ws-clientCard-update"), "ws-clientCard-update", {
                    ClientCardContext()
                }, { appSettings.ccProcessor.exec(it) }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is ClientCardArchiveRequest ->
            processWsReq(
                request, wsSession, appSettings.ccCorSettings.loggerProvider.logger("ws-clientCard-archive"), "ws-clientCard-archive", {
                    ClientCardContext()
                }, { appSettings.ccProcessor.exec(it) }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is ClientCardSearchRequest ->
            processWsReq(
                request, wsSession, appSettings.ccCorSettings.loggerProvider.logger("ws-clientCard-search"), "ws-clientCard-search", {
                    ClientCardContext()
                }, { appSettings.ccProcessor.exec(it) }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanCreateRequest ->
            processWsReq(
                request, wsSession, appSettings.tpCorSettings.loggerProvider.logger("ws-trainingPlan-create"), "ws-trainingPlan-create", {
                    TrainingPlanContext()
                }, { appSettings.tpProcessor.exec(it) }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanReadRequest ->
            processWsReq(
                request, wsSession, appSettings.tpCorSettings.loggerProvider.logger("ws-trainingPlan-read"), "ws-trainingPlan-read", {
                    TrainingPlanContext()
                }, { appSettings.tpProcessor.exec(it) }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanUpdateRequest ->
            processWsReq(
                request, wsSession, appSettings.tpCorSettings.loggerProvider.logger("ws-trainingPlan-update"), "ws-trainingPlan-update", {
                    TrainingPlanContext()
                }, { appSettings.tpProcessor.exec(it) }, { fromTransport(it) }, { toTransport() as IResponse }, { ctx, id -> ctx.toLog(id) }
            )
        is TrainingPlanArchiveRequest ->
            processWsReq(
                request,
                wsSession,
                appSettings.tpCorSettings.loggerProvider.logger("ws-trainingPlan-archive"),
                "ws-trainingPlan-archive",
                { TrainingPlanContext() },
                { appSettings.tpProcessor.exec(it) },
                { fromTransport(it) },
                { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) },
            )
        is TrainingPlanActivateRequest ->
            processWsReq(
                request,
                wsSession,
                appSettings.tpCorSettings.loggerProvider.logger("ws-trainingPlan-activate"),
                "ws-trainingPlan-activate",
                { TrainingPlanContext() },
                { appSettings.tpProcessor.exec(it) },
                { fromTransport(it) },
                { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) },
            )
        is TrainingPlanCompleteRequest ->
            processWsReq(
                request,
                wsSession,
                appSettings.tpCorSettings.loggerProvider.logger("ws-trainingPlan-complete"),
                "ws-trainingPlan-complete",
                { TrainingPlanContext() },
                { appSettings.tpProcessor.exec(it) },
                { fromTransport(it) },
                { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) },
            )
        is TrainingPlanSearchRequest ->
            processWsReq(
                request,
                wsSession,
                appSettings.tpCorSettings.loggerProvider.logger("ws-trainingPlan-search"),
                "ws-trainingPlan-search",
                { TrainingPlanContext() },
                { appSettings.tpProcessor.exec(it) },
                { fromTransport(it) },
                { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) },
            )
    }
}
