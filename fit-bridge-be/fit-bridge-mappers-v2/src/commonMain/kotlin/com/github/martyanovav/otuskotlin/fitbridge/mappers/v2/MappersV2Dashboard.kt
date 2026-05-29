package com.github.martyanovav.otuskotlin.fitbridge.mappers.v2

import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.DashboardGetTrainerSummaryRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.DashboardGetTrainerSummaryResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.DashboardSummaryResponseObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v2.models.ResponseResult
import com.github.martyanovav.otuskotlin.fitbridge.common.DashboardContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.DashboardCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.dashboard.DashboardSummary
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State

// ─── From Transport ──────────────────────────────────────────────────────────

internal fun DashboardContext.fromTransport(request: DashboardGetTrainerSummaryRequest) {
    command = DashboardCommand.GET_TRAINER_SUMMARY
    fromTransportBase(request.requestId, request.debug)
}

// ─── To Transport ────────────────────────────────────────────────────────────

internal fun DashboardContext.toTransportDashboardGetTrainerSummary() = DashboardGetTrainerSummaryResponse(
    requestId = requestId.takeIf { it != RequestId.NONE }?.asString(),
    result = if (state == State.RUNNING) ResponseResult.SUCCESS else ResponseResult.ERROR,
    errors = errors.toTransportErrors(),
    summary = dashboardSummaryResponse.toTransportDashboardSummary()
)

internal fun DashboardSummary.toTransportDashboardSummary(): DashboardSummaryResponseObject? {
    if (this == DashboardSummary()) return null
    return DashboardSummaryResponseObject(
        activeClientCards = activeClientCards.takeIf { it > 0 },
        archivedClientCards = archivedClientCards.takeIf { it > 0 },
        activeTrainingPlans = activeTrainingPlans.takeIf { it > 0 },
        activePublicLinks = activePublicLinks.takeIf { it > 0 }
    )
}
