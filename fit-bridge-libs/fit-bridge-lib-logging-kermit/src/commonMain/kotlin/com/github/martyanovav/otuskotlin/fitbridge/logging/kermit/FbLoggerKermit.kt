package com.github.martyanovav.otuskotlin.fitbridge.logging.kermit

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import com.github.martyanovav.otuskotlin.fitbridge.logging.common.IFbLogWrapper
import kotlin.reflect.KClass

@Suppress("unused", "FunctionName")
fun FbLoggerKermit(loggerId: String): IFbLogWrapper {
    val logger =
        Logger(
            config =
                StaticConfig(
                    minSeverity = Severity.Info,
                ),
            tag = "DEV"
        )
    return FbLoggerWrapperKermit(
        logger = logger,
        loggerId = loggerId,
    )
}

@Suppress("unused", "FunctionName")
fun FbLoggerKermit(cls: KClass<*>): IFbLogWrapper {
    val logger =
        Logger(
            config =
                StaticConfig(
                    minSeverity = Severity.Info,
                ),
            tag = "DEV"
        )
    return FbLoggerWrapperKermit(
        logger = logger,
        loggerId = cls.qualifiedName ?: "",
    )
}
