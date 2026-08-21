package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock

abstract class BaseInitClientCards(private val op: String) : IInitObjects<ClientCard> {
    fun createInitTestModel(
        suf: String,
        ownerUserId: String = "owner-123",
    ) = ClientCard(
        id = ClientCardId("cc-repo-$op-$suf"),
        ownerUserId = ownerUserId,
        createdByUserId = ownerUserId,
        displayName = "$suf stub",
        note = "$suf stub description",
        lock = ClientCardLock("lock-$op-$suf"),
    )
}
