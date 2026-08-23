package com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation

import com.github.martyanovav.otuskotlin.fitbridge.training.biz.TrainingPlanProcessor
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanCorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.CircuitItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.SupersetItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoClientCardInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoTrainingPlanInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoTrainingPlanStub
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrainingPlanValidationTest {
    private val processor =
        TrainingPlanProcessor(
            TrainingPlanCorSettings(
                repoTrainingPlanTest = RepoTrainingPlanInMemory(),
                repoTrainingPlanStub = RepoTrainingPlanStub(),
                repoClientCardTest =
                    RepoClientCardInMemory().apply {
                        save(listOf(ClientCard(id = ClientCardId("client-1"), ownerUserId = "user-1", createdByUserId = "user-1")))
                    },
            ),
        )

    @Test
    fun validCreateRequestIsDeepCopiedNormalizedAndValidated() =
        runTest {
            val request =
                TrainingPlan(
                    clientCardId = ClientCardId("  client-1  "),
                    title = "  План  ",
                    planItems =
                        mutableListOf(
                            ExerciseItem(
                                id = "  00000000-0000-0000-0000-000000000301  ",
                                title = "  Приседания  ",
                            ),
                        ),
                )
            val ctx =
                TrainingPlanContext(
                    command = TrainingPlanCommand.CREATE,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                    trainingPlanRequest = request,
                )

            processor.exec(ctx)

            assertEquals(State.FINISHING, ctx.state)
            assertTrue(ctx.errors.isEmpty())
            assertEquals("client-1", ctx.trainingPlanValidated.clientCardId.asString())
            assertEquals("План", ctx.trainingPlanValidated.title)
            assertEquals("Приседания", ctx.trainingPlanValidated.planItems.single().title)
            assertEquals("  Приседания  ", request.planItems.single().title)
            assertEquals("user-1", ctx.trainingPlanResponse.ownerUserId)
            assertEquals("user-1", ctx.trainingPlanResponse.createdByUserId)
        }

    @Test
    fun itemIdsMustBeUuidsAndUnique() =
        runTest {
            val duplicateId = "00000000-0000-0000-0000-000000000301"
            val ctx =
                createContext(
                    ExerciseItem(id = "not-a-uuid", title = "Первое"),
                    ExerciseItem(id = duplicateId, title = "Второе"),
                    ExerciseItem(id = duplicateId, title = "Третье"),
                )

            processor.exec(ctx)

            assertEquals(State.FAILING, ctx.state)
            assertEquals(
                setOf(
                    "validation-planItems.id-badFormat",
                    "validation-planItems.id-duplicate",
                ),
                ctx.errors.map { it.code }.toSet(),
            )
        }

    @Test
    fun nestedStructureAndNumericRangesAreValidated() =
        runTest {
            val ctx =
                createContext(
                    CircuitItem(
                        id = "00000000-0000-0000-0000-000000000301",
                        title = "Круг",
                        rounds = 0,
                        restBetweenRoundsSeconds = -1,
                        items =
                            mutableListOf(
                                SupersetItem(
                                    id = "00000000-0000-0000-0000-000000000302",
                                    title = "Суперсет",
                                    restBetweenSetsSeconds = -1,
                                    items =
                                        mutableListOf(
                                            ExerciseItem(
                                                id = "00000000-0000-0000-0000-000000000303",
                                                title = "Упражнение",
                                                sets = mutableListOf(ExerciseSet(durationSeconds = -1)),
                                            ),
                                        ),
                                ),
                            ),
                    ),
                )

            processor.exec(ctx)

            assertEquals(State.FAILING, ctx.state)
            assertEquals(
                setOf(
                    "validation-planItems.items-invalidSize",
                    "validation-planItems.rounds-outOfRange",
                    "validation-planItems.sets.durationSeconds-outOfRange",
                    "validation-planItems.restSeconds-outOfRange",
                ),
                ctx.errors.map { it.code }.toSet(),
            )
        }

    @Test
    fun itemCountAndNestingDepthAreLimited() =
        runTest {
            val tooManyItems =
                (1..201).map { index ->
                    ExerciseItem(
                        id = "00000000-0000-0000-0000-${index.toString().padStart(12, '0')}",
                        title = "Упражнение $index",
                    )
                }.toTypedArray()
            val countContext = createContext(*tooManyItems)

            processor.exec(countContext)

            assertEquals(State.FAILING, countContext.state)
            assertEquals(setOf("validation-planItems-tooMany"), countContext.errors.map { it.code }.toSet())

            var nestedItem: PlanItem =
                ExerciseItem(
                    id = "00000000-0000-0000-0000-000000000306",
                    title = "Упражнение",
                )
            for (index in 5 downTo 1) {
                nestedItem =
                    CircuitItem(
                        id = "00000000-0000-0000-0000-${index.toString().padStart(12, '0')}",
                        title = "Круг $index",
                        items = mutableListOf(nestedItem),
                    )
            }
            val depthContext = createContext(nestedItem)

            processor.exec(depthContext)

            assertEquals(State.FAILING, depthContext.state)
            assertEquals(setOf("validation-planItems-tooDeep"), depthContext.errors.map { it.code }.toSet())
        }

    @Test
    fun requiredUpdateFieldsAreValidatedTogether() =
        runTest {
            val ctx =
                TrainingPlanContext(
                    command = TrainingPlanCommand.UPDATE,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                    trainingPlanRequest = TrainingPlan(),
                )

            processor.exec(ctx)

            assertEquals(State.FAILING, ctx.state)
            assertEquals(
                setOf(
                    "validation-id-empty",
                    "validation-title-empty",
                    "validation-planItems-empty",
                    "validation-lock-empty",
                ),
                ctx.errors.map { it.code }.toSet(),
            )
        }

    @Test
    fun completedPlanCannotBeUpdated() =
        runTest {
            val planRepo =
                RepoTrainingPlanInMemory().apply {
                    save(
                        listOf(
                            TrainingPlan(
                                id = TrainingPlanId("00000000-0000-0000-0000-000000000201"),
                                clientCardId = ClientCardId("client-1"),
                                ownerUserId = "user-1",
                                createdByUserId = "user-1",
                                title = "Завершенная тренировка",
                                status = TrainingPlanStatus.COMPLETED,
                                lock = TrainingPlanLock("lock-1"),
                                planItems = listOf(ExerciseItem(id = "00000000-0000-0000-0000-000000000301", title = "Приседания")),
                            ),
                        ),
                    )
                }
            val testProcessor =
                TrainingPlanProcessor(
                    TrainingPlanCorSettings(
                        repoTrainingPlanTest = planRepo,
                        repoClientCardTest =
                            RepoClientCardInMemory().apply {
                                save(listOf(ClientCard(id = ClientCardId("client-1"), ownerUserId = "user-1", createdByUserId = "user-1")))
                            },
                    ),
                )

            val ctx =
                TrainingPlanContext(
                    command = TrainingPlanCommand.UPDATE,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                    trainingPlanRequest =
                        TrainingPlan(
                            id = TrainingPlanId("00000000-0000-0000-0000-000000000201"),
                            lock = TrainingPlanLock("lock-1"),
                            title = "Попытка обновить",
                            planItems = listOf(ExerciseItem(id = "00000000-0000-0000-0000-000000000301", title = "Приседания")),
                        ),
                )

            testProcessor.exec(ctx)

            assertEquals(State.FAILING, ctx.state)
            assertEquals(setOf("invalid-status"), ctx.errors.map { it.code }.toSet())
        }

    @Test
    fun validCreateDraftRequestSetsDraftStatus() =
        runTest {
            val request =
                TrainingPlan(
                    clientCardId = ClientCardId("client-1"),
                    title = "Черновой план",
                    status = TrainingPlanStatus.DRAFT,
                    planItems =
                        mutableListOf(
                            ExerciseItem(
                                id = "00000000-0000-0000-0000-000000000301",
                                title = "Приседания",
                            ),
                        ),
                )
            val ctx =
                TrainingPlanContext(
                    command = TrainingPlanCommand.CREATE,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                    trainingPlanRequest = request,
                )

            processor.exec(ctx)

            assertEquals(State.FINISHING, ctx.state)
            assertTrue(ctx.errors.isEmpty())
            assertEquals(TrainingPlanStatus.DRAFT, ctx.trainingPlanResponse.status)
        }

    @Test
    fun activateDraftTrainingPlanSucceeds() =
        runTest {
            val repo =
                RepoTrainingPlanInMemory().apply {
                    save(
                        listOf(
                            TrainingPlan(
                                id = TrainingPlanId("00000000-0000-0000-0000-000000000201"),
                                ownerUserId = "user-1",
                                createdByUserId = "user-1",
                                clientCardId = ClientCardId("client-1"),
                                title = "Черновик",
                                status = TrainingPlanStatus.DRAFT,
                                lock = TrainingPlanLock("lock-1"),
                                planItems = listOf(ExerciseItem(id = "00000000-0000-0000-0000-000000000301", title = "Приседания")),
                            ),
                        ),
                    )
                }
            val testProcessor =
                TrainingPlanProcessor(
                    TrainingPlanCorSettings(
                        repoTrainingPlanTest = repo,
                        repoTrainingPlanStub = RepoTrainingPlanStub(),
                        repoClientCardTest =
                            RepoClientCardInMemory().apply {
                                save(listOf(ClientCard(id = ClientCardId("client-1"), ownerUserId = "user-1", createdByUserId = "user-1")))
                            },
                    ),
                )
            val ctx =
                TrainingPlanContext(
                    command = TrainingPlanCommand.ACTIVATE,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                    trainingPlanRequest =
                        TrainingPlan(
                            id = TrainingPlanId("00000000-0000-0000-0000-000000000201"),
                            lock = TrainingPlanLock("lock-1"),
                        ),
                )

            testProcessor.exec(ctx)

            assertEquals(State.FINISHING, ctx.state)
            assertTrue(ctx.errors.isEmpty())
            assertEquals(TrainingPlanStatus.ACTIVE, ctx.trainingPlanResponse.status)
        }

    @Test
    fun activateActiveTrainingPlanFailsWithInvalidStatus() =
        runTest {
            val repo =
                RepoTrainingPlanInMemory().apply {
                    save(
                        listOf(
                            TrainingPlan(
                                id = TrainingPlanId("00000000-0000-0000-0000-000000000201"),
                                ownerUserId = "user-1",
                                createdByUserId = "user-1",
                                clientCardId = ClientCardId("client-1"),
                                title = "Активный план",
                                status = TrainingPlanStatus.ACTIVE,
                                lock = TrainingPlanLock("lock-1"),
                                planItems = listOf(ExerciseItem(id = "00000000-0000-0000-0000-000000000301", title = "Приседания")),
                            ),
                        ),
                    )
                }
            val testProcessor =
                TrainingPlanProcessor(
                    TrainingPlanCorSettings(
                        repoTrainingPlanTest = repo,
                        repoTrainingPlanStub = RepoTrainingPlanStub(),
                        repoClientCardTest =
                            RepoClientCardInMemory().apply {
                                save(listOf(ClientCard(id = ClientCardId("client-1"), ownerUserId = "user-1", createdByUserId = "user-1")))
                            },
                    ),
                )
            val ctx =
                TrainingPlanContext(
                    command = TrainingPlanCommand.ACTIVATE,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                    trainingPlanRequest =
                        TrainingPlan(
                            id = TrainingPlanId("00000000-0000-0000-0000-000000000201"),
                            lock = TrainingPlanLock("lock-1"),
                        ),
                )

            testProcessor.exec(ctx)

            assertEquals(State.FAILING, ctx.state)
            assertEquals(setOf("invalid-status"), ctx.errors.map { it.code }.toSet())
        }

    @Test
    fun updateDraftTrainingPlanPreservesDraftStatus() =
        runTest {
            val repo =
                RepoTrainingPlanInMemory().apply {
                    save(
                        listOf(
                            TrainingPlan(
                                id = TrainingPlanId("00000000-0000-0000-0000-000000000202"),
                                ownerUserId = "user-1",
                                createdByUserId = "user-1",
                                clientCardId = ClientCardId("client-1"),
                                title = "Черновик",
                                status = TrainingPlanStatus.DRAFT,
                                lock = TrainingPlanLock("lock-2"),
                                planItems = listOf(ExerciseItem(id = "00000000-0000-0000-0000-000000000302", title = "Приседания")),
                            ),
                        ),
                    )
                }
            val testProcessor =
                TrainingPlanProcessor(
                    TrainingPlanCorSettings(
                        repoTrainingPlanTest = repo,
                        repoTrainingPlanStub = RepoTrainingPlanStub(),
                        repoClientCardTest =
                            RepoClientCardInMemory().apply {
                                save(listOf(ClientCard(id = ClientCardId("client-1"), ownerUserId = "user-1", createdByUserId = "user-1")))
                            },
                    ),
                )
            val ctx =
                TrainingPlanContext(
                    command = TrainingPlanCommand.UPDATE,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                    trainingPlanRequest =
                        TrainingPlan(
                            id = TrainingPlanId("00000000-0000-0000-0000-000000000202"),
                            lock = TrainingPlanLock("lock-2"),
                            title = "Обновлённый черновик",
                            planItems = mutableListOf(ExerciseItem(id = "00000000-0000-0000-0000-000000000303", title = "Жим лёжа")),
                        ),
                )

            testProcessor.exec(ctx)

            assertEquals(State.FINISHING, ctx.state)
            assertTrue(ctx.errors.isEmpty())
            assertEquals(TrainingPlanStatus.DRAFT, ctx.trainingPlanResponse.status)
            assertEquals("Обновлённый черновик", ctx.trainingPlanResponse.title)
        }

    @Test
    fun searchFilterIsValidated() =
        runTest {
            val ctx =
                TrainingPlanContext(
                    command = TrainingPlanCommand.SEARCH,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
                    trainingPlanFilter =
                        TrainingPlanFilter(
                            clientCardId = ClientCardId("bad id"),
                            searchString = "a".repeat(121),
                            pageNumber = 0,
                            pageSize = 101,
                        ),
                )

            processor.exec(ctx)

            assertEquals(State.FAILING, ctx.state)
            assertEquals(
                setOf(
                    "validation-clientCardId-badFormat",
                    "validation-searchString-tooLong",
                    "validation-pageNumber-outOfRange",
                    "validation-pageSize-outOfRange",
                ),
                ctx.errors.map { it.code }.toSet(),
            )
        }

    private fun createContext(vararg items: PlanItem) =
        TrainingPlanContext(
            command = TrainingPlanCommand.CREATE,
            workMode = WorkMode.TEST,
            principal = AuthPrincipal(userId = "user-1", roles = setOf(AuthPrincipal.TRAINER_ROLE)),
            trainingPlanRequest =
                TrainingPlan(
                    clientCardId = ClientCardId("client-1"),
                    title = "План тренировок",
                    planItems = items.toMutableList(),
                ),
        )
}
