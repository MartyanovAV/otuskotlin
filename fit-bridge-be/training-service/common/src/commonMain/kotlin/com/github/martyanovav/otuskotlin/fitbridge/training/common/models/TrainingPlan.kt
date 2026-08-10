package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

data class TrainingPlan(
    var id: TrainingPlanId = TrainingPlanId.NONE,
    var clientCardId: ClientCardId = ClientCardId.NONE,
    var ownerId: String = "",
    var title: String = "",
    var status: TrainingPlanStatus = TrainingPlanStatus.ACTIVE,
    var lock: String = "",
    var planItems: MutableList<PlanItem> = mutableListOf(),
) {
    fun deepCopy(): TrainingPlan = copy(
        planItems = planItems.map { it.deepCopy() }.toMutableList(),
    )
}
