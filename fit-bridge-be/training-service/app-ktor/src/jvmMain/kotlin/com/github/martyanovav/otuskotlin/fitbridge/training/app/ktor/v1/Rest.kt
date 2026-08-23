package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.logging.common.IFbLogWrapper
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.fromTransport
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.toTransport
import com.github.martyanovav.otuskotlin.fitbridge.training.api.log1.mapper.toLog
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.AUTH_HEADER
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.jwt2principal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.executePipeline
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

suspend inline fun <
    reified Q : IRequest,
    @Suppress("unused")
    reified R : IResponse,
    C : IFBContext
    > ApplicationCall.processV1(
    logger: IFbLogWrapper,
    crossinline processorExec: suspend (C) -> Unit,
    logId: String,
    crossinline makeContext: () -> C,
    crossinline fromTransport: suspend C.(Q) -> Unit,
    crossinline toTransport: suspend C.() -> R,
    crossinline toLog: (C, String) -> Any
) {
    val request = receive<Q>()
    val principal = this.request.header(AUTH_HEADER).jwt2principal()
    executePipeline(
        getContext = { makeContext().apply { this.principal = principal } },
        logger = logger,
        logId = logId,
        receive = { fromTransport(request) },
        exec = { processorExec(this) },
        respond = { respond(toTransport()) },
        toLog = { toLog(it, logId) },
    )
}

fun Route.v1Training(appSettings: AppSettings) {
    route("client-card") {
        post("create") {
            call.processV1<ClientCardCreateRequest, IResponse, ClientCardContext>(
                appSettings.ccCorSettings.loggerProvider.logger("clientCard-create"),
                { appSettings.ccProcessor.exec(it) }, "clientCard-create",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("read") {
            call.processV1<ClientCardReadRequest, IResponse, ClientCardContext>(
                appSettings.ccCorSettings.loggerProvider.logger("clientCard-read"),
                { appSettings.ccProcessor.exec(it) }, "clientCard-read",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("update") {
            call.processV1<ClientCardUpdateRequest, IResponse, ClientCardContext>(
                appSettings.ccCorSettings.loggerProvider.logger("clientCard-update"),
                { appSettings.ccProcessor.exec(it) }, "clientCard-update",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("archive") {
            call.processV1<ClientCardArchiveRequest, IResponse, ClientCardContext>(
                appSettings.ccCorSettings.loggerProvider.logger("clientCard-archive"),
                { appSettings.ccProcessor.exec(it) }, "clientCard-archive",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("search") {
            call.processV1<ClientCardSearchRequest, IResponse, ClientCardContext>(
                appSettings.ccCorSettings.loggerProvider.logger("clientCard-search"),
                { appSettings.ccProcessor.exec(it) }, "clientCard-search",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
    }
    route("training-plan") {
        post("create") {
            call.processV1<TrainingPlanCreateRequest, IResponse, TrainingPlanContext>(
                appSettings.tpCorSettings.loggerProvider.logger("trainingPlan-create"),
                { appSettings.tpProcessor.exec(it) }, "trainingPlan-create",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("read") {
            call.processV1<TrainingPlanReadRequest, IResponse, TrainingPlanContext>(
                appSettings.tpCorSettings.loggerProvider.logger("trainingPlan-read"),
                { appSettings.tpProcessor.exec(it) }, "trainingPlan-read",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("update") {
            call.processV1<TrainingPlanUpdateRequest, IResponse, TrainingPlanContext>(
                appSettings.tpCorSettings.loggerProvider.logger("trainingPlan-update"),
                { appSettings.tpProcessor.exec(it) }, "trainingPlan-update",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("archive") {
            call.processV1<TrainingPlanArchiveRequest, IResponse, TrainingPlanContext>(
                appSettings.tpCorSettings.loggerProvider.logger("trainingPlan-archive"),
                { appSettings.tpProcessor.exec(it) }, "trainingPlan-archive",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("search") {
            call.processV1<TrainingPlanSearchRequest, IResponse, TrainingPlanContext>(
                appSettings.tpCorSettings.loggerProvider.logger("trainingPlan-search"),
                { appSettings.tpProcessor.exec(it) }, "trainingPlan-search",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
    }
}
