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
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.executePipeline
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.reflect.KClass

suspend inline fun <
    reified Q : IRequest,
    @Suppress("unused")
    reified R : IResponse,
    C : IFBContext
    > ApplicationCall.processV1(
    appSettings: AppSettings,
    clazz: KClass<*>,
    logId: String,
    crossinline makeContext: () -> C,
    crossinline fromTransport: suspend C.(Q) -> Unit,
    crossinline toTransport: suspend C.() -> R
) {
    val request = receive<Q>()
    executePipeline(
        getContext = makeContext,
        clazz = clazz,
        receive = { fromTransport(request) },
        exec = { appSettings.processor.exec(this) },
        respond = { respond(toTransport()) },
        toLog = { /* toLog */ },
    )
}

fun Route.v1Training(appSettings: AppSettings) {
    route("client-card") {
        post("create") {
            call.processV1<ClientCardCreateRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-create",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("read") {
            call.processV1<ClientCardReadRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-read",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("update") {
            call.processV1<ClientCardUpdateRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-update",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("archive") {
            call.processV1<ClientCardArchiveRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-archive",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("search") {
            call.processV1<ClientCardSearchRequest, IResponse, ClientCardContext>(
                appSettings, this::class, "clientCard-search",
                { ClientCardContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
    }
    route("training-plan") {
        post("create") {
            call.processV1<TrainingPlanCreateRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-create",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("read") {
            call.processV1<TrainingPlanReadRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-read",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("update") {
            call.processV1<TrainingPlanUpdateRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-update",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("archive") {
            call.processV1<TrainingPlanArchiveRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-archive",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
        post("search") {
            call.processV1<TrainingPlanSearchRequest, IResponse, TrainingPlanContext>(
                appSettings, this::class, "trainingPlan-search",
                { TrainingPlanContext() }, { fromTransport(it) }, { toTransport() as IResponse }
            )
        }
    }
}
