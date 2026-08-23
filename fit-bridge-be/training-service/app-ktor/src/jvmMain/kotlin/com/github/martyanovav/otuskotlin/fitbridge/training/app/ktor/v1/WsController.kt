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
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanActivateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCompleteRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.logging.common.IFbLogWrapper
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.fromTransport
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.toTransport
import com.github.martyanovav.otuskotlin.fitbridge.training.api.log1.mapper.toLog
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionV1
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.executePipeline
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Error as ApiError

suspend fun WebSocketSession.wsHandlerV1(
    appSettings: AppSettings,
    principal: AuthPrincipal,
) {
    val wsSession = KtorWsSessionV1(this, principal)
    val sessions = appSettings.wsSessionsV1
    sessions.add(wsSession)

    try {
        appSettings.ccProcessor.exec(
            ClientCardContext(command = FBCommandBase.INIT, wsSession = wsSession, principal = principal),
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
        result = ResponseResult.ERROR,
        errors = listOf(ApiError(code = code, group = "websocket", message = message)),
    )

private suspend inline fun <reified Q : IRequest, C : IFBContext> processWsReq(
    request: Q,
    wsSession: KtorWsSessionV1,
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
        exec = { execProc(this as C) },
        respond = { toTransport() },
        toLog = { toLog(it, logId) }
    )
}

private suspend fun String.processV1(
    appSettings: AppSettings,
    wsSession: KtorWsSessionV1,
): IResponse {
    val request = apiV1RequestDeserialize<IRequest>(this)
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
        else -> error("Unsupported v1 WebSocket request: ${request::class.simpleName}")
    }
}
