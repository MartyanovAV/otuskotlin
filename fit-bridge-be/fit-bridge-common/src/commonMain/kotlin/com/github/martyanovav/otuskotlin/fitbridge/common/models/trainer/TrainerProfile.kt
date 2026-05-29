package com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer

data class TrainerProfile(
    var id: TrainerId = TrainerId.NONE,
    var publicName: String = "",
    var specialization: String = "",
)
