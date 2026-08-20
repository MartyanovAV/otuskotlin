package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

class JwtHelperTest {
    @Test
    fun decodesUnpaddedEnvoyPayload() {
        val payload =
            """{"sub":"user-1","preferred_username":"anna","realm_access":{"roles":["TRAINER"]}}"""
        val header = Base64.UrlSafe.encode(payload.encodeToByteArray()).trimEnd('=')

        val principal = header.jwt2principal()

        assertEquals("user-1", principal.userId)
        assertEquals("anna", principal.username)
        assertEquals(setOf("TRAINER"), principal.roles)
    }

    @Test
    fun malformedPayloadProducesAnonymousPrincipal() {
        assertEquals(AuthPrincipal.NONE, "not-a-jwt-payload".jwt2principal())
    }
}
