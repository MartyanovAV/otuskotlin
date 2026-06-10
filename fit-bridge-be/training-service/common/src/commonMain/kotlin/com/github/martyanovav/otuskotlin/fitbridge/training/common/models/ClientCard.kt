package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

data class ClientCard(
    var id: ClientCardId = ClientCardId.NONE,
    var trainerId: String = "",
    var displayName: String = "",
    var isArchived: Boolean = false,
    var note: String = "",
    var lock: String = "",
)
