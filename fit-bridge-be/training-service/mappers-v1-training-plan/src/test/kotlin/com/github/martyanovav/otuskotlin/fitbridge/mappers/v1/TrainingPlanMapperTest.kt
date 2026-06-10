package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanCreateResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchFilter
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem as InternalExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseSet as InternalExerciseSet
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainingPlanMapperTest {
    @Test
    fun `training plan create request maps to context`() {
        val itemId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val request = TrainingPlanCreateRequest(
            requestType = "trainingPlan.create",
            requestId = "tp-create-1",
            debug = Debug(mode = RequestDebugMode.TEST),
            trainingPlan = TrainingPlanCreateObject(
                title = "Hypertrophy Plan",
                clientCardId = "client-card-1",
                planItems = listOf(
                    ExerciseItem(
                        id = itemId,
                        title = "Squat",
                        description = "Back squat",
                        exerciseId = "exercise-squat",
                        sets = listOf(ExerciseSet(reps = "8", weight = "100", weightUnit = "KG", durationSeconds = 0)),
                        restBetweenSetsSeconds = 120,
                    )
                ),
            ),
        )

        val context = request.fromTransport()
        val exercise = context.trainingPlanRequest.planItems.first() as InternalExerciseItem

        assertEquals(TrainingPlanCommand.CREATE, context.command)
        assertEquals(RequestId("tp-create-1"), context.requestId)
        assertEquals(WorkMode.TEST, context.workMode)
        assertEquals("Hypertrophy Plan", context.trainingPlanRequest.title)
        assertEquals(ClientCardId("client-card-1"), context.trainingPlanRequest.clientCardId)
        assertEquals(itemId.toString(), exercise.id)
        assertEquals("Squat", exercise.title)
        assertEquals("Back squat", exercise.description)
        assertEquals("exercise-squat", exercise.exerciseId)
        assertEquals("8", exercise.sets.first().reps)
        assertEquals(120, exercise.restBetweenSetsSeconds)
    }

    @Test
    fun `training plan search request maps to context and response page`() {
        val request = TrainingPlanSearchRequest(
            requestType = "trainingPlan.search",
            requestId = "tp-search-1",
            debug = Debug(mode = RequestDebugMode.STUB),
            trainingPlanFilter = TrainingPlanSearchFilter(
                clientCardId = "client-card-1",
                status = TrainingPlanStatus.ACTIVE,
                searchString = "strength",
                pageNumber = 3,
                pageSize = 15,
            ),
        )

        val context = request.fromTransport()

        assertEquals(TrainingPlanCommand.SEARCH, context.command)
        assertEquals(WorkMode.STUB, context.workMode)
        assertEquals(ClientCardId("client-card-1"), context.trainingPlanFilter.clientCardId)
        assertEquals("ACTIVE", context.trainingPlanFilter.status)
        assertEquals("strength", context.trainingPlanFilter.searchString)
        assertEquals(3, context.trainingPlansResponse.pageNumber)
        assertEquals(15, context.trainingPlansResponse.pageSize)
    }

    @Test
    fun `training plan context maps to create response`() {
        val itemId = "22222222-2222-2222-2222-222222222222"
        val context = TrainingPlanContext(
            requestId = RequestId("tp-response-1"),
            command = TrainingPlanCommand.CREATE,
            state = State.RUNNING,
            trainingPlanResponse = TrainingPlan(
                id = TrainingPlanId("plan-1"),
                clientCardId = ClientCardId("client-card-1"),
                title = "Strength Plan",
                planItems = mutableListOf(
                    InternalExerciseItem(
                        id = itemId,
                        title = "Bench press",
                        description = "",
                        exerciseId = "exercise-bench",
                        sets = mutableListOf(InternalExerciseSet(reps = "5", weight = "80", weightUnit = "KG")),
                        restBetweenSetsSeconds = 180,
                    )
                ),
            ),
        )

        val response = context.toTransport() as TrainingPlanCreateResponse
        val exercise = response.trainingPlan?.planItems?.first() as ExerciseItem

        assertEquals("tp-response-1", response.requestId)
        assertEquals("plan-1", response.trainingPlan?.id)
        assertEquals("client-card-1", response.trainingPlan?.clientCardId)
        assertEquals("Strength Plan", response.trainingPlan?.title)
        assertEquals(UUID.fromString(itemId), exercise.id)
        assertEquals("Bench press", exercise.title)
        assertEquals(null, exercise.description)
        assertEquals("exercise-bench", exercise.exerciseId)
        assertEquals("5", exercise.sets?.first()?.reps)
    }

    @Test
    fun `training plan context maps to search response`() {
        val context = TrainingPlanContext(
            requestId = RequestId("tp-search-response-1"),
            command = TrainingPlanCommand.SEARCH,
            state = State.RUNNING,
            trainingPlansResponse = Page(
                items = listOf(TrainingPlan(id = TrainingPlanId("plan-1"), title = "Strength Plan")),
                totalSize = 1,
                pageNumber = 1,
                pageSize = 10,
            ),
        )

        val response = context.toTransport() as TrainingPlanSearchResponse

        assertEquals("tp-search-response-1", response.requestId)
        assertEquals("Strength Plan", response.trainingPlans?.firstOrNull()?.title)
        assertEquals(1, response.totalSize)
        assertEquals(1, response.pageNumber)
        assertEquals(10, response.pageSize)
    }
}
