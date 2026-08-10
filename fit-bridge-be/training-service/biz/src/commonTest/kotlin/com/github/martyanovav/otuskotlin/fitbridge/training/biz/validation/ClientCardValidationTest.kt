package com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation

import com.github.martyanovav.otuskotlin.fitbridge.training.biz.TrainingProcessor
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClientCardValidationTest {
    private val processor = TrainingProcessor(CorSettings())

    @Test
    fun validCreateRequestIsNormalizedAndValidated() = runTest {
        val request = ClientCard(displayName = "  Анна  ", note = "  Заметка  ")
        val ctx = ClientCardContext(
            command = ClientCardCommand.CREATE,
            workMode = WorkMode.TEST,
            clientCardRequest = request,
        )

        processor.exec(ctx)

        assertEquals(State.RUNNING, ctx.state)
        assertTrue(ctx.errors.isEmpty())
        assertEquals("Анна", ctx.clientCardValidated.displayName)
        assertEquals("Заметка", ctx.clientCardValidated.note)
        assertEquals("  Анна  ", request.displayName, "The source request must remain unchanged")
    }

    @Test
    fun requiredUpdateFieldsAreValidatedTogether() = runTest {
        val ctx = ClientCardContext(
            command = ClientCardCommand.UPDATE,
            workMode = WorkMode.TEST,
            clientCardRequest = ClientCard(),
        )

        processor.exec(ctx)

        assertEquals(State.FAILING, ctx.state)
        assertEquals(
            setOf("validation-id-empty", "validation-displayName-empty", "validation-lock-empty"),
            ctx.errors.map { it.code }.toSet(),
        )
    }

    @Test
    fun searchFilterIsValidated() = runTest {
        val ctx = ClientCardContext(
            command = ClientCardCommand.SEARCH,
            workMode = WorkMode.TEST,
            clientCardFilter = ClientCardFilter(
                status = "unknown",
                searchString = "a".repeat(121),
                pageNumber = 0,
                pageSize = 101,
            ),
        )

        processor.exec(ctx)

        assertEquals(State.FAILING, ctx.state)
        assertEquals(
            setOf(
                "validation-searchString-tooLong",
                "validation-status-unsupported",
                "validation-pageNumber-outOfRange",
                "validation-pageSize-outOfRange",
            ),
            ctx.errors.map { it.code }.toSet(),
        )
    }
}
