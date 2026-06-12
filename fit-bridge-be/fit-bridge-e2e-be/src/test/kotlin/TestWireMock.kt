package com.github.martyanovav.otuskotlin.fitbridge.e2e

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.BindMode

class TestWireMock {
    companion object {
        private lateinit var container: GenericContainer<*>

        @JvmStatic
        @BeforeAll
        fun setUp() {
            container = GenericContainer("wiremock/wiremock:3.4.2")
                .withExposedPorts(8080)
                .withFileSystemBind("../../fit-bridge-other/fit-bridge-dcompose/dcompose/volumes/wm-fitbridge/mappings", "/home/wiremock/mappings", BindMode.READ_ONLY)
            container.start()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            container.stop()
        }
    }

    @Test
    fun `test wiremock is running and root mapping works`() = kotlinx.coroutines.runBlocking {
        val client = HttpClient()
        val port = container.getMappedPort(8080)
        val host = container.host
        
        val response = client.get("http://$host:$port/")
        assertEquals(200, response.status.value)
        assertEquals("Hello, FitBridge Wiremock!", response.bodyAsText())
    }
}
