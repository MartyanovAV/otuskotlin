package com.github.martyanovav.otuskotlin.fitbridge.e2e.scenarios.v2

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.BindMode

class ScenarioTrainingPlanV2 {
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
    fun `test create training plan v2`() = kotlinx.coroutines.runBlocking {
        val client = HttpClient()
        val port = container.getMappedPort(8080)
        val host = container.host
        
        val response = client.post("http://$host:$port/v2/trainingPlan/create") {
            contentType(ContentType.Application.Json)
            setBody("""{ "requestType": "create" }""")
        }
        assertEquals(200, response.status.value)
    }

    @Test
    fun `test read training plan v2`() = kotlinx.coroutines.runBlocking {
        val client = HttpClient()
        val port = container.getMappedPort(8080)
        val host = container.host
        
        val response = client.post("http://$host:$port/v2/trainingPlan/read") {
            contentType(ContentType.Application.Json)
            setBody("""{ "requestType": "read" }""")
        }
        assertEquals(200, response.status.value)
    }

    @Test
    fun `test update training plan v2`() = kotlinx.coroutines.runBlocking {
        val client = HttpClient()
        val port = container.getMappedPort(8080)
        val host = container.host
        
        val response = client.post("http://$host:$port/v2/trainingPlan/update") {
            contentType(ContentType.Application.Json)
            setBody("""{ "requestType": "update" }""")
        }
        assertEquals(200, response.status.value)
    }

    @Test
    fun `test archive training plan v2`() = kotlinx.coroutines.runBlocking {
        val client = HttpClient()
        val port = container.getMappedPort(8080)
        val host = container.host
        
        val response = client.post("http://$host:$port/v2/trainingPlan/archive") {
            contentType(ContentType.Application.Json)
            setBody("""{ "requestType": "archive" }""")
        }
        assertEquals(200, response.status.value)
    }

    @Test
    fun `test search training plan v2`() = kotlinx.coroutines.runBlocking {
        val client = HttpClient()
        val port = container.getMappedPort(8080)
        val host = container.host
        
        val response = client.post("http://$host:$port/v2/trainingPlan/search") {
            contentType(ContentType.Application.Json)
            setBody("""{ "requestType": "search" }""")
        }
        assertEquals(200, response.status.value)
    }
}

