package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkoutDifficulty
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

abstract class RepoTrainingPlanCompleteTest {
    abstract val repo: IRepoTrainingPlan

    @Test
    fun completeActivePlan() =
        runRepoTest {
            val result =
                repo.completeTrainingPlan(
                    DbTrainingPlanRequest(
                        activePlan.copy(
                            completedAt = "2026-08-22T10:00:00Z",
                            difficulty = WorkoutDifficulty.HARD,
                            coachComment = "Strong session",
                        ),
                    ),
                )

            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals(TrainingPlanStatus.COMPLETED, result.data.status)
            assertEquals("2026-08-22T10:00:00Z", result.data.completedAt)
            assertEquals(WorkoutDifficulty.HARD, result.data.difficulty)
            assertEquals("Strong session", result.data.coachComment)
            assertNotEquals(activePlan.lock, result.data.lock)
        }

    @Test
    fun completeArchivedPlanIsRejected() =
        runRepoTest {
            val result = repo.completeTrainingPlan(DbTrainingPlanRequest(archivedPlan))

            assertIs<DbTrainingPlanResponseErrWithData>(result)
            assertEquals("repo-invalid-status", result.errors.single().code)
            assertEquals(TrainingPlanStatus.ARCHIVED, result.data.status)
        }

    @Test
    fun completeAlreadyCompletedPlanIsRejected() =
        runRepoTest {
            val result = repo.completeTrainingPlan(DbTrainingPlanRequest(completedPlan))

            assertIs<DbTrainingPlanResponseErrWithData>(result)
            assertEquals("repo-invalid-status", result.errors.single().code)
            assertEquals(TrainingPlanStatus.COMPLETED, result.data.status)
        }

    companion object : BaseInitTrainingPlans("complete") {
        val activePlan = createInitTestModel("active")
        val archivedPlan = createInitTestModel("archived").copy(status = TrainingPlanStatus.ARCHIVED)
        val completedPlan = createInitTestModel("completed").copy(status = TrainingPlanStatus.COMPLETED)
        override val initObjects: List<TrainingPlan> = listOf(activePlan, archivedPlan, completedPlan)
    }
}
