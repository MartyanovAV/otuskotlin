package com.github.martyanovav.otuskotlin.fitbridge.e2e

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.time.Duration

private const val ENVOY_SERVICE = "envoy-1"
private const val ENVOY_PORT = 8080

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(FitBridgeComposeExtension::class)
internal annotation class WithFitBridgeStack

internal class FitBridgeComposeExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        context.root
            .getStore(NAMESPACE)
            .getOrComputeIfAbsent(STACK_KEY) { FitBridgeStackResource.start() }
    }

    private companion object {
        val NAMESPACE: ExtensionContext.Namespace =
            ExtensionContext.Namespace.create(FitBridgeComposeExtension::class.java)
        const val STACK_KEY = "fit-bridge-stack"
    }
}

private class FitBridgeStackResource private constructor(
    private val compose: ComposeContainer,
) : ExtensionContext.Store.CloseableResource {
    override fun close() {
        System.clearProperty(BASE_URL_PROPERTY)
        compose.stop()
    }

    companion object {
        private const val BASE_URL_PROPERTY = "fitbridge.e2e.baseUrl"
        private val logger = LoggerFactory.getLogger("fitbridge.e2e.compose")

        fun start(): FitBridgeStackResource {
            val composeFile = File("build/dcompose/docker-compose.yml").absoluteFile
            require(composeFile.isFile) {
                "Docker Compose resource not found: $composeFile. Run extractLibResources before E2E tests."
            }

            val compose = ComposeContainer(composeFile)
                .withExposedService(
                    ENVOY_SERVICE,
                    ENVOY_PORT,
                    Wait.forHttp("/health")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)),
                )
                .withLogConsumer("postgresql-1", logConsumer("postgresql"))
                .withLogConsumer("training-service-1", logConsumer("training-service"))
                .withLogConsumer("keycloak-1", logConsumer("keycloak"))
                .withLogConsumer(ENVOY_SERVICE, logConsumer("envoy"))
                .withRemoveVolumes(true)
                .withStartupTimeout(Duration.ofMinutes(5))

            try {
                compose.start()
                val host = compose.getServiceHost(ENVOY_SERVICE, ENVOY_PORT)
                val port = compose.getServicePort(ENVOY_SERVICE, ENVOY_PORT)
                System.setProperty(BASE_URL_PROPERTY, "http://$host:$port")
                return FitBridgeStackResource(compose)
            } catch (error: Throwable) {
                compose.stop()
                throw error
            }
        }

        private fun logConsumer(prefix: String) = Slf4jLogConsumer(logger).withPrefix(prefix)
    }
}
