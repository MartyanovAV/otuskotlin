package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.IRepoTrainingPlanInitializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

abstract class RepoTrainingPlanCreateTest {
    abstract val repo: IRepoTrainingPlanInitializable
    protected open val uuidNew = TrainingPlanId("10000000-0000-0000-0000-000000000001")

    private val createObj =
        TrainingPlan(
            clientCardId = ClientCardId("cc-123"),
            ownerId = "owner-123",
            title = "create object",
        )

    @Test
    fun createSuccess() =
        runRepoTest {
            val result = repo.createTrainingPlan(DbTrainingPlanRequest(createObj))
            val expected = createObj
            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals(uuidNew, result.data.id)
            assertEquals(expected.title, result.data.title)
            assertEquals(expected.clientCardId, result.data.clientCardId)
            assertNotEquals(TrainingPlanId.NONE, result.data.id)
        }

    companion object : BaseInitTrainingPlans("create") {
        override val initObjects: List<TrainingPlan> = emptyList()
    }
}
