package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.IRepoClientCardInitializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

abstract class RepoClientCardCreateTest {
    abstract val repo: IRepoClientCardInitializable
    protected open val uuidNew = ClientCardId("10000000-0000-0000-0000-000000000001")

    private val createObj =
        ClientCard(
            ownerId = "owner-123",
            displayName = "create object",
            note = "create object description",
        )

    @Test
    fun createSuccess() =
        runRepoTest {
            val result = repo.createClientCard(DbClientCardRequest(createObj))
            val expected = createObj
            assertIs<DbClientCardResponseOk>(result)
            assertEquals(uuidNew, result.data.id)
            assertEquals(expected.displayName, result.data.displayName)
            assertEquals(expected.note, result.data.note)
            assertNotEquals(ClientCardId.NONE, result.data.id)
        }

    companion object : BaseInitClientCards("create") {
        override val initObjects: List<ClientCard> = emptyList()
    }
}
