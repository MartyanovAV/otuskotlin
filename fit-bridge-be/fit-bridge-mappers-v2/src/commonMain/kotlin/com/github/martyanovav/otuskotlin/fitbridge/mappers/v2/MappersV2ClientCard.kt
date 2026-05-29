package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardArchiveObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardCreateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardListRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardListResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardReadObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ClientCardUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State

// ─── From Transport ──────────────────────────────────────────────────────────

internal fun ClientCardContext.fromTransport(request: ClientCardCreateRequest) {
    command = ClientCardCommand.CREATE
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = request.clientCard.toInternal()
}

internal fun ClientCardContext.fromTransport(request: ClientCardReadRequest) {
    command = ClientCardCommand.READ
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = request.clientCard.toInternal()
}

internal fun ClientCardContext.fromTransport(request: ClientCardUpdateRequest) {
    command = ClientCardCommand.UPDATE
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = request.clientCard.toInternal()
}

internal fun ClientCardContext.fromTransport(request: ClientCardArchiveRequest) {
    command = ClientCardCommand.ARCHIVE
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = request.clientCard.toInternal()
}

internal fun ClientCardContext.fromTransport(request: ClientCardListRequest) {
    command = ClientCardCommand.LIST
    fromTransportBase(request.requestId, request.debug)
}

// ─── To Transport ────────────────────────────────────────────────────────────

internal fun ClientCardContext.toTransportClientCardCreate() = ClientCardCreateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

internal fun ClientCardContext.toTransportClientCardRead() = ClientCardReadResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

internal fun ClientCardContext.toTransportClientCardUpdate() = ClientCardUpdateResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

internal fun ClientCardContext.toTransportClientCardArchive() = ClientCardArchiveResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCard = clientCardResponse.toTransportClientCard()
)

internal fun ClientCardContext.toTransportClientCardList() = ClientCardListResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    clientCards = clientCardsResponse.mapNotNull { it.toTransportClientCard() }.takeIf { it.isNotEmpty() }
)

internal fun ClientCard.toTransportClientCard(): ClientCardResponseObject? {
    if (this == ClientCard()) return null
    return ClientCardResponseObject(
        id = id.takeIf { it != ClientCardId.NONE }?.asString(),
        displayName = displayName.takeIf { it.isNotBlank() },
        note = note.takeIf { it.isNotBlank() },
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
