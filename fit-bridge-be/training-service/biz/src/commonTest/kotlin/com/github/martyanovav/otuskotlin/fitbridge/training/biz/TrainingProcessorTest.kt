package com.github.martyanovav.otuskotlin.fitbridge.training.biz

import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoClientCardInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoTrainingPlanInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoClientCardStub
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoTrainingPlanStub
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrainingProcessorTest {
    private val processor =
        TrainingProcessor(
            CorSettings(
                repoClientCardTest = RepoClientCardInMemory(),
                repoClientCardProd = RepoClientCardInMemory(),
                repoClientCardStub = RepoClientCardStub(),
                repoTrainingPlanTest = RepoTrainingPlanInMemory(),
                repoTrainingPlanProd = RepoTrainingPlanInMemory(),
                repoTrainingPlanStub = RepoTrainingPlanStub(),
            ),
        )

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
                        val expectedStatus =
                            when (command) {
                                TrainingPlanCommand.ARCHIVE -> TrainingPlanStatus.ARCHIVED
                                TrainingPlanCommand.COMPLETE -> TrainingPlanStatus.COMPLETED
                                else -> TrainingPlanStatus.ACTIVE
                            }
                        assertEquals(expectedStatus, ctx.trainingPlanResponse.status, command.name)
                    }
                }
        }

    @Test
    fun notFoundStubReturnsExpectedError() =
        runTest {
            assertClientCardStubFailure(
                command = ClientCardCommand.READ,
                stubCase = Stubs.NOT_FOUND,
                expectedCode = "not-found",
            )
            assertTrainingPlanStubFailure(
                command = TrainingPlanCommand.READ,
                stubCase = Stubs.NOT_FOUND,
                expectedCode = "not-found",
            )
        }

    @Test
    fun badIdStubReturnsExpectedErrorForIdBasedCommands() =
        runTest {
            listOf(ClientCardCommand.READ, ClientCardCommand.UPDATE, ClientCardCommand.ARCHIVE)
                .forEach { command ->
                    assertClientCardStubFailure(
                        command = command,
                        stubCase = Stubs.BAD_ID,
                        expectedCode = "bad-id",
                        expectedField = "id",
                    )
                }
            listOf(TrainingPlanCommand.READ, TrainingPlanCommand.UPDATE, TrainingPlanCommand.ARCHIVE)
                .forEach { command ->
                    assertTrainingPlanStubFailure(
                        command = command,
                        stubCase = Stubs.BAD_ID,
                        expectedCode = "bad-id",
                        expectedField = "id",
                    )
                }
        }

    @Test
    fun badPlanTitleStubReturnsExpectedError() =
        runTest {
            listOf(TrainingPlanCommand.CREATE, TrainingPlanCommand.UPDATE)
                .forEach { command ->
                    assertTrainingPlanStubFailure(
                        command = command,
                        stubCase = Stubs.BAD_PLAN_TITLE,
                        expectedCode = "bad-plan-title",
                        expectedField = "title",
                    )
                }
        }

    @Test
    fun cannotArchiveStubReturnsExpectedError() =
        runTest {
            assertClientCardStubFailure(
                command = ClientCardCommand.ARCHIVE,
                stubCase = Stubs.CANNOT_ARCHIVE,
                expectedCode = "cannot-archive",
            )
            assertTrainingPlanStubFailure(
                command = TrainingPlanCommand.ARCHIVE,
                stubCase = Stubs.CANNOT_ARCHIVE,
                expectedCode = "cannot-archive",
            )
        }

    @Test
    fun unsupportedStubReturnsNoCaseError() =
        runTest {
            assertClientCardStubFailure(
                command = ClientCardCommand.CREATE,
                stubCase = Stubs.BAD_ID,
                expectedCode = "stub-not-configured",
            )
            assertTrainingPlanStubFailure(
                command = TrainingPlanCommand.SEARCH,
                stubCase = Stubs.BAD_PLAN_TITLE,
                expectedCode = "stub-not-configured",
            )
        }

    @Test
    fun prodRequestIsValidated() =
        runTest {
            val ctx = ClientCardContext(command = ClientCardCommand.READ)

            processor.exec(ctx)

            assertEquals(State.FAILING, ctx.state)
            assertEquals("validation-id-empty", ctx.errors.single().code)
        }

    @Test
    fun validProdRequestCompletesValidation() =
        runTest {
            val ctx =
                ClientCardContext(
                    command = ClientCardCommand.CREATE,
                    clientCardRequest = ClientCard(displayName = "Клиент"),
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                )

            processor.exec(ctx)

            assertEquals(State.FINISHING, ctx.state)
            assertTrue(ctx.errors.isEmpty())
            assertEquals("Клиент", ctx.clientCardValidated.displayName)
            assertEquals("user-1", ctx.clientCardResponse.ownerUserId)
            assertEquals("user-1", ctx.clientCardResponse.createdByUserId)
        }

    private suspend fun assertClientCardStubFailure(
        command: ClientCardCommand,
        stubCase: Stubs,
        expectedCode: String,
        expectedField: String = "",
    ) {
        val ctx =
            ClientCardContext(
                command = command,
                workMode = WorkMode.STUB,
                stubCase = stubCase,
            )

        processor.exec(ctx)

        val error = ctx.errors.single()
        assertEquals(State.FAILING, ctx.state, command.name)
        assertEquals(expectedCode, error.code, command.name)
        assertEquals("business", error.group, command.name)
        assertEquals(expectedField, error.field, command.name)
        assertEquals(ClientCard(), ctx.clientCardResponse, command.name)
    }

    private suspend fun assertTrainingPlanStubFailure(
        command: TrainingPlanCommand,
        stubCase: Stubs,
        expectedCode: String,
        expectedField: String = "",
    ) {
        val ctx =
            TrainingPlanContext(
                command = command,
                workMode = WorkMode.STUB,
                stubCase = stubCase,
            )

        processor.exec(ctx)

        val error = ctx.errors.single()
        assertEquals(State.FAILING, ctx.state, command.name)
        assertEquals(expectedCode, error.code, command.name)
        assertEquals("business", error.group, command.name)
        assertEquals(expectedField, error.field, command.name)
        assertEquals(TrainingPlan(), ctx.trainingPlanResponse, command.name)
    }
}
