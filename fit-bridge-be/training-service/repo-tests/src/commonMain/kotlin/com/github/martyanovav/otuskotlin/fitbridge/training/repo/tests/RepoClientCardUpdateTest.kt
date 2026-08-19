package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErrWithData
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

abstract class RepoClientCardUpdateTest {
    abstract val repo: IRepoClientCard
    protected open val updateSucc = initObjects[0]
    protected val updateIdNotFound = ClientCardId("cc-repo-update-not-found")

    private val reqUpdateSucc by lazy {
        ClientCard(
            id = updateSucc.id,
            ownerId = "owner-123",
            displayName = "update object",
            note = "update object description",
            lock = updateSucc.lock,
        )
    }
    private val reqUpdateNotFound =
        ClientCard(
            id = updateIdNotFound,
            ownerId = "owner-123",
            displayName = "update object not found",
            note = "update object not found description",
        )
    private val reqUpdateConc by lazy {
        ClientCard(
            id = updateSucc.id,
            ownerId = "owner-123",
            displayName = "update object",
            note = "update object description",
            lock = ClientCardLock("bad-lock"),
        )
    }

    @Test
    fun updateSuccess() =
        runRepoTest {
            val result = repo.updateClientCard(DbClientCardRequest(reqUpdateSucc))
            assertIs<DbClientCardResponseOk>(result)
            assertEquals(reqUpdateSucc.id, result.data.id)
            assertEquals(reqUpdateSucc.displayName, result.data.displayName)
            assertEquals(reqUpdateSucc.note, result.data.note)
        }

    @Test
    fun updateNotFound() =
        runRepoTest {
            val result = repo.updateClientCard(DbClientCardRequest(reqUpdateNotFound))
            assertIs<DbClientCardResponseErr>(result)
            val error = result.errors.find { it.code == "repo-not-found" }
            assertEquals("id", error?.field)
        }

    @Test
    fun updateConcurrency() =
        runRepoTest {
            val result = repo.updateClientCard(DbClientCardRequest(reqUpdateConc))
            assertIs<DbClientCardResponseErrWithData>(result)
            val error = result.errors.find { it.code == "repo-concurrency" }
            assertEquals("lock", error?.field)
            assertEquals(updateSucc.id, result.data.id)
            assertEquals(updateSucc.lock, result.data.lock)
        }

    companion object : BaseInitClientCards("update") {
        override val initObjects: List<ClientCard> =
            listOf(
                createInitTestModel("update"),
            )
    }
}
