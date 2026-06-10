package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

data class TrainingPlanFilter(
    var clientCardId: ClientCardId = ClientCardId.NONE,
    var status: String = "",
    var searchString: String = "",
    var pageNumber: Int = 1,
    var pageSize: Int = 10,
)
