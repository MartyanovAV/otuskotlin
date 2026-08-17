package com.github.martyanovav.otuskotlin.fitbridge.e2e

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.assertEquals

internal class FitBridgeE2eClient : AutoCloseable {
    private val baseUrl = configuration("fitbridge.e2e.baseUrl", "FITBRIDGE_E2E_BASE_URL", "http://localhost:8080")
        .trimEnd('/')
    private val username = configuration("fitbridge.e2e.username", "FITBRIDGE_E2E_USERNAME", "fitbridge-test")
    private val password = configuration("fitbridge.e2e.password", "FITBRIDGE_E2E_PASSWORD", "fitbridge")
    private val clientId = configuration("fitbridge.e2e.clientId", "FITBRIDGE_E2E_CLIENT_ID", "fit-bridge-service")
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(OkHttp) {
        expectSuccess = false
    }
    private var accessToken: String? = null

    suspend fun get(path: String): E2eResponse = client.get("$baseUrl$path").toE2eResponse()

    suspend fun post(
        path: String,
        body: String,
        authenticated: Boolean = true,
    ): E2eResponse = client.post("$baseUrl$path") {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        requestId(body)?.let { header("X-Request-ID", it) }
        if (authenticated) bearerAuth(token())
        setBody(body)
    }.toE2eResponse()

    suspend fun requireHealthy(vararg paths: String) {
        paths.forEach { path ->
            val response = get(path)
            assertEquals(HttpStatusCode.OK, response.status, "Health check $path failed: ${response.body}")
        }
    }

    private suspend fun token(): String {
        accessToken?.let { return it }

        val response = client.submitForm(
            url = "$baseUrl/realms/fit-bridge/protocol/openid-connect/token",
            formParameters = Parameters.build {
                append("client_id", clientId)
                append("username", username)
                append("password", password)
                append("grant_type", "password")
            },
        )
        val body = response.bodyAsText()
        check(response.status == HttpStatusCode.OK) {
            "Unable to obtain E2E access token: HTTP ${response.status.value}: $body"
        }

        return requireNotNull(json.parseToJsonElement(body).jsonObject["access_token"]?.jsonPrimitive?.contentOrNull) {
            "Keycloak response does not contain access_token: $body"
        }.also { accessToken = it }
    }

    private suspend fun HttpResponse.toE2eResponse(): E2eResponse =
        E2eResponse(status, bodyAsText())

    override fun close() = client.close()

    private fun requestId(body: String): String? = runCatching {
        json.parseToJsonElement(body).jsonObject["requestId"]?.jsonPrimitive?.contentOrNull
    }.getOrNull()

    private fun configuration(property: String, environment: String, default: String): String =
        System.getProperty(property)?.takeIf(String::isNotBlank)
            ?: System.getenv(environment)?.takeIf(String::isNotBlank)
            ?: default
}

internal data class E2eResponse(
    val status: HttpStatusCode,
    val body: String,
) {
    val json: JsonObject by lazy {
        Json.parseToJsonElement(body).jsonObject
    }
}

internal fun E2eResponse.assertSuccess(
    responseType: String,
    requestId: String,
): JsonObject {
    assertEquals(HttpStatusCode.OK, status, "Unexpected response: $body")
    assertEquals(responseType, json["responseType"]?.jsonPrimitive?.contentOrNull, body)
    assertEquals(requestId, json["requestId"]?.jsonPrimitive?.contentOrNull, body)
    assertEquals("success", json["result"]?.jsonPrimitive?.contentOrNull, body)
    return json
}

internal fun E2eResponse.assertValidationErrors(
    responseType: String,
    requestId: String,
    vararg expectedCodes: String,
): JsonObject {
    assertEquals(HttpStatusCode.OK, status, "Unexpected response: $body")
    assertEquals(responseType, json["responseType"]?.jsonPrimitive?.contentOrNull, body)
    assertEquals(requestId, json["requestId"]?.jsonPrimitive?.contentOrNull, body)
    assertEquals("error", json["result"]?.jsonPrimitive?.contentOrNull, body)
    val actualCodes = requireNotNull(json["errors"]?.jsonArray) { body }
        .mapNotNull { it.jsonObject["code"]?.jsonPrimitive?.contentOrNull }
        .toSet()
    assertEquals(expectedCodes.toSet(), actualCodes, body)
    return json
}
