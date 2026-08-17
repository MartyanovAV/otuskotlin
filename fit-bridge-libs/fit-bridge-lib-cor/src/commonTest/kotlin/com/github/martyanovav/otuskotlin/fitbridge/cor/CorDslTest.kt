package com.github.martyanovav.otuskotlin.fitbridge.cor

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CorDslTest {
    private data class TestContext(
        var status: Status = Status.NONE,
        var history: String = "",
    ) {
        enum class Status { NONE, RUNNING, ERROR }
    }

    @Test
    fun `chain executes matching workers in order`() = runTest {
        val chain = rootChain<TestContext> {
            worker("start") {
                history += "start; "
                status = TestContext.Status.RUNNING
            }
            worker {
                on { status == TestContext.Status.RUNNING }
                handle { history += "process; " }
            }
        }.build()

        val context = TestContext()
        chain.exec(context)

        assertEquals("start; process; ", context.history)
    }

    @Test
    fun `except handles worker exception`() = runTest {
        val chain = rootChain<TestContext> {
            worker {
                handle { error("failure") }
                except {
                    history = it.message.orEmpty()
                    status = TestContext.Status.ERROR
                }
            }
        }.build()

        val context = TestContext()
        chain.exec(context)

        assertEquals("failure", context.history)
        assertEquals(TestContext.Status.ERROR, context.status)
    }

    @Test
    fun `exception is propagated without except handler`() = runTest {
        val chain = rootChain<TestContext> {
            worker("failure") { error("failure") }
        }.build()

        assertFailsWith<IllegalStateException> { chain.exec(TestContext()) }
    }
}
