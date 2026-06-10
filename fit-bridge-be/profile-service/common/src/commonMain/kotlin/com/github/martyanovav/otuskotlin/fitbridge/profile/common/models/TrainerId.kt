package com.github.martyanovav.otuskotlin.fitbridge.profile.common.models

import kotlin.jvm.JvmInline

@JvmInline
value class TrainerId(private val id: String) {
    fun asString() = id

    companion object {
        val NONE = TrainerId("")
    }
}
