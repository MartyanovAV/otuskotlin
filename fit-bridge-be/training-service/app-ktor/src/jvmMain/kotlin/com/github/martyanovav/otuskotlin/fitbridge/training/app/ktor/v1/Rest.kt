package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.*
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.fromTransport
import com.github.martyanovav.otuskotlin.fitbridge.mappers.v1.toTransport
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.ControllerHelper
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.reflect.KClass

suspend inline fun <reified Q : IRequest, @Suppress("unused") reified R : IResponse> ApplicationCall.processV1(
    appSettings: AppSettings,
    clazz: KClass<*>,
    logId: String,
) {
    val request = receive<Q>()
    when (request) {
        is ClientCardCreateRequest, is ClientCardReadRequest, is ClientCardUpdateRequest, is ClientCardArchiveRequest, is ClientCardSearchRequest -> {
            ControllerHelper<ClientCardContext, Unit>(
                { ClientCardContext() },
                clazz,
                {
                    when (request) {
                        is ClientCardCreateRequest -> fromTransport(request)
                        is ClientCardReadRequest -> fromTransport(request)
                        is ClientCardUpdateRequest -> fromTransport(request)
                        is ClientCardArchiveRequest -> fromTransport(request)
                        is ClientCardSearchRequest -> fromTransport(request)
                    }
                },
                { /* processor.exec(this) */ },
                { respond(toTransport()) },
                { /* toLog */ }
            )
        }
        is TrainingPlanCreateRequest, is TrainingPlanReadRequest, is TrainingPlanUpdateRequest, is TrainingPlanArchiveRequest, is TrainingPlanSearchRequest -> {
            ControllerHelper<TrainingPlanContext, Unit>(
                { TrainingPlanContext() },
                clazz,
                {
                    when (request) {
                        is TrainingPlanCreateRequest -> fromTransport(request)
                        is TrainingPlanReadRequest -> fromTransport(request)
                        is TrainingPlanUpdateRequest -> fromTransport(request)
                        is TrainingPlanArchiveRequest -> fromTransport(request)
                        is TrainingPlanSearchRequest -> fromTransport(request)
                    }
                },
                { /* processor.exec(this) */ },
                { respond(toTransport()) },
                { /* toLog */ }
            )
        }
    }
}

fun Route.v1Training(appSettings: AppSettings) {
    route("client-card") {
        post("create") { call.processV1<ClientCardCreateRequest, IResponse>(appSettings, this::class, "clientCard-create") }
        post("read") { call.processV1<ClientCardReadRequest, IResponse>(appSettings, this::class, "clientCard-read") }
        post("update") { call.processV1<ClientCardUpdateRequest, IResponse>(appSettings, this::class, "clientCard-update") }
        post("archive") { call.processV1<ClientCardArchiveRequest, IResponse>(appSettings, this::class, "clientCard-archive") }
        post("search") { call.processV1<ClientCardSearchRequest, IResponse>(appSettings, this::class, "clientCard-search") }
    }
    route("training-plan") {
        post("create") { call.processV1<TrainingPlanCreateRequest, IResponse>(appSettings, this::class, "trainingPlan-create") }
        post("read") { call.processV1<TrainingPlanReadRequest, IResponse>(appSettings, this::class, "trainingPlan-read") }
        post("update") { call.processV1<TrainingPlanUpdateRequest, IResponse>(appSettings, this::class, "trainingPlan-update") }
        post("archive") { call.processV1<TrainingPlanArchiveRequest, IResponse>(appSettings, this::class, "trainingPlan-archive") }
        post("search") { call.processV1<TrainingPlanSearchRequest, IResponse>(appSettings, this::class, "trainingPlan-search") }
    }
}
