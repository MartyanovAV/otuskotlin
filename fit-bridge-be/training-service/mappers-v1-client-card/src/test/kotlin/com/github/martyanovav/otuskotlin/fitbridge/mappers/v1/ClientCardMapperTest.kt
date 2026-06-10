package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchFilter
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardStatus
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import kotlin.test.Test
import kotlin.test.assertEquals

class ClientCardMapperTest {
    @Test
    fun `client card search request maps to context`() {
        val req = ClientCardSearchRequest(
            requestType = "clientCard.search",
            requestId = "cc-search-1",
            debug = Debug(mode = RequestDebugMode.TEST),
            clientCardFilter = ClientCardSearchFilter(
                status = ClientCardStatus.ARCHIVED,
                searchString = "Ann",
                pageNumber = 2,
                pageSize = 25,
            )
        )

        val context = req.fromTransport()

        assertEquals(ClientCardCommand.SEARCH, context.command)
        assertEquals(WorkMode.TEST, context.workMode)
        assertEquals("ARCHIVED", context.clientCardFilter.status)
        assertEquals("Ann", context.clientCardFilter.searchString)
        assertEquals(2, context.clientCardsResponse.pageNumber)
        assertEquals(25, context.clientCardsResponse.pageSize)
    }

    @Test
    fun `client card search context maps to response`() {
        val context = ClientCardContext(
            requestId = RequestId("cc-res-1"),
            command = ClientCardCommand.SEARCH,
            state = State.RUNNING,
            clientCardsResponse = Page(
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
}
