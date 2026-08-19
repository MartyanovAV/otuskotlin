package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

abstract class RepoTrainingPlanUpdateTest {
    abstract val repo: IRepoTrainingPlan
    protected open val updateSucc = initObjects[0]
    protected val updateIdNotFound = TrainingPlanId("tp-repo-update-not-found")

    private val reqUpdateSucc by lazy {
        TrainingPlan(
            id = updateSucc.id,
            clientCardId = ClientCardId("cc-123"),
            ownerId = "owner-123",
            title = "update object",
            lock = updateSucc.lock,
            planItems = listOf(ExerciseItem(id = "ex-update", title = "Update Exercise", exerciseId = "ex-1")),
        )
    }
    private val reqUpdateNotFound =
        TrainingPlan(
            id = updateIdNotFound,
            clientCardId = ClientCardId("cc-123"),
            ownerId = "owner-123",
            title = "update object not found",
        )

    @Test
    fun updateSuccess() =
        runRepoTest {
            val result = repo.updateTrainingPlan(DbTrainingPlanRequest(reqUpdateSucc))
            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals(reqUpdateSucc.id, result.data.id)
            assertEquals(reqUpdateSucc.title, result.data.title)
        }

    @Test
    fun updateNotFound() =
        runRepoTest {
            val result = repo.updateTrainingPlan(DbTrainingPlanRequest(reqUpdateNotFound))
            assertIs<DbTrainingPlanResponseErr>(result)
            val error = result.errors.find { it.code == "repo-not-found" }
            assertEquals("id", error?.field)
        }

    companion object : BaseInitTrainingPlans("update") {
        override val initObjects: List<TrainingPlan> =
            listOf(
                createInitTestModel("update"),
            )
    }
}
