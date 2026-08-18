package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
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
            val result = repo.readClientCard(DbClientCardIdRequest(readSucc.id))

            assertIs<DbClientCardResponseOk>(result)
            assertEquals(readSucc, result.data)
        }

    @Test
    fun readNotFound() =
        runRepoTest {
            val result = repo.readClientCard(DbClientCardIdRequest(notFoundId))

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
