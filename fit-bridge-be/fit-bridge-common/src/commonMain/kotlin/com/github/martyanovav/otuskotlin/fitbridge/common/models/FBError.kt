package com.github.martyanovav.otuskotlin.fitbridge.common.models

data class FBError(
    val code: String = "",
    val group: String = "",
    val field: String = "",
    val message: String = "",
    val exception: Throwable? = null,
)
