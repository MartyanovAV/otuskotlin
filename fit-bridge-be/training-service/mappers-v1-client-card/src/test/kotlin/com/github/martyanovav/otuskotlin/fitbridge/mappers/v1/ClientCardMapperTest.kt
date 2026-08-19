package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardReadResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchFilter
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardStatus
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import kotlin.test.Test
import kotlin.test.assertEquals
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardStatus as InternalClientCardStatus

class ClientCardMapperTest {
    @Test
    fun `client card search request maps to context`() {
        val req =
            ClientCardSearchRequest(
                requestType = "clientCard.search",
                requestId = "cc-search-1",
                debug = Debug(mode = RequestDebugMode.TEST),
                clientCardFilter =
                    ClientCardSearchFilter(
                        status = ClientCardStatus.ARCHIVED,
                        searchString = "Ann",
                        pageNumber = 2,
                        pageSize = 25,
                    )
            )

        val context = req.fromTransport()

        assertEquals(ClientCardCommand.SEARCH, context.command)
        assertEquals(WorkMode.TEST, context.workMode)
        assertEquals(InternalClientCardStatus.ARCHIVED, context.clientCardFilter.status)
        assertEquals("Ann", context.clientCardFilter.searchString)
        assertEquals(2, context.clientCardsResponse.pageNumber)
        assertEquals(25, context.clientCardsResponse.pageSize)
    }

    @Test
    fun `client card search context maps to response`() {
        val context =
            ClientCardContext(
                requestId = RequestId("cc-res-1"),
                command = ClientCardCommand.SEARCH,
                state = State.RUNNING,
                clientCardsResponse =
                    Page(
                        items = listOf(ClientCard(id = ClientCardId("client-1"), displayName = "Ann")),
                        totalSize = 1,
                        pageNumber = 1,
                        pageSize = 10,
                    )
            )

        val response = context.toTransport() as ClientCardSearchResponse

        assertEquals("cc-res-1", response.requestId)
        assertEquals("Ann", response.clientCards?.firstOrNull()?.displayName)
        assertEquals(1, response.totalSize)
    }

    @Test
    fun `client card response maps status to transport`() {
        val activeContext =
            ClientCardContext(
                requestId = RequestId("cc-res-2"),
                command = ClientCardCommand.READ,
                state = State.RUNNING,
                clientCardResponse = ClientCard(id = ClientCardId("client-2"), displayName = "Ann", isArchived = false),
            )

        val activeResponse = activeContext.toTransport() as ClientCardReadResponse

        assertEquals(ClientCardStatus.ACTIVE, activeResponse.clientCard?.status)

        val archivedContext =
            ClientCardContext(
                requestId = RequestId("cc-res-3"),
                command = ClientCardCommand.READ,
                state = State.RUNNING,
                clientCardResponse = ClientCard(id = ClientCardId("client-3"), displayName = "Ann", isArchived = true),
            )

        val archivedResponse = archivedContext.toTransport() as ClientCardReadResponse

        assertEquals(ClientCardStatus.ARCHIVED, archivedResponse.clientCard?.status)
    }

    @Test
    fun `client card response maps all fields to transport`() {
        val context =
            ClientCardContext(
                requestId = RequestId("cc-res-4"),
                command = ClientCardCommand.READ,
                state = State.RUNNING,
                clientCardResponse =
                    ClientCard(
                        id = ClientCardId("client-4"),
                        displayName = "Ann",
                        note = "Prefers morning sessions",
                        isArchived = false,
                        createdAt = "2026-01-01T10:00:00Z",
                        updatedAt = "2026-01-02T11:30:00Z",
                        lock = ClientCardLock("lock-cc-1"),
                    ),
            )

        val response = context.toTransport() as ClientCardReadResponse

        assertEquals("client-4", response.clientCard?.id)
        assertEquals("Ann", response.clientCard?.displayName)
        assertEquals("Prefers morning sessions", response.clientCard?.note)
        assertEquals(ClientCardStatus.ACTIVE, response.clientCard?.status)
        assertEquals("2026-01-01T10:00:00Z", response.clientCard?.createdAt)
        assertEquals("2026-01-02T11:30:00Z", response.clientCard?.updatedAt)
        assertEquals("lock-cc-1", response.clientCard?.lock)
    }

    @Test
    fun `client card response maps archived flag to status`() {
        val cases =
            listOf(
                Pair(false, ClientCardStatus.ACTIVE),
                Pair(true, ClientCardStatus.ARCHIVED),
            )

        cases.forEach { (isArchived, expectedStatus) ->
            val context =
                ClientCardContext(
                    requestId = RequestId("cc-res-5"),
                    command = ClientCardCommand.READ,
                    state = State.RUNNING,
                    clientCardResponse =
                        ClientCard(
                            id = ClientCardId("client-5"),
                            displayName = "Ann",
                            isArchived = isArchived,
                        ),
                )

            val response = context.toTransport() as ClientCardReadResponse

            assertEquals(expectedStatus, response.clientCard?.status, "Unexpected status for isArchived=$isArchived")
        }
    }

    @Test
    fun `client card search filter status values map to domain`() {
        val cases =
            listOf(
                ClientCardStatus.ACTIVE to InternalClientCardStatus.ACTIVE,
                ClientCardStatus.ARCHIVED to InternalClientCardStatus.ARCHIVED,
                null to InternalClientCardStatus.NONE,
            )

        cases.forEach { (transportStatus, expected) ->
            val request =
                ClientCardSearchRequest(
                    requestType = "clientCard.search",
                    requestId = "cc-search-2",
                    debug = Debug(mode = RequestDebugMode.TEST),
                    clientCardFilter = ClientCardSearchFilter(status = transportStatus),
                )

            val context = request.fromTransport()

            assertEquals(expected, context.clientCardFilter.status, "Unexpected domain status for $transportStatus")
        }
    }
}
