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
    appSettings: AppSettings,
    logId: String,
    crossinline makeContext: () -> C,
    crossinline fromTransport: suspend C.(Q) -> Unit,
    crossinline toTransport: suspend C.() -> R,
    crossinline toLog: (C, String) -> Any
) {
    val logger = appSettings.corSettings.loggerProvider.logger(logId)
    val request = receive<Q>()
    val principal = this.request.header(AUTH_HEADER).jwt2principal()
    executePipeline(
        getContext = { makeContext().apply { this.principal = principal } },
        logger = logger,
        logId = logId,
        receive = { fromTransport(request) },
        exec = { appSettings.processor.exec(this) },
        respond = { respond(toTransport()) },
        toLog = { toLog(it, logId) },
    )
}

fun Route.v1Training(appSettings: AppSettings) {
    route("client-card") {
        post("create") {
            call.processV1<ClientCardCreateRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-create",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("read") {
            call.processV1<ClientCardReadRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-read",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("update") {
            call.processV1<ClientCardUpdateRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-update",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("archive") {
            call.processV1<ClientCardArchiveRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-archive",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("search") {
            call.processV1<ClientCardSearchRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-search",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
    }
    route("training-plan") {
        post("create") {
            call.processV1<TrainingPlanCreateRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-create",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("read") {
            call.processV1<TrainingPlanReadRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-read",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("update") {
            call.processV1<TrainingPlanUpdateRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-update",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("archive") {
            call.processV1<TrainingPlanArchiveRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-archive",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("search") {
            call.processV1<TrainingPlanSearchRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-search",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
    }
}
