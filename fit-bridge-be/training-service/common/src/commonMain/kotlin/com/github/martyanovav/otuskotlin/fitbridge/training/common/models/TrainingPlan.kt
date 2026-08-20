package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

data class TrainingPlan(
    var id: TrainingPlanId = TrainingPlanId.NONE,
    var clientCardId: ClientCardId = ClientCardId.NONE,
    var ownerUserId: String = "",
    var createdByUserId: String = "",
    var title: String = "",
    var status: TrainingPlanStatus = TrainingPlanStatus.ACTIVE,
    var lock: TrainingPlanLock = TrainingPlanLock.NONE,
    var planItems: List<PlanItem> = emptyList(),
    var version: Int = 1,
    var createdAt: String = "",
    var updatedAt: String = "",
) {
    fun deepCopy(): TrainingPlan =
        copy(
            planItems = planItems.map { it.deepCopy() },
        )
}
