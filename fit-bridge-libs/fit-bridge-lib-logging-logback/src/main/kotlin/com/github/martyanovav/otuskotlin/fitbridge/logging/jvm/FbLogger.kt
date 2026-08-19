package com.github.martyanovav.otuskotlin.fitbridge.logging.jvm

import ch.qos.logback.classic.Logger
import com.github.martyanovav.otuskotlin.fitbridge.logging.common.IFbLogWrapper
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Generate internal MpLogContext logger
 *
 * @param logger Logback instance from [LoggerFactory.getLogger()]
 */
@Suppress("FunctionName")
fun FbLoggerLogback(logger: Logger): IFbLogWrapper =
    FbLogWrapperLogback(
        logger = logger,
        loggerId = logger.name,
    )

@Suppress("FunctionName")
fun FbLoggerLogback(clazz: KClass<*>): IFbLogWrapper = FbLoggerLogback(LoggerFactory.getLogger(clazz.java) as Logger)

@Suppress("unused", "FunctionName")
fun FbLoggerLogback(loggerId: String): IFbLogWrapper = FbLoggerLogback(LoggerFactory.getLogger(loggerId) as Logger)
