package com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan

import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerId

data class TrainingPlan(
    var id: TrainingPlanId = TrainingPlanId.NONE,
    var clientCardId: ClientCardId = ClientCardId.NONE,
    var trainerId: TrainerId = TrainerId.NONE,
    var title: String = "",
    var isArchived: Boolean = false,
    var lock: String = "",
    var planItems: MutableList<PlanItem> = mutableListOf(),
)
