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
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.ControllerHelper
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
> ApplicationCall.processV2(
    appSettings: AppSettings,
    clazz: KClass<*>,
    logId: String,
) {
    val request = apiV2RequestDeserialize<Q>(receiveText())
    when (request) {
        is ClientCardCreateRequest,
        is ClientCardReadRequest,
        is ClientCardUpdateRequest,
        is ClientCardArchiveRequest,
        is ClientCardSearchRequest,
        -> {
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
                { appSettings.processor.exec(this) },
                {
                    respondText(
                        apiV2ResponseSerialize(toTransport() as IResponse),
                        ContentType.Application.Json,
                    )
                },
                { /* toLog */ },
            )
        }
        is TrainingPlanCreateRequest,
        is TrainingPlanReadRequest,
        is TrainingPlanUpdateRequest,
        is TrainingPlanArchiveRequest,
        is TrainingPlanSearchRequest,
        -> {
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
                { appSettings.processor.exec(this) },
                {
                    respondText(
                        apiV2ResponseSerialize(toTransport() as IResponse),
                        ContentType.Application.Json,
                    )
                },
                { /* toLog */ },
            )
        }
    }
}

fun Route.v2Training(appSettings: AppSettings) {
    route("client-card") {
        post("create") { call.processV2<ClientCardCreateRequest, IResponse>(appSettings, this::class, "clientCard-create") }
        post("read") { call.processV2<ClientCardReadRequest, IResponse>(appSettings, this::class, "clientCard-read") }
        post("update") { call.processV2<ClientCardUpdateRequest, IResponse>(appSettings, this::class, "clientCard-update") }
        post("archive") { call.processV2<ClientCardArchiveRequest, IResponse>(appSettings, this::class, "clientCard-archive") }
        post("search") { call.processV2<ClientCardSearchRequest, IResponse>(appSettings, this::class, "clientCard-search") }
    }
    route("training-plan") {
        post("create") { call.processV2<TrainingPlanCreateRequest, IResponse>(appSettings, this::class, "trainingPlan-create") }
        post("read") { call.processV2<TrainingPlanReadRequest, IResponse>(appSettings, this::class, "trainingPlan-read") }
        post("update") { call.processV2<TrainingPlanUpdateRequest, IResponse>(appSettings, this::class, "trainingPlan-update") }
        post("archive") { call.processV2<TrainingPlanArchiveRequest, IResponse>(appSettings, this::class, "trainingPlan-archive") }
        post("search") { call.processV2<TrainingPlanSearchRequest, IResponse>(appSettings, this::class, "trainingPlan-search") }
    }
}
