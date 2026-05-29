package com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard

import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerId

data class ClientCard(
    var id: ClientCardId = ClientCardId.NONE,
    var trainerId: TrainerId = TrainerId.NONE,
    var displayName: String = "",
    var isArchived: Boolean = false,
    var note: String = "",
    var lock: String = "",
)
