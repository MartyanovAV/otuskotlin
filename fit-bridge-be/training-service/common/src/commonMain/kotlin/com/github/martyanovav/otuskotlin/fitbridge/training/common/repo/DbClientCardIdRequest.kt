package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId

data class DbClientCardIdRequest(
    val id: ClientCardId,
) {
    constructor(card: ClientCard) : this(card.id)
}
