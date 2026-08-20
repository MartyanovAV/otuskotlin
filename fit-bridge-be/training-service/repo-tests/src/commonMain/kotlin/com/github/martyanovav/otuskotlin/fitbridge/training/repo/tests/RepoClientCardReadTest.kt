package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

abstract class RepoClientCardReadTest {
    abstract val repo: IRepoClientCard
    protected open val readSucc = initObjects[0]

    @Test
    fun readSuccess() =
        runRepoTest {
            val result = repo.readClientCard(DbClientCardIdRequest(readSucc))

            assertIs<DbClientCardResponseOk>(result)
            assertEquals(readSucc.id, result.data.id)
            assertEquals(readSucc.ownerUserId, result.data.ownerUserId)
            assertEquals(readSucc.createdByUserId, result.data.createdByUserId)
            assertEquals(readSucc.displayName, result.data.displayName)
            assertEquals(readSucc.note, result.data.note)
            assertEquals(readSucc.isArchived, result.data.isArchived)
        }

    @Test
    fun readNotFound() =
        runRepoTest {
            val result = repo.readClientCard(DbClientCardIdRequest(notFoundId, ClientCardLock.NONE))

            assertIs<DbClientCardResponseErr>(result)
            val error = result.errors.find { it.code == "repo-not-found" }
            assertEquals("id", error?.field)
        }

    companion object : BaseInitClientCards("read") {
        override val initObjects: List<ClientCard> =
            listOf(
                createInitTestModel("read"),
            )

        val notFoundId = ClientCardId("cc-repo-read-notFound")
    }
}
