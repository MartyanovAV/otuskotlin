package com.github.martyanovav.otuskotlin.fitbridge.logging.socket

import kotlinx.serialization.Serializable
import com.github.martyanovav.otuskotlin.fitbridge.logging.common.LogLevel

@Serializable
data class LogData(
    val level: LogLevel,
    val message: String,
//    val data: T
)
