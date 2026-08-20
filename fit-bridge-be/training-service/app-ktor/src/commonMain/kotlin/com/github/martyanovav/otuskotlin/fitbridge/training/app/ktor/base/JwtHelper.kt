package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

const val AUTH_HEADER: String = "x-jwt-payload"

fun String?.jwt2principal(): AuthPrincipal {
    if (isNullOrBlank()) return AuthPrincipal.NONE

    return runCatching {
        val paddedPayload = padEnd(length + (4 - length % 4) % 4, '=')
        val jwtJson = Base64.UrlSafe.decode(paddedPayload).decodeToString()
        jsonMapper.decodeFromString(JwtPayload.serializer(), jwtJson).toPrincipal()
    }.getOrElse { AuthPrincipal.NONE }
}

private val jsonMapper =
    Json {
        ignoreUnknownKeys = true
    }

@Serializable
private data class JwtPayload(
    val sub: String? = null,
    @SerialName("preferred_username")
    val preferredUsername: String? = null,
    @SerialName("given_name")
    val givenName: String? = null,
    val groups: List<String>? = null,
    @SerialName("realm_access")
    val realmAccess: RealmAccess? = null,
)

@Serializable
private data class RealmAccess(
    val roles: List<String> = emptyList(),
)

private fun JwtPayload.toPrincipal(): AuthPrincipal =
    AuthPrincipal(
        userId = sub ?: "",
        username = preferredUsername ?: givenName ?: "",
        roles = (groups.orEmpty() + realmAccess?.roles.orEmpty()).toSet(),
    )
