package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2RequestDeserialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2ResponseSerialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.IResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.TrainingPlanUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v2.fromTransport
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v2.toTransport
import com.github.martyanovav.otuskotlin.fitbridge.training.api.log1.mapper.toLog
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.executePipeline
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

suspend inline fun <
    reified Q : IRequest,
    @Suppress("unused")
    reified R : IResponse,
    C : IFBContext
    > ApplicationCall.processV2(
    appSettings: AppSettings,
    logId: String,
    crossinline makeContext: () -> C,
    crossinline fromTransport: suspend C.(Q) -> Unit,
    crossinline toTransport: suspend C.() -> R,
    crossinline toLog: (C, String) -> Any
) {
    val logger = appSettings.corSettings.loggerProvider.logger(logId)
    val request = apiV2RequestDeserialize<Q>(receiveText())
    executePipeline(
        getContext = makeContext,
        logger = logger,
        logId = logId,
        receive = { fromTransport(request) },
        exec = { appSettings.processor.exec(this) },
        respond = {
            respondText(
                apiV2ResponseSerialize(toTransport() as IResponse),
                ContentType.Application.Json,
            )
        },
        toLog = { toLog(it, logId) },
    )
}

fun Route.v2Training(appSettings: AppSettings) {
    route("client-card") {
        post("create") {
            call.processV2<ClientCardCreateRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-create",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("read") {
            call.processV2<ClientCardReadRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-read",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("update") {
            call.processV2<ClientCardUpdateRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-update",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("archive") {
            call.processV2<ClientCardArchiveRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-archive",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("search") {
            call.processV2<ClientCardSearchRequest, IResponse, ClientCardContext>(
                appSettings, "clientCard-search",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
    }
    route("training-plan") {
        post("create") {
            call.processV2<TrainingPlanCreateRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-create",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("read") {
            call.processV2<TrainingPlanReadRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-read",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("update") {
            call.processV2<TrainingPlanUpdateRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-update",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("archive") {
            call.processV2<TrainingPlanArchiveRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-archive",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
        post("search") {
            call.processV2<TrainingPlanSearchRequest, IResponse, TrainingPlanContext>(
                appSettings, "trainingPlan-search",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse },
                { ctx, id -> ctx.toLog(id) }
            )
        }
    }
}
