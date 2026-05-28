package com.github.martyanovav.otuskotlin.fitbridge.common.models

import kotlin.jvm.JvmInline

@JvmInline
value class TrainingPlanId(private val id: String) {
    fun asString() = id

    companion object {
        val NONE = TrainingPlanId("")
    }
}
