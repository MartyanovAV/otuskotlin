package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseOk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RepoClientCardMockTest {
    private val repo =
        RepoClientCardMock(
            invokeCreateClientCard = { DbClientCardResponseOk(ClientCard(displayName = "create")) },
            invokeReadClientCard = { DbClientCardResponseOk(ClientCard(displayName = "read")) },
            invokeUpdateClientCard = { DbClientCardResponseOk(ClientCard(displayName = "update")) },
            invokeArchiveClientCard = { DbClientCardResponseOk(ClientCard(displayName = "archive")) },
            invokeSearchClientCards = { DbClientCardsResponseOk(Page(items = listOf(ClientCard(displayName = "search")), totalSize = 1)) },
        )

    @Test
    fun mockCreate() =
        runTest {
            val result = repo.createClientCard(DbClientCardRequest(ClientCard()))
            assertIs<DbClientCardResponseOk>(result)
            assertEquals("create", result.data.displayName)
        }

    @Test
    fun mockRead() =
        runTest {
            val result = repo.readClientCard(DbClientCardIdRequest(ClientCard()))
            assertIs<DbClientCardResponseOk>(result)
            assertEquals("read", result.data.displayName)
        }

    @Test
    fun mockUpdate() =
        runTest {
            val result = repo.updateClientCard(DbClientCardRequest(ClientCard()))
            assertIs<DbClientCardResponseOk>(result)
            assertEquals("update", result.data.displayName)
        }

    @Test
    fun mockArchive() =
        runTest {
            val result = repo.archiveClientCard(DbClientCardIdRequest(ClientCard()))
            assertIs<DbClientCardResponseOk>(result)
            assertEquals("archive", result.data.displayName)
        }

    @Test
    fun mockSearch() =
        runTest {
            val result = repo.searchClientCards(DbClientCardFilterRequest())
            assertIs<DbClientCardsResponseOk>(result)
            assertEquals("search", result.data.items.first().displayName)
        }
}
