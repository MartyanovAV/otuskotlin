package com.github.martyanovav.otuskotlin.fitbridge.common.models

data class TrainingPlan(
    var id: TrainingPlanId = TrainingPlanId.NONE,
    var clientCardId: ClientCardId = ClientCardId.NONE,
    var trainerId: TrainerId = TrainerId.NONE,
    var title: String = "",
    var isArchived: Boolean = false,
)
