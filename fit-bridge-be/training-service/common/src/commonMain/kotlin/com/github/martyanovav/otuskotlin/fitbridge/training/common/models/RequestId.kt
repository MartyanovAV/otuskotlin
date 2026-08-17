package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

@JvmInline
value class RequestId(private val id: String) {
    fun asString() = id

    companion object {
        val NONE = RequestId("")
    }
}
