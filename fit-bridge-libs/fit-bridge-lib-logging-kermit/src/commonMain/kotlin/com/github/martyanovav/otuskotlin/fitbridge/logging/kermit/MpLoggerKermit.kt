package com.github.martyanovav.otuskotlin.fitbridge.logging.kermit

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import com.github.martyanovav.otuskotlin.fitbridge.logging.common.IMpLogWrapper
import kotlin.reflect.KClass

@Suppress("unused")
fun mpLoggerKermit(loggerId: String): IMpLogWrapper {
    val logger = Logger(
        config = StaticConfig(
            minSeverity = Severity.Info,
        ),
        tag = "DEV"
    )
    return MpLoggerWrapperKermit(
        logger = logger,
        loggerId = loggerId,
    )
}

@Suppress("unused")
fun mpLoggerKermit(cls: KClass<*>): IMpLogWrapper {
    val logger = Logger(
        config = StaticConfig(
            minSeverity = Severity.Info,
        ),
        tag = "DEV"
    )
    return MpLoggerWrapperKermit(
        logger = logger,
        loggerId = cls.qualifiedName ?: "",
    )
}
