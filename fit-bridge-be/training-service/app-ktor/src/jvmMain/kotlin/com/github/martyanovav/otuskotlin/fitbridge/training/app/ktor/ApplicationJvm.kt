package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.apiV1Mapper
import com.github.martyanovav.otuskotlin.fitbridge.logging.common.FbLoggerProvider
import com.github.martyanovav.otuskotlin.fitbridge.logging.jvm.FbLoggerLogback
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins.initAppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v1.v1Training
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.v1.wsHandlerV1
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.tomcat.jakarta.EngineMain
import io.ktor.server.websocket.webSocket
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.slf4j.event.Level
import java.time.Duration

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.yaml
fun Application.moduleJvm(
    appSettings: AppSettings = initAppSettings(FbLoggerProvider { FbLoggerLogback(it) })
) {
    install(CallLogging) {
        level = Level.INFO
    }

    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders =
            listOf(
                JvmMemoryMetrics(),
                JvmGcMetrics(),
                ProcessorMetrics(),
                JvmThreadMetrics(),
            )
        distributionStatisticConfig =
            DistributionStatisticConfig
                .builder()
                .percentilesHistogram(true)
                .maximumExpectedValue(Duration.ofSeconds(20).toNanos().toDouble())
                .serviceLevelObjectives(
                    Duration.ofMillis(100).toNanos().toDouble(),
                    Duration.ofMillis(500).toNanos().toDouble(),
                ).build()
    }

    routing {
        get("/metrics") {
            try {
                call.respondText(appMicrometerRegistry.scrape(), ContentType.parse("text/plain; version=0.0.4"))
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
                    setConfig(apiV1Mapper.serializationConfig)
                    setConfig(apiV1Mapper.deserializationConfig)
                }
            }
            v1Training(appSettings)
            webSocket("ws") {
                wsHandlerV1(appSettings)
            }
        }
    }
}
