package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError

sealed interface IDbClientCardsResponse : IDbResponse<List<ClientCard>>

data class DbClientCardsResponseOk(
    val data: List<ClientCard>,
) : IDbClientCardsResponse

@Suppress("unused")
data class DbClientCardsResponseErr(
    val errors: List<FBError> = emptyList(),
) : IDbClientCardsResponse {
    constructor(err: FBError) : this(listOf(err))
}
