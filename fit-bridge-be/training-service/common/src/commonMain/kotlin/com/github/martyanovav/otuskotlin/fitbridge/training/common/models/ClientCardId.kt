package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

import kotlin.jvm.JvmInline

@JvmInline
value class ClientCardId(private val id: String) {
    fun asString() = id

    companion object {
        val NONE = ClientCardId("")
    }
}
