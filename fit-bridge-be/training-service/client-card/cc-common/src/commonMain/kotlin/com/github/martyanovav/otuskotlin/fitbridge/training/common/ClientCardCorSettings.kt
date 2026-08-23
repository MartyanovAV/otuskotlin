package com.github.martyanovav.otuskotlin.fitbridge.training.common

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.FbLoggerProvider
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard

data class ClientCardCorSettings(
    val loggerProvider: FbLoggerProvider = FbLoggerProvider(),
    val repoClientCardStub: IRepoClientCard = IRepoClientCard.NONE,
    val repoClientCardTest: IRepoClientCard = IRepoClientCard.NONE,
    val repoClientCardProd: IRepoClientCard = IRepoClientCard.NONE,
)
