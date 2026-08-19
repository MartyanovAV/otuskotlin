package com.github.martyanovav.otuskotlin.fitbridge.training.repo.common

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard

class RepoClientCardInitialized(
    val repo: IRepoClientCardInitializable,
    initObjects: Collection<ClientCard> = emptyList(),
) : IRepoClientCardInitializable by repo {
    @Suppress("unused")
    val initializedObjects: List<ClientCard> = save(initObjects).toList()
}
