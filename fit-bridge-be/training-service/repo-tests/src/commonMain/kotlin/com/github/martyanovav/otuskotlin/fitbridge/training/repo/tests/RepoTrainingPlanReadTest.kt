package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

abstract class RepoTrainingPlanReadTest {
    abstract val repo: IRepoTrainingPlan
    protected open val readSucc = initObjects[0]

    @Test
    fun readSuccess() =
        runRepoTest {
            val result = repo.readTrainingPlan(DbTrainingPlanIdRequest(readSucc))

            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals(readSucc.id, result.data.id)
            assertEquals(readSucc.clientCardId, result.data.clientCardId)
            assertEquals(readSucc.ownerId, result.data.ownerId)
            assertEquals(readSucc.title, result.data.title)
            assertEquals(readSucc.status, result.data.status)
        }

    @Test
    fun readNotFound() =
        runRepoTest {
            val result = repo.readTrainingPlan(DbTrainingPlanIdRequest(notFoundId, TrainingPlanLock.NONE))

            assertIs<DbTrainingPlanResponseErr>(result)
            val error = result.errors.find { it.code == "repo-not-found" }
            assertEquals("id", error?.field)
        }

    companion object : BaseInitTrainingPlans("read") {
        override val initObjects: List<TrainingPlan> =
            listOf(
                createInitTestModel("read"),
            )

        val notFoundId = TrainingPlanId("tp-repo-read-notFound")
    }
}
