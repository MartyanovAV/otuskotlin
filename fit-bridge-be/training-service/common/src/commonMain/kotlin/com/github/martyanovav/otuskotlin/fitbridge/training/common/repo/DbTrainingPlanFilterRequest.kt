package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus

data class DbTrainingPlanFilterRequest(
    val clientCardId: ClientCardId = ClientCardId.NONE,
    val searchString: String = "",
    val status: TrainingPlanStatus = TrainingPlanStatus.NONE,
    val pageNumber: Int = 1,
    val pageSize: Int = 10,
)
