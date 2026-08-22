package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError

sealed interface IDbClientCardResponse : IDbResponse<ClientCard>

data class DbClientCardResponseOk(
    val data: ClientCard,
) : IDbClientCardResponse

data class DbClientCardResponseErr(
    val errors: List<FBError> = emptyList(),
) : IDbClientCardResponse {
    constructor(err: FBError) : this(listOf(err))
}

data class DbClientCardResponseErrWithData(
    val data: ClientCard,
    val errors: List<FBError> = emptyList(),
) : IDbClientCardResponse {
    constructor(card: ClientCard, err: FBError) : this(card, listOf(err))
}
