package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

data class TrainingPlan(
    var id: TrainingPlanId = TrainingPlanId.NONE,
    var clientCardId: ClientCardId = ClientCardId.NONE,
    var ownerId: String = "",
    var title: String = "",
    var isArchived: Boolean = false,
    var lock: String = "",
    var planItems: MutableList<PlanItem> = mutableListOf(),
)
