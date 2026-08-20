package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.AUTH_HEADER
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

class RestAuthTest {
    @Test
    fun v1RestPropagatesPrincipal() = assertRestPrincipal("v1")

    @Test
    fun v2RestPropagatesPrincipal() = assertRestPrincipal("v2")

    private fun assertRestPrincipal(version: String) =
        testApplication {
            application { moduleJvm(AppSettings()) }
            val requestBody =
                """{"requestType":"clientCard.create","requestId":"auth-$version","clientCard":{"displayName":"Auth $version"}}"""

            val authorized =
                client.post("/$version/client-card/create") {
                    contentType(ContentType.Application.Json)
                    header(AUTH_HEADER, principalHeader("user-$version"))
                    setBody(requestBody)
                }
            val anonymous =
                client.post("/$version/client-card/create") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.replace("auth-$version", "anonymous-$version"))
                }

            assertEquals("success", resultOf(authorized.bodyAsText()))
            assertEquals("error", resultOf(anonymous.bodyAsText()))
        }

    private fun principalHeader(userId: String): String {
        val payload = """{"sub":"$userId","given_name":"Test","groups":["TRAINER"]}"""
        return Base64.UrlSafe.encode(payload.encodeToByteArray()).trimEnd('=')
    }

    private fun resultOf(response: String): String =
        Json.parseToJsonElement(response).jsonObject.getValue("result").jsonPrimitive.content
}
