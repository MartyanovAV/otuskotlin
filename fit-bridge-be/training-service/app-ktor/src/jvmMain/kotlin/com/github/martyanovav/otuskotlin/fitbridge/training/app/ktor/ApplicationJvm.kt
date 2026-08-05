package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initAppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v1.v1Training
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.tomcat.jakarta.EngineMain.main(args)

@Suppress("unused") // Referenced in application.yaml
fun Application.moduleJvm(appSettings: AppSettings = initAppSettings()) {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            JvmThreadMetrics(),
            ProcessorMetrics()
        )
        distributionStatisticConfig = io.micrometer.core.instrument.distribution.DistributionStatisticConfig.builder()
            .percentilesHistogram(true)
            .serviceLevelObjectives(
                Duration.ofMillis(100).toNanos().toDouble(),
                Duration.ofMillis(300).toNanos().toDouble(),
                Duration.ofSeconds(1).toNanos().toDouble()
            )
            .build()
    }

    routing {
        get("/metrics") {
            try {
                call.respondText(
                    text = appMicrometerRegistry.scrape(),
                    contentType = ContentType.Text.Plain
                )
            } catch (e: Exception) {
                call.respondText("Error", status = HttpStatusCode.InternalServerError)
            }
        }
    }

    module(appSettings)

    routing {
        route("v1") {
            install(ContentNegotiation) {
                jackson {
                    setConfig(com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1Mapper.serializationConfig)
                    setConfig(com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1Mapper.deserializationConfig)
                }
            }
            v1Training(appSettings)
        }
    }
}
