package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock

data class DbClientCardIdRequest(
    val id: ClientCardId,
    val lock: ClientCardLock = ClientCardLock.NONE,
) {
    @Deprecated("Use constructor with explicit lock to avoid empty lock errors")
    constructor(id: ClientCardId) : this(id, ClientCardLock.NONE)

    constructor(card: ClientCard) : this(card.id, card.lock)
}
