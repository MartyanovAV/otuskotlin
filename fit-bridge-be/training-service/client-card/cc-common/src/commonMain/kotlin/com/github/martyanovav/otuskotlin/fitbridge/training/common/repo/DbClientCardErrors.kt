package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError

fun errorNotFoundClientCard(id: ClientCardId) =
    DbClientCardResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-not-found",
            group = ERROR_GROUP_REPO,
            field = "id",
            message = "ClientCard with ID: ${id.asString()} is not Found",
        ),
    )

val errorEmptyClientCardId =
    DbClientCardResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-empty-id",
            group = ERROR_GROUP_REPO,
            field = "id",
            message = "ClientCard Id must not be null or blank",
        ),
    )

fun errorEmptyClientCardLock(id: ClientCardId) =
    DbClientCardResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-empty-lock",
            group = ERROR_GROUP_REPO,
            field = "lock",
            message = "ClientCard lock is empty for id: ${id.asString()}",
        ),
    )

fun errorRepoConcurrencyClientCard(card: ClientCard, oldLock: ClientCardLock) =
    DbClientCardResponseErrWithData(
        card = card,
        err =
            FBError(
                code = "$ERROR_GROUP_REPO-concurrency",
                group = ERROR_GROUP_REPO,
                field = "lock",
                message = "ClientCard concurrent modification detected for id: ${card.id.asString()}",
            ),
    )

fun errorRepoDbClientCard(e: Throwable) =
    DbClientCardResponseErr(
        FBError(
            code = "$ERROR_GROUP_REPO-db-error",
            group = ERROR_GROUP_REPO,
            field = "db",
            message = "Database error occurred",
            exception = e,
        ),
    )
