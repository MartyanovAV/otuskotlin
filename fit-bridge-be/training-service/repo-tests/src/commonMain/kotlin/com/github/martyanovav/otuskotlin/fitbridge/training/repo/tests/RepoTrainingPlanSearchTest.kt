package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

abstract class RepoTrainingPlanSearchTest {
    abstract val repo: IRepoTrainingPlan

    protected open val initializedObjects: List<TrainingPlan> = initObjects

    @Test
    fun searchByClientCardId() =
        runRepoTest {
            val result = repo.searchTrainingPlans(DbTrainingPlanFilterRequest(clientCardId = searchClientCardId))
            assertIs<DbTrainingPlansResponseOk>(result)
            val expected = listOf(initializedObjects[1], initializedObjects[3]).sortedBy { it.id.asString() }
            assertEquals(expected.size, result.data.items.size)
        }

    @Test
    fun searchByTitle() =
        runRepoTest {
            val result = repo.searchTrainingPlans(DbTrainingPlanFilterRequest(searchString = "ad1"))
            assertIs<DbTrainingPlansResponseOk>(result)
            assertEquals(1, result.data.items.size)
        }

    @Test
    fun searchByClientCardIdAndTitle() =
        runRepoTest {
            val result =
                repo.searchTrainingPlans(
                    DbTrainingPlanFilterRequest(
                        clientCardId = searchClientCardId,
                        searchString = "ad2",
                    ),
                )
            assertIs<DbTrainingPlansResponseOk>(result)
            assertEquals(1, result.data.items.size)
            assertEquals(initializedObjects[1].id, result.data.items[0].id)
        }

    @Test
    fun searchByOwnerUserId() =
        runRepoTest {
            val result = repo.searchTrainingPlans(DbTrainingPlanFilterRequest(ownerUserId = "owner-124"))
            assertIs<DbTrainingPlansResponseOk>(result)
            assertEquals(setOf(initializedObjects[1].id, initializedObjects[3].id), result.data.items.map { it.id }.toSet())
        }

    @Test
    fun searchAll() =
        runRepoTest {
            val result = repo.searchTrainingPlans(DbTrainingPlanFilterRequest())
            assertIs<DbTrainingPlansResponseOk>(result)
            assertEquals(initializedObjects.size, result.data.items.size)
        }

    companion object : BaseInitTrainingPlans("search") {
        val searchClientCardId = ClientCardId("cc-search-owner")
        override val initObjects: List<TrainingPlan> =
            listOf(
                createInitTestModel("ad1"),
                createInitTestModel("ad2", clientCardId = searchClientCardId, ownerUserId = "owner-124"),
                createInitTestModel("ad3"),
                createInitTestModel("ad4", clientCardId = searchClientCardId, ownerUserId = "owner-124"),
                createInitTestModel("ad5"),
            )
    }
}
