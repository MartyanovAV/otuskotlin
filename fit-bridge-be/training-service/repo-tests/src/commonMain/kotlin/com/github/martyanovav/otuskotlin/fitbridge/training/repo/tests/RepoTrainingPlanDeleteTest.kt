package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErr
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
            val result = repo.archiveTrainingPlan(DbTrainingPlanIdRequest(deleteSucc.id))
            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals(deleteSucc.title, result.data.title)
        }

    @Test
    fun deleteNotFound() =
        runRepoTest {
            val result = repo.readTrainingPlan(DbTrainingPlanIdRequest(notFoundId))

            assertIs<DbTrainingPlanResponseErr>(result)
            val error = result.errors.find { it.code == "repo-not-found" }
            assertNotNull(error)
        }

    companion object : BaseInitTrainingPlans("delete") {
        override val initObjects: List<TrainingPlan> =
            listOf(
                createInitTestModel("delete"),
            )
    }
}
