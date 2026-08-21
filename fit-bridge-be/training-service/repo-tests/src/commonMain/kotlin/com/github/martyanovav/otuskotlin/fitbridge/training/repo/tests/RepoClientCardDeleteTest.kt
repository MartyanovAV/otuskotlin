package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

abstract class RepoClientCardDeleteTest {
    abstract val repo: IRepoClientCard
    protected open val deleteSucc = initObjects[0]
    protected open val notFoundId = ClientCardId("cc-repo-delete-notFound")

    @Test
    fun deleteSuccess() =
        runRepoTest {
            val result = repo.archiveClientCard(DbClientCardIdRequest(deleteSucc))
            assertIs<DbClientCardResponseOk>(result)
            assertEquals(deleteSucc.displayName, result.data.displayName)
            assertEquals(deleteSucc.note, result.data.note)
        }

    @Test
    fun deleteNotFound() =
        runRepoTest {
            val result = repo.archiveClientCard(DbClientCardIdRequest(notFoundId, ClientCardLock("some-lock")))

            assertIs<DbClientCardResponseErr>(result)
            val error = result.errors.find { it.code == "repo-not-found" }
            assertNotNull(error)
        }

    @Test
    fun deleteConcurrency() =
        runRepoTest {
            val result =
                repo.archiveClientCard(
                    DbClientCardIdRequest(
                        id = deleteSucc.id,
                        lock = ClientCardLock("bad-lock"),
                    ),
                )
            assertIs<DbClientCardResponseErrWithData>(result)
            val error = result.errors.find { it.code == "repo-concurrency" }
            assertNotNull(error)
            assertEquals("lock", error.field)
            assertEquals(deleteSucc.id, result.data.id)
            assertEquals(deleteSucc.lock, result.data.lock)
        }

    companion object : BaseInitClientCards("delete") {
        override val initObjects: List<ClientCard> =
            listOf(
                createInitTestModel("delete"),
            )
    }
}
