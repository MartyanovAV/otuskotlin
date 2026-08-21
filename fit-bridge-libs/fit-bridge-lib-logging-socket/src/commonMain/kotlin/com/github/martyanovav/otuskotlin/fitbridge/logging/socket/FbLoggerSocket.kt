package com.github.martyanovav.otuskotlin.fitbridge.logging.socket

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.IFbLogWrapper
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlin.reflect.KClass

data class SocketLoggerSettings(
    val host: String = "127.0.0.1",
    val port: Int = 9002,
    val emitToStdout: Boolean = true,
    val bufferSize: Int = 16,
    val overflowPolicy: BufferOverflow = BufferOverflow.SUSPEND,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + CoroutineName("Logging")),
)

@OptIn(ExperimentalStdlibApi::class)
@Suppress("unused", "FunctionName")
fun FbLoggerSocket(
    loggerId: String,
    settings: SocketLoggerSettings = SocketLoggerSettings()
): IFbLogWrapper =
    FbLoggerWrapperSocket(
        loggerId = loggerId,
        host = settings.host,
        port = settings.port,
        emitToStdout = settings.emitToStdout,
        bufferSize = settings.bufferSize,
        overflowPolicy = settings.overflowPolicy,
        scope = settings.scope,
    )

@Suppress("unused", "FunctionName")
fun FbLoggerSocket(cls: KClass<*>, settings: SocketLoggerSettings = SocketLoggerSettings()): IFbLogWrapper =
    FbLoggerSocket(
        loggerId = cls.qualifiedName.orEmpty(),
        settings = settings,
    )
