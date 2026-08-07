package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2RequestDeserialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.apiV2ResponseSerialize
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.*
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v2.fromTransport
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v2.toTransport
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.executePipeline
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.reflect.KClass

suspend inline fun <
    reified Q : IRequest,
    @Suppress("unused")
    reified R : IResponse,
    C : IFBContext
> ApplicationCall.processV2(
    appSettings: AppSettings,
    clazz: KClass<*>,
    logId: String,
    crossinline makeContext: () -> C,
    crossinline fromTransport: suspend C.(Q) -> Unit,
    crossinline toTransport: suspend C.() -> R
) {
    val request = apiV2RequestDeserialize<Q>(receiveText())
    executePipeline(
        getContext = makeContext,
        clazz = clazz,
        receive = { fromTransport(request) },
        exec = { appSettings.processor.exec(this) },
        respond = {
            respondText(
                apiV2ResponseSerialize(toTransport() as IResponse),
                ContentType.Application.Json,
            )
        },
        toLog = { /* toLog */ },
    )
}

fun Route.v2Training(appSettings: AppSettings) {
    route("client-card") {
        post("create") {
            call.processV2<ClientCardCreateRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-create",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("read") {
            call.processV2<ClientCardReadRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-read",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("update") {
            call.processV2<ClientCardUpdateRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-update",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("archive") {
            call.processV2<ClientCardArchiveRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-archive",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("search") {
            call.processV2<ClientCardSearchRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-search",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
    }
    route("training-plan") {
        post("create") {
            call.processV2<TrainingPlanCreateRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-create",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("read") {
            call.processV2<TrainingPlanReadRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-read",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("update") {
            call.processV2<TrainingPlanUpdateRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-update",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("archive") {
            call.processV2<TrainingPlanArchiveRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-archive",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("search") {
            call.processV2<TrainingPlanSearchRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-search",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
    }
}
