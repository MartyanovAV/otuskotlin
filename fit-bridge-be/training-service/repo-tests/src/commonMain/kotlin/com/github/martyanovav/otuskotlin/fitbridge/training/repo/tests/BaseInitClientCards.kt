package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId

abstract class BaseInitClientCards(private val op: String) : IInitObjects<ClientCard> {
    fun createInitTestModel(
        suf: String,
        ownerId: String = "owner-123",
    ) = ClientCard(
        id = ClientCardId("cc-repo-$op-$suf"),
        ownerId = ownerId,
        displayName = "$suf stub",
        note = "$suf stub description",
    )
}
