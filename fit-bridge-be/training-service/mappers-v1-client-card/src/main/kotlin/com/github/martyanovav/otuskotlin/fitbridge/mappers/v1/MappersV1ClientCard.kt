package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardArchiveObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardCreateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchFilter
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardStatus as ClientCardStatusV1


// ─── From Transport ──────────────────────────────────────────────────────────

fun ClientCardCreateRequest.fromTransport(): ClientCardContext = ClientCardContext().apply { fromTransport(this@fromTransport) }
fun ClientCardReadRequest.fromTransport(): ClientCardContext = ClientCardContext().apply { fromTransport(this@fromTransport) }
fun ClientCardUpdateRequest.fromTransport(): ClientCardContext = ClientCardContext().apply { fromTransport(this@fromTransport) }
fun ClientCardArchiveRequest.fromTransport(): ClientCardContext = ClientCardContext().apply { fromTransport(this@fromTransport) }
fun ClientCardSearchRequest.fromTransport(): ClientCardContext = ClientCardContext().apply { fromTransport(this@fromTransport) }

fun ClientCardContext.toTransport(): Any = when (command) {
    ClientCardCommand.CREATE -> toTransportClientCardCreate()
    ClientCardCommand.READ -> toTransportClientCardRead()
    ClientCardCommand.UPDATE -> toTransportClientCardUpdate()
    ClientCardCommand.ARCHIVE -> toTransportClientCardArchive()
    ClientCardCommand.SEARCH -> toTransportClientCardSearch()
    FBCommandBase.NONE -> toTransportInit()
    FBCommandBase.INIT -> toTransportInit()
    else -> throw IllegalArgumentException("Unsupported client card command $command")
}

fun ClientCardContext.fromTransport(request: ClientCardCreateRequest) {
    command = ClientCardCommand.CREATE
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = request.clientCard.toInternal()
}

fun ClientCardContext.fromTransport(request: ClientCardReadRequest) {
    command = ClientCardCommand.READ
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = request.clientCard.toInternal()
}

fun ClientCardContext.fromTransport(request: ClientCardUpdateRequest) {
    command = ClientCardCommand.UPDATE
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = request.clientCard.toInternal()
}

fun ClientCardContext.fromTransport(request: ClientCardArchiveRequest) {
    command = ClientCardCommand.ARCHIVE
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = request.clientCard.toInternal()
}

fun ClientCardContext.fromTransport(request: ClientCardSearchRequest) {
    command = ClientCardCommand.SEARCH
    fromTransportBase(request.requestId, request.debug)
    clientCardFilter = request.clientCardFilter.toInternal()
    clientCardsResponse = Page(pageNumber = clientCardFilter.pageNumber, pageSize = clientCardFilter.pageSize)
}

private fun ClientCardSearchFilter?.toInternal() = ClientCardFilter(
    status = this?.status.toClientCardStatus(),
    searchString = this?.searchString.orEmpty(),
    pageNumber = this?.pageNumber ?: 1,
    pageSize = this?.pageSize ?: 10,
)

private fun ClientCardStatusV1?.toClientCardStatus() = when (this) {
    ClientCardStatusV1.ACTIVE -> ClientCardStatus.ACTIVE
    ClientCardStatusV1.ARCHIVED -> ClientCardStatus.ARCHIVED
    null -> ClientCardStatus.NONE
}

// ─── To Transport ────────────────────────────────────────────────────────────

fun ClientCardContext.toTransportClientCardCreate() = ClientCardCreateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

fun ClientCardContext.toTransportClientCardRead() = ClientCardReadResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

fun ClientCardContext.toTransportClientCardUpdate() = ClientCardUpdateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

fun ClientCardContext.toTransportClientCardArchive() = ClientCardArchiveResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

fun ClientCardContext.toTransportClientCardSearch() = ClientCardSearchResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING || state == State.FINISHING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCards = clientCardsResponse.items.mapNotNull { it.toTransportClientCard() }.takeIf { it.isNotEmpty() },
    totalSize = clientCardsResponse.totalSize,
    pageNumber = clientCardsResponse.pageNumber.takeIf { it > 0 },
    pageSize = clientCardsResponse.pageSize.takeIf { it > 0 },
)

internal fun ClientCard.toTransportClientCard(): ClientCardResponseObject? {
    if (this == ClientCard()) return null
    return ClientCardResponseObject(
        id = id.takeIf { it != ClientCardId.NONE }?.asString(),
        displayName = displayName.takeIf { it.isNotBlank() },
        note = note.takeIf { it.isNotBlank() },
        status = when (isArchived) {
            true -> ClientCardStatusV1.ARCHIVED
            false -> ClientCardStatusV1.ACTIVE
        },
        createdAt = createdAt.takeIf { it.isNotBlank() },
        updatedAt = updatedAt.takeIf { it.isNotBlank() },
        lock = lock.takeIf { it.isNotBlank() }
    )
}

// ─── Private: Request DTO to Internal ────────────────────────────────────────

private fun ClientCardCreateObject?.toInternal() = ClientCard(
    displayName = this?.displayName.orEmpty(),
    note = this?.note.orEmpty()
)

private fun ClientCardReadObject?.toInternal() = ClientCard(
    id = this?.id.toClientCardId()
)

private fun ClientCardUpdateObject?.toInternal() = ClientCard(
    id = this?.id.toClientCardId(),
    displayName = this?.displayName.orEmpty(),
    note = this?.note.orEmpty(),
    lock = this?.lock.orEmpty()
)

private fun ClientCardArchiveObject?.toInternal() = ClientCard(
    id = this?.id.toClientCardId(),
    lock = this?.lock.orEmpty()
)
