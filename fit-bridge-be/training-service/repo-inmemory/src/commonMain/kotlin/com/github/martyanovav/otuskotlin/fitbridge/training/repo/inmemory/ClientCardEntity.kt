package com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock

data class ClientCardEntity(
    val id: String? = null,
    val ownerUserId: String? = null,
    val createdByUserId: String? = null,
    val displayName: String? = null,
    val isArchived: Boolean = false,
    val note: String? = null,
    val lock: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    constructor(model: ClientCard) : this(
        id = model.id.asString().takeIf { it.isNotBlank() },
        ownerUserId = model.ownerUserId.takeIf { it.isNotBlank() },
        createdByUserId = model.createdByUserId.takeIf { it.isNotBlank() },
        displayName = model.displayName.takeIf { it.isNotBlank() },
        isArchived = model.isArchived,
        note = model.note.takeIf { it.isNotBlank() },
        lock = model.lock.asString().takeIf { it.isNotBlank() },
        createdAt = model.createdAt.takeIf { it.isNotBlank() },
        updatedAt = model.updatedAt.takeIf { it.isNotBlank() },
    )

    fun toInternal() =
        ClientCard(
            id = id?.let { ClientCardId(it) } ?: ClientCardId.NONE,
            ownerUserId = ownerUserId ?: "",
            createdByUserId = createdByUserId ?: "",
            displayName = displayName ?: "",
            isArchived = isArchived,
            note = note ?: "",
            lock = ClientCardLock(lock ?: ""),
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: "",
        )
}
