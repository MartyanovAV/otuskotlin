package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

@JvmInline
value class TrainingPlanId(private val id: String) {
    fun asString() = id

    companion object {
        val NONE = TrainingPlanId("")
    }
}
