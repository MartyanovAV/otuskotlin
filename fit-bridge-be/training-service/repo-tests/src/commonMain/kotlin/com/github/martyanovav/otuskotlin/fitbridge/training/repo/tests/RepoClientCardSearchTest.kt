package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

abstract class RepoClientCardSearchTest {
    abstract val repo: IRepoClientCard

    protected open val initializedObjects: List<ClientCard> = initObjects

    @Test
    fun searchByDisplayName() =
        runRepoTest {
            val result = repo.searchClientCards(DbClientCardFilterRequest(searchString = "ad1"))
            assertIs<DbClientCardsResponseOk>(result)
            val expected = listOf(initializedObjects[0])
            assertEquals(expected.size, result.data.items.size)
        }

    @Test
    fun searchByStatusAndDisplayName() =
        runRepoTest {
            val result =
                repo.searchClientCards(
                    DbClientCardFilterRequest(
                        status = ClientCardStatus.ACTIVE,
                        searchString = "ad2",
                    ),
                )
            assertIs<DbClientCardsResponseOk>(result)
            assertEquals(1, result.data.items.size)
            assertEquals(initializedObjects[1].id, result.data.items[0].id)
        }

    @Test
    fun searchByOwnerUserId() =
        runRepoTest {
            val result = repo.searchClientCards(DbClientCardFilterRequest(ownerUserId = "owner-124"))
            assertIs<DbClientCardsResponseOk>(result)
            assertEquals(setOf(initializedObjects[1].id, initializedObjects[3].id), result.data.items.map { it.id }.toSet())
        }

    @Test
    fun searchAll() =
        runRepoTest {
            val result = repo.searchClientCards(DbClientCardFilterRequest())
            assertIs<DbClientCardsResponseOk>(result)
            assertEquals(initializedObjects.size, result.data.items.size)
        }

    @Test
    fun searchUsesPagination() =
        runRepoTest {
            val first = repo.searchClientCards(DbClientCardFilterRequest(pageNumber = 1, pageSize = 2))
            val second = repo.searchClientCards(DbClientCardFilterRequest(pageNumber = 2, pageSize = 2))

            assertIs<DbClientCardsResponseOk>(first)
            assertIs<DbClientCardsResponseOk>(second)
            assertEquals(2, first.data.items.size)
            assertEquals(2, second.data.items.size)
            assertEquals(initializedObjects.size, first.data.totalSize)
            assertEquals(initializedObjects.size, second.data.totalSize)
            assertEquals(1, first.data.pageNumber)
            assertEquals(2, second.data.pageNumber)
            assertTrue(first.data.items.map { it.id }.toSet().intersect(second.data.items.map { it.id }.toSet()).isEmpty())
        }

    companion object : BaseInitClientCards("search") {
        override val initObjects: List<ClientCard> =
            listOf(
                createInitTestModel("ad1"),
                createInitTestModel("ad2", ownerUserId = "owner-124"),
                createInitTestModel("ad3"),
                createInitTestModel("ad4", ownerUserId = "owner-124"),
                createInitTestModel("ad5"),
            )
    }
}
