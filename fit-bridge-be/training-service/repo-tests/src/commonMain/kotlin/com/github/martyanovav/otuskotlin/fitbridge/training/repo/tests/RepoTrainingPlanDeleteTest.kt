package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

abstract class RepoTrainingPlanDeleteTest {
    abstract val repo: IRepoTrainingPlan
    protected open val deleteSucc = initObjects[0]
    protected open val notFoundId = TrainingPlanId("tp-repo-delete-notFound")

    @Test
    fun deleteSuccess() =
        runRepoTest {
            val result = repo.archiveTrainingPlan(DbTrainingPlanIdRequest(deleteSucc))
            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals(deleteSucc.title, result.data.title)
        }

    @Test
    fun deleteNotFound() =
        runRepoTest {
            val result = repo.archiveTrainingPlan(DbTrainingPlanIdRequest(notFoundId, TrainingPlanLock("some-lock")))

            assertIs<DbTrainingPlanResponseErr>(result)
            val error = result.errors.find { it.code == "repo-not-found" }
            assertNotNull(error)
        }

    @Test
    fun deleteConcurrency() =
        runRepoTest {
            val result =
                repo.archiveTrainingPlan(
                    DbTrainingPlanIdRequest(
                        id = deleteSucc.id,
                        lock = TrainingPlanLock("bad-lock"),
                    ),
                )
            assertIs<DbTrainingPlanResponseErrWithData>(result)
            val error = result.errors.find { it.code == "repo-concurrency" }
            assertNotNull(error)
            assertEquals("lock", error.field)
            assertEquals(deleteSucc.id, result.data.id)
            assertEquals(deleteSucc.lock, result.data.lock)
        }

    companion object : BaseInitTrainingPlans("delete") {
        override val initObjects: List<TrainingPlan> =
            listOf(
                createInitTestModel("delete"),
            )
    }
}
