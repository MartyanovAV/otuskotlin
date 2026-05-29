package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardArchiveRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardArchiveResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardListRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardListResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
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
    clientCardRequest = ClientCard(
        displayName = request.clientCard?.displayName.orEmpty(),
        note = request.clientCard?.note.orEmpty()
    )
}

internal fun ClientCardContext.fromTransport(request: ClientCardReadRequest) {
    command = ClientCardCommand.READ
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = ClientCard(
        id = request.clientCard?.id.toClientCardId()
    )
}

internal fun ClientCardContext.fromTransport(request: ClientCardUpdateRequest) {
    command = ClientCardCommand.UPDATE
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = ClientCard(
        id = request.clientCard?.id.toClientCardId(),
        displayName = request.clientCard?.displayName.orEmpty(),
        note = request.clientCard?.note.orEmpty(),
        lock = request.clientCard?.lock.orEmpty()
    )
}

internal fun ClientCardContext.fromTransport(request: ClientCardArchiveRequest) {
    command = ClientCardCommand.ARCHIVE
    fromTransportBase(request.requestId, request.debug)
    clientCardRequest = ClientCard(
        id = request.clientCard?.id.toClientCardId(),
        lock = request.clientCard?.lock.orEmpty()
    )
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
