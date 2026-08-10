package com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.rootChain
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.CircuitItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.SupersetItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainingPlanValidationRuleUnitTest {
    @Test
    fun `training plan id must not be empty`() = runTest {
        assertRule(
            plan = validPlan(id = TrainingPlanId.NONE),
            expectedCode = "validation-id-empty",
        ) { validateTrainingPlanIdNotEmpty("validate id") }
    }

    @Test
    fun `training plan id must have valid format`() = runTest {
        assertRule(
            plan = validPlan(id = TrainingPlanId("bad id")),
            expectedCode = "validation-id-badFormat",
        ) { validateTrainingPlanIdFormat("validate id") }
    }

    @Test
    fun `client card id must not be empty`() = runTest {
        assertRule(
            plan = validPlan(clientCardId = ClientCardId.NONE),
            expectedCode = "validation-clientCardId-empty",
        ) { validateTrainingPlanClientCardIdNotEmpty("validate client card id") }
    }

    @Test
    fun `client card id must have valid format`() = runTest {
        assertRule(
            plan = validPlan(clientCardId = ClientCardId("bad id")),
            expectedCode = "validation-clientCardId-badFormat",
        ) { validateTrainingPlanClientCardIdFormat("validate client card id") }
    }

    @Test
    fun `title must not be empty`() = runTest {
        assertRule(
            plan = validPlan(title = ""),
            expectedCode = "validation-title-empty",
        ) { validateTrainingPlanTitleNotEmpty("validate title") }
    }

    @Test
    fun `title must satisfy minimum length`() = runTest {
        assertRule(
            plan = validPlan(title = "ab"),
            expectedCode = "validation-title-tooShort",
        ) { validateTrainingPlanTitleMinLength("validate title") }
    }

    @Test
    fun `title must satisfy maximum length`() = runTest {
        assertRule(
            plan = validPlan(title = "a".repeat(121)),
            expectedCode = "validation-title-tooLong",
        ) { validateTrainingPlanTitleMaxLength("validate title") }
    }

    @Test
    fun `title must contain letters`() = runTest {
        assertRule(
            plan = validPlan(title = "123--"),
            expectedCode = "validation-title-noContent",
        ) { validateTrainingPlanTitleHasContent("validate title") }
    }

    @Test
    fun `plan items must not be empty`() = runTest {
        assertRule(
            plan = validPlan(planItems = mutableListOf()),
            expectedCode = "validation-planItems-empty",
        ) { validateTrainingPlanItemsNotEmpty("validate items") }
    }

    @Test
    fun `plan item count must satisfy maximum`() = runTest {
        val items: MutableList<PlanItem> = (0..200).map { validExercise(uuid(it)) }.toMutableList()

        assertRule(
            plan = validPlan(planItems = items),
            expectedCode = "validation-planItems-tooMany",
        ) { validateTrainingPlanItemCount("validate item count") }
    }

    @Test
    fun `plan item nesting must satisfy maximum depth`() = runTest {
        assertRule(
            plan = validPlan(planItems = mutableListOf(nestedCircuit(depth = 6))),
            expectedCode = "validation-planItems-tooDeep",
        ) { validateTrainingPlanItemDepth("validate item depth") }
    }

    @Test
    fun `plan item id must be uuid`() = runTest {
        assertRule(
            plan = validPlan(planItems = mutableListOf(validExercise("item-1"))),
            expectedCode = "validation-planItems.id-badFormat",
        ) { validateTrainingPlanItemIds("validate item ids") }
    }

    @Test
    fun `plan item ids must be unique`() = runTest {
        val duplicateId = uuid(1)

        assertRule(
            plan = validPlan(
                planItems = mutableListOf(
                    validExercise(duplicateId),
                    validExercise(duplicateId),
                ),
            ),
            expectedCode = "validation-planItems.id-duplicate",
        ) { validateTrainingPlanItemIdsUnique("validate unique item ids") }
    }

    @Test
    fun `plan item title must be valid`() = runTest {
        assertRule(
            plan = validPlan(planItems = mutableListOf(validExercise(uuid(1), title = "123--"))),
            expectedCode = "validation-planItems.title-invalid",
        ) { validateTrainingPlanItemTitles("validate item titles") }
    }

    @Test
    fun `plan item description must satisfy maximum length`() = runTest {
        assertRule(
            plan = validPlan(
                planItems = mutableListOf(validExercise(uuid(1), description = "a".repeat(2001))),
            ),
            expectedCode = "validation-planItems.description-tooLong",
        ) { validateTrainingPlanItemDescriptions("validate item descriptions") }
    }

    @Test
    fun `circuit must contain an item`() = runTest {
        assertRule(
            plan = validPlan(
                planItems = mutableListOf(
                    CircuitItem(id = uuid(1), title = "Circuit", items = mutableListOf()),
                ),
            ),
            expectedCode = "validation-planItems.items-invalidSize",
        ) { validateTrainingPlanItemCollections("validate item collections") }
    }

    @Test
    fun `superset must contain at least two items`() = runTest {
        assertRule(
            plan = validPlan(
                planItems = mutableListOf(
                    SupersetItem(
                        id = uuid(1),
                        title = "Superset",
                        items = mutableListOf(validExercise(uuid(2))),
                    ),
                ),
            ),
            expectedCode = "validation-planItems.items-invalidSize",
        ) { validateTrainingPlanItemCollections("validate item collections") }
    }

    @Test
    fun `circuit rounds must be positive`() = runTest {
        assertRule(
            plan = validPlan(
                planItems = mutableListOf(
                    CircuitItem(
                        id = uuid(1),
                        title = "Circuit",
                        rounds = 0,
                        items = mutableListOf(validExercise(uuid(2))),
                    ),
                ),
            ),
            expectedCode = "validation-planItems.rounds-outOfRange",
        ) { validateTrainingPlanItemRounds("validate rounds") }
    }

    @Test
    fun `exercise set duration must not be negative`() = runTest {
        assertRule(
            plan = validPlan(
                planItems = mutableListOf(
                    validExercise(uuid(1), sets = mutableListOf(ExerciseSet(durationSeconds = -1))),
                ),
            ),
            expectedCode = "validation-planItems.sets.durationSeconds-outOfRange",
        ) { validateTrainingPlanItemDurations("validate set duration") }
    }

    @Test
    fun `exercise rest must not be negative`() = runTest {
        assertRule(
            plan = validPlan(
                planItems = mutableListOf(validExercise(uuid(1), restBetweenSetsSeconds = -1)),
            ),
            expectedCode = "validation-planItems.restSeconds-outOfRange",
        ) { validateTrainingPlanItemRestSeconds("validate rest") }
    }

    @Test
    fun `circuit rest must not be negative`() = runTest {
        assertRule(
            plan = validPlan(
                planItems = mutableListOf(
                    CircuitItem(
                        id = uuid(1),
                        title = "Circuit",
                        items = mutableListOf(validExercise(uuid(2))),
                        restBetweenRoundsSeconds = -1,
                    ),
                ),
            ),
            expectedCode = "validation-planItems.restSeconds-outOfRange",
        ) { validateTrainingPlanItemRestSeconds("validate rest") }
    }

    @Test
    fun `superset rest must not be negative`() = runTest {
        assertRule(
            plan = validPlan(
                planItems = mutableListOf(
                    SupersetItem(
                        id = uuid(1),
                        title = "Superset",
                        items = mutableListOf(validExercise(uuid(2)), validExercise(uuid(3))),
                        restBetweenSetsSeconds = -1,
                    ),
                ),
            ),
            expectedCode = "validation-planItems.restSeconds-outOfRange",
        ) { validateTrainingPlanItemRestSeconds("validate rest") }
    }

    @Test
    fun `lock must not be empty`() = runTest {
        assertRule(
            plan = validPlan(lock = ""),
            expectedCode = "validation-lock-empty",
        ) { validateTrainingPlanLockNotEmpty("validate lock") }
    }

    @Test
    fun `lock must have valid format`() = runTest {
        assertRule(
            plan = validPlan(lock = "bad lock"),
            expectedCode = "validation-lock-badFormat",
        ) { validateTrainingPlanLockFormat("validate lock") }
    }

    @Test
    fun `filter client card id must have valid format`() = runTest {
        assertRule(
            filter = TrainingPlanFilter(clientCardId = ClientCardId("bad id")),
            expectedCode = "validation-clientCardId-badFormat",
        ) { validateTrainingPlanFilterClientCardIdFormat("validate client card id") }
    }

    @Test
    fun `search string must satisfy maximum length`() = runTest {
        assertRule(
            filter = TrainingPlanFilter(searchString = "a".repeat(121)),
            expectedCode = "validation-searchString-tooLong",
        ) { validateTrainingPlanSearchStringLength("validate search string") }
    }

    @Test
    fun `filter status must be supported`() = runTest {
        assertRule(
            filter = TrainingPlanFilter(status = "UNKNOWN"),
            expectedCode = "validation-status-unsupported",
        ) { validateTrainingPlanFilterStatus("validate status") }
    }

    @Test
    fun `page number must be positive`() = runTest {
        assertRule(
            filter = TrainingPlanFilter(pageNumber = 0),
            expectedCode = "validation-pageNumber-outOfRange",
        ) { validateTrainingPlanPageNumber("validate page number") }
    }

    @Test
    fun `page size must be within supported range`() = runTest {
        assertRule(
            filter = TrainingPlanFilter(pageSize = 101),
            expectedCode = "validation-pageSize-outOfRange",
        ) { validateTrainingPlanPageSize("validate page size") }
    }

    private suspend fun assertRule(
        plan: TrainingPlan = validPlan(),
        filter: TrainingPlanFilter = TrainingPlanFilter(),
        expectedCode: String,
        rule: ICorChainDsl<IFBContext>.() -> Unit,
    ) {
        val context = TrainingPlanContext(
            state = State.RUNNING,
            trainingPlanValidating = plan,
            trainingPlanFilterValidating = filter,
        )

        rootChain(rule).build().exec(context)

        assertEquals(State.FAILING, context.state)
        assertEquals(expectedCode, context.errors.single().code)
    }

    private fun validPlan(
        id: TrainingPlanId = TrainingPlanId("plan-1"),
        clientCardId: ClientCardId = ClientCardId("client-1"),
        title: String = "Training plan",
        lock: String = "lock-1",
        planItems: MutableList<PlanItem> = mutableListOf(validExercise(uuid(1))),
    ) = TrainingPlan(
        id = id,
        clientCardId = clientCardId,
        title = title,
        lock = lock,
        planItems = planItems,
    )

    private fun validExercise(
        id: String,
        title: String = "Exercise",
        description: String = "",
        sets: MutableList<ExerciseSet> = mutableListOf(),
        restBetweenSetsSeconds: Int = 0,
    ) = ExerciseItem(
        id = id,
        title = title,
        description = description,
        sets = sets,
        restBetweenSetsSeconds = restBetweenSetsSeconds,
    )

    private fun nestedCircuit(depth: Int): PlanItem = if (depth == 1) {
        validExercise(uuid(depth))
    } else {
        CircuitItem(
            id = uuid(depth),
            title = "Circuit",
            items = mutableListOf(nestedCircuit(depth - 1)),
        )
    }

    private fun uuid(value: Int): String = "00000000-0000-0000-0000-${value.toString().padStart(12, '0')}"
}
