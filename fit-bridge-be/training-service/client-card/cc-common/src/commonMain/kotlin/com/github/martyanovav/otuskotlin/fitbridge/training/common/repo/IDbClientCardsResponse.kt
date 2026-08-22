package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page

sealed interface IDbClientCardsResponse : IDbResponse<Page<ClientCard>>

data class DbClientCardsResponseOk(
    val data: Page<ClientCard>,
) : IDbClientCardsResponse

@Suppress("unused")
data class DbClientCardsResponseErr(
    val errors: List<FBError> = emptyList(),
) : IDbClientCardsResponse {
    constructor(err: FBError) : this(listOf(err))
}
