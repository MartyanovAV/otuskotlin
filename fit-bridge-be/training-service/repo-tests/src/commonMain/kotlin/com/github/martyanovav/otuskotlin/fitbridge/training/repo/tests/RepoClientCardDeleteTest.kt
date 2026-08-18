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
import kotlin.test.assertNotNull

abstract class RepoClientCardDeleteTest {
    abstract val repo: IRepoClientCard
    protected open val deleteSucc = initObjects[0]
    protected open val notFoundId = ClientCardId("cc-repo-delete-notFound")

    @Test
    fun deleteSuccess() =
        runRepoTest {
            val result = repo.archiveClientCard(DbClientCardIdRequest(deleteSucc.id))
            assertIs<DbClientCardResponseOk>(result)
            assertEquals(deleteSucc.displayName, result.data.displayName)
            assertEquals(deleteSucc.note, result.data.note)
        }

    @Test
    fun deleteNotFound() =
        runRepoTest {
            val result = repo.readClientCard(DbClientCardIdRequest(notFoundId))

            assertIs<DbClientCardResponseErr>(result)
            val error = result.errors.find { it.code == "repo-not-found" }
            assertNotNull(error)
        }

    companion object : BaseInitClientCards("delete") {
        override val initObjects: List<ClientCard> =
            listOf(
                createInitTestModel("delete"),
            )
    }
}
