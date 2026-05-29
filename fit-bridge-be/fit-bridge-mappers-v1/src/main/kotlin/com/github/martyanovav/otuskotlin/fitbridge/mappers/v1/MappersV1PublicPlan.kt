package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.CompletionStatus
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicCompletionMarkResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicPlanMarkCompletionRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicPlanMarkCompletionResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicPlanOpenByTokenRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicPlanOpenByTokenResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.PublicPlanView
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.common.PublicPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.CompletionMarkInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.CompletionMarkRequest
import com.github.martyanovav.otuskotlin.fitbridge.common.models.PublicPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.publicplan.PublicPlanViewInfo
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State

// ─── From Transport ──────────────────────────────────────────────────────────

internal fun PublicPlanContext.fromTransport(request: PublicPlanOpenByTokenRequest) {
    command = PublicPlanCommand.OPEN_BY_TOKEN
    fromTransportBase(request.requestId, request.debug)
    tokenRequest = request.token.orEmpty()
}

internal fun PublicPlanContext.fromTransport(request: PublicPlanMarkCompletionRequest) {
    command = PublicPlanCommand.MARK_COMPLETION
    fromTransportBase(request.requestId, request.debug)
    tokenRequest = request.token.orEmpty()
    completionMarkRequest = CompletionMarkRequest(
        itemRef = request.completion?.itemRef.orEmpty(),
        status = request.completion?.status?.name.orEmpty(),
        completedAt = request.completion?.completedAt.toInstant(),
        clientComment = request.completion?.clientComment.orEmpty()
    )
}

// ─── To Transport ────────────────────────────────────────────────────────────

internal fun PublicPlanContext.toTransportPublicPlanOpenByToken() = PublicPlanOpenByTokenResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    publicPlan = publicPlanViewResponse.toTransportPublicPlanView()
)

internal fun PublicPlanContext.toTransportPublicPlanMarkCompletion() = PublicPlanMarkCompletionResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    completion = completionMarkResponse.toTransportCompletionMark()
)

internal fun PublicPlanViewInfo.toTransportPublicPlanView(): PublicPlanView? {
    if (this == PublicPlanViewInfo()) return null
    return PublicPlanView(
        trainerPublicName = trainerPublicName.takeIf { it.isNotBlank() },
        planTitle = planTitle.takeIf { it.isNotBlank() }
    )
}

internal fun CompletionMarkInfo.toTransportCompletionMark(): PublicCompletionMarkResponseObject? {
    if (this == CompletionMarkInfo()) return null
    return PublicCompletionMarkResponseObject(
        itemRef = itemRef.takeIf { it.isNotBlank() },
        status = status.takeIf { it.isNotBlank() }?.let { CompletionStatus.valueOf(it) }
    )
}
