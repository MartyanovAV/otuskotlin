package com.github.martyanovav.otuskotlin.fitbridge.logging.socket

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.LineEnding
import io.ktor.utils.io.readLineStrict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlin.io.println
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.use

class SocketLoggerTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun socketTest() = runTest(timeout = 3.seconds) {
        val port = Random.nextInt(9000, 12000)
        val loggerSettings = SocketLoggerSettings(port = port)
        withContext(Dispatchers.Default) {
            // Prepare Server
            val selectorManager = SelectorManager(Dispatchers.IO)
            val serverJob = launch {
                aSocket(selectorManager).tcp().bind("127.0.0.1", port).use { serverSocket ->
                    // Ожидаем 100 лог-сообщений здесь в течение 3 секунд. Иначе ошибка.
                    serverSocket.accept().use { socket ->
                        flow<String> {
                            val receiveChannel = socket.openReadChannel()
                            while (true) {
                                receiveChannel.readLineStrict(limit = 8L * 1024, lineEnding = LineEnding.Lenient)?.let { emit(it) }
                            }
                        }.take(100).collect {
                            println("GOT: $it")
                        }
                    }
                }
            }
            // Prepare Logger
            mpLoggerSocket("test", loggerSettings).use { logger ->
                // Wait for logger is ready
                while ((logger as? MpLoggerWrapperSocket)?.isReady?.value != true) {
                    delay(1)
                }
                coroutineScope {
                    launch {
                        repeat(100) {
                            logger.info(
                                msg = "Test message $it",
                                marker = "TST",
                                data = object {
                                    @Suppress("unused")
                                    val str: String = "one"

                                    @Suppress("unused")
                                    val iVal: Int = 2
                                }
                            )
                        }
                    }
                }
            }
            serverJob.cancelAndJoin()
        }
    }
}
