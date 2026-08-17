package com.github.martyanovav.otuskotlin.fitbridge.logging.socket

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.LogLevel
import kotlinx.serialization.Serializable

@Serializable
data class LogData(
    val level: LogLevel,
    val message: String,
//    val data: T
)
