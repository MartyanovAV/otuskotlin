package com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan

import kotlin.time.Instant

data class CompletionMarkRequest(
    var itemRef: String = "",
    var status: String = "",
    var completedAt: Instant = Instant.DISTANT_PAST,
    var clientComment: String = "",
)
