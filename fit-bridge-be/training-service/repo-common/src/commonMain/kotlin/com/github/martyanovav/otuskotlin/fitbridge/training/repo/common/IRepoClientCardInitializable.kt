package com.github.martyanovav.otuskotlin.fitbridge.training.repo.common

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard

interface IRepoClientCardInitializable : IRepoClientCard {
    fun save(cards: Collection<ClientCard>): Collection<ClientCard>
}
