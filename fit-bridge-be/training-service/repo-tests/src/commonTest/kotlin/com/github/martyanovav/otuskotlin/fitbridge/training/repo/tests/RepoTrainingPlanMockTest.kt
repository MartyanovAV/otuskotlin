package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseOk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RepoTrainingPlanMockTest {
    private val repo =
        RepoTrainingPlanMock(
            invokeCreateTrainingPlan = { DbTrainingPlanResponseOk(TrainingPlan(title = "create")) },
            invokeReadTrainingPlan = { DbTrainingPlanResponseOk(TrainingPlan(title = "read")) },
            invokeUpdateTrainingPlan = { DbTrainingPlanResponseOk(TrainingPlan(title = "update")) },
            invokeArchiveTrainingPlan = { DbTrainingPlanResponseOk(TrainingPlan(title = "archive")) },
            invokeSearchTrainingPlans = { DbTrainingPlansResponseOk(listOf(TrainingPlan(title = "search"))) },
        )

    @Test
    fun mockCreate() =
        runTest {
            val result = repo.createTrainingPlan(DbTrainingPlanRequest(TrainingPlan()))
            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals("create", result.data.title)
        }

    @Test
    fun mockRead() =
        runTest {
            val result = repo.readTrainingPlan(DbTrainingPlanIdRequest(TrainingPlan()))
            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals("read", result.data.title)
        }

    @Test
    fun mockUpdate() =
        runTest {
            val result = repo.updateTrainingPlan(DbTrainingPlanRequest(TrainingPlan()))
            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals("update", result.data.title)
        }

    @Test
    fun mockArchive() =
        runTest {
            val result = repo.archiveTrainingPlan(DbTrainingPlanIdRequest(TrainingPlan()))
            assertIs<DbTrainingPlanResponseOk>(result)
            assertEquals("archive", result.data.title)
        }

    @Test
    fun mockSearch() =
        runTest {
            val result = repo.searchTrainingPlans(DbTrainingPlanFilterRequest())
            assertIs<DbTrainingPlansResponseOk>(result)
            assertEquals("search", result.data.first().title)
        }
}
