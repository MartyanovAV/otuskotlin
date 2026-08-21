package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

@JvmInline
value class ClientCardLock(private val lock: String) {
    fun asString() = lock

    companion object {
        val NONE = ClientCardLock("")
    }
}
