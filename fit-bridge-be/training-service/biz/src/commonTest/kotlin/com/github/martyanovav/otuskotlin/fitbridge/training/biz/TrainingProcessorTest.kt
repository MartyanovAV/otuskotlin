package com.github.martyanovav.otuskotlin.fitbridge.training.biz

import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrainingProcessorTest {
    private val processor = TrainingProcessor(CorSettings())

    @Test
    fun allClientCardCommandsReturnSuccessStub() =
        runTest {
            ClientCardCommand.entries
                .filterNot { it == ClientCardCommand.NONE }
                .forEach { command ->
                    val ctx = ClientCardContext(command = command, workMode = WorkMode.STUB, stubCase = Stubs.SUCCESS)
                    processor.exec(ctx)
                    assertEquals(State.FINISHING, ctx.state, command.name)
                    if (command == ClientCardCommand.SEARCH) {
                        assertEquals(1, ctx.clientCardsResponse.totalSize, command.name)
                    } else {
                        assertTrue(ctx.clientCardResponse.displayName.isNotBlank(), command.name)
                    }
                }
        }

    @Test
    fun allTrainingPlanCommandsReturnSuccessStub() =
        runTest {
            TrainingPlanCommand.entries
                .filterNot { it == TrainingPlanCommand.NONE }
                .forEach { command ->
                    val ctx = TrainingPlanContext(command = command, workMode = WorkMode.STUB, stubCase = Stubs.SUCCESS)
                    processor.exec(ctx)
                    assertEquals(State.FINISHING, ctx.state, command.name)
                    if (command == TrainingPlanCommand.SEARCH) {
                        assertEquals(1, ctx.trainingPlansResponse.totalSize, command.name)
                    } else {
                        assertTrue(ctx.trainingPlanResponse.title.isNotBlank(), command.name)
                    }
                }
        }

    @Test
    fun prodIsNotImplemented() =
        runTest {
            val ctx = ClientCardContext(command = ClientCardCommand.READ)
            processor.exec(ctx)
            assertEquals(State.FAILING, ctx.state)
            assertEquals("not-implemented", ctx.errors.single().code)
        }
}
