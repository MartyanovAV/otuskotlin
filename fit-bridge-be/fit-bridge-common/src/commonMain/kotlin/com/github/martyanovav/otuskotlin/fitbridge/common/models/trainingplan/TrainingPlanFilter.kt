package com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan

import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId

data class TrainingPlanFilter(
    var clientCardId: ClientCardId = ClientCardId.NONE,
    var status: String = "",
    var searchString: String = "",
    var pageNumber: Int = 1,
    var pageSize: Int = 10,
)
