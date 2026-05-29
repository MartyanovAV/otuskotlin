package com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard

import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerId
import kotlin.jvm.JvmInline

@JvmInline
value class ClientCardId(private val id: String) {
    fun asString() = id

    companion object {
        val NONE = ClientCardId("")
    }
}
