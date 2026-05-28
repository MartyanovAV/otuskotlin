package com.github.martyanovav.otuskotlin.fitbridge.common.models

data class DashboardSummary(
    var activeClientCards: Int = 0,
    var archivedClientCards: Int = 0,
    var activeTrainingPlans: Int = 0,
    var activePublicLinks: Int = 0,
)
