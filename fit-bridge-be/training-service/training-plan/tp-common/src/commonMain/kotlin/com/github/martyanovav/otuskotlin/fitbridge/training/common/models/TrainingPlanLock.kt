package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

@JvmInline
value class TrainingPlanLock(private val lock: String) {
    fun asString() = lock

    companion object {
        val NONE = TrainingPlanLock("")
    }
}
