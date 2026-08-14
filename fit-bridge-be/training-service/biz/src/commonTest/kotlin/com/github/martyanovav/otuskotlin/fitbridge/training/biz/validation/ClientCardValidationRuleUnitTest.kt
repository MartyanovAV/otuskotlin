package com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.rootChain
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardFilter
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ClientCardValidationRuleUnitTest {
    @Test
    fun `client card id must not be empty`() = runTest {
        assertRule(
            card = ClientCard(id = ClientCardId.NONE),
            expectedCode = "validation-id-empty",
        ) { validateClientCardIdNotEmpty("validate id") }
    }

    @Test
    fun `client card id must have valid format`() = runTest {
        assertRule(
            card = ClientCard(id = ClientCardId("bad id")),
            expectedCode = "validation-id-badFormat",
        ) { validateClientCardIdFormat("validate id") }
    }

    @Test
    fun `display name must not be empty`() = runTest {
        assertRule(
            card = ClientCard(displayName = ""),
            expectedCode = "validation-displayName-empty",
        ) { validateClientCardDisplayNameNotEmpty("validate display name") }
    }

    @Test
    fun `display name must satisfy maximum length`() = runTest {
        assertRule(
            card = ClientCard(displayName = "a".repeat(121)),
            expectedCode = "validation-displayName-tooLong",
        ) { validateClientCardDisplayNameMaxLength("validate display name") }
    }

    @Test
    fun `display name must contain letters`() = runTest {
        assertRule(
            card = ClientCard(displayName = "123--"),
            expectedCode = "validation-displayName-noContent",
        ) { validateClientCardDisplayNameHasContent("validate display name") }
    }

    @Test
    fun `note must satisfy maximum length`() = runTest {
        assertRule(
            card = ClientCard(note = "a".repeat(1001)),
            expectedCode = "validation-note-tooLong",
        ) { validateClientCardNoteMaxLength("validate note") }
    }

    @Test
    fun `lock must not be empty`() = runTest {
        assertRule(
            card = ClientCard(lock = ""),
            expectedCode = "validation-lock-empty",
        ) { validateClientCardLockNotEmpty("validate lock") }
    }

    @Test
    fun `lock must have valid format`() = runTest {
        assertRule(
            card = ClientCard(lock = "bad lock"),
            expectedCode = "validation-lock-badFormat",
        ) { validateClientCardLockFormat("validate lock") }
    }

    @Test
    fun `search string must satisfy maximum length`() = runTest {
        assertRule(
            filter = ClientCardFilter(searchString = "a".repeat(121)),
            expectedCode = "validation-searchString-tooLong",
        ) { validateClientCardSearchStringLength("validate search string") }
    }

    @Test
    fun `page number must be positive`() = runTest {
        assertRule(
            filter = ClientCardFilter(pageNumber = 0),
            expectedCode = "validation-pageNumber-outOfRange",
        ) { validateClientCardPageNumber("validate page number") }
    }

    @Test
    fun `page size must be within supported range`() = runTest {
        assertRule(
            filter = ClientCardFilter(pageSize = 101),
            expectedCode = "validation-pageSize-outOfRange",
        ) { validateClientCardPageSize("validate page size") }
    }

    private suspend fun assertRule(
        card: ClientCard = ClientCard(),
        filter: ClientCardFilter = ClientCardFilter(),
        expectedCode: String,
        rule: ICorChainDsl<IFBContext>.() -> Unit,
    ) {
        val context = ClientCardContext(
            state = State.RUNNING,
            clientCardValidating = card,
            clientCardFilterValidating = filter,
        )

        rootChain(rule).build().exec(context)

        assertEquals(State.FAILING, context.state)
        assertEquals(expectedCode, context.errors.single().code)
    }
}
