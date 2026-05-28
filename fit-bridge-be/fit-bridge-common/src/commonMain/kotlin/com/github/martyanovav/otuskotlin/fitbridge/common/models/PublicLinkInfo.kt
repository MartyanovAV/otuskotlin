package com.github.martyanovav.otuskotlin.fitbridge.common.models

import kotlin.time.Instant

data class PublicLinkInfo(
    var publicUrl: String = "",
    var publicToken: String = "",
    var expiresAt: Instant = Instant.DISTANT_PAST,
)
