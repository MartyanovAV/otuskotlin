package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.exceptions

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId

open class RepoClientCardException(
    @Suppress("unused")
    val clientCardId: ClientCardId,
    msg: String,
) : RepoException(msg)
