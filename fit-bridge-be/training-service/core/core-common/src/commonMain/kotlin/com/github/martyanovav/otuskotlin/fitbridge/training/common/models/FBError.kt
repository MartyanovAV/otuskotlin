package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.LogLevel

data class FBError(
    val code: String = "",
    val group: String = "",
    val field: String = "",
    val message: String = "",
    val level: LogLevel = LogLevel.ERROR,
    val exception: Throwable? = null,
)
