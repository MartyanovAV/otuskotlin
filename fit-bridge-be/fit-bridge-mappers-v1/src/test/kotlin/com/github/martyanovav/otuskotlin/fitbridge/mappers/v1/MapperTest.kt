package com.github.martyanovav.otuskotlin.fitbridge.mappers.v1

import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.Debug
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugMode
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.RequestDebugStubs
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchFilter
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardSearchResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.ClientCardStatus
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchFilter
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanSearchResponse
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateObject
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateRequest
import com.github.martyanovav.otuskotlin.fitbridge.api.v1.models.TrainerProfileCreateOrUpdateResponse
import com.github.martyanovav.otuskotlin.fitbridge.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainerProfileContext
import com.github.martyanovav.otuskotlin.fitbridge.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.common.models.RequestId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainer.TrainerProfile
import com.github.martyanovav.otuskotlin.fitbridge.common.models.TrainerProfileCommand
import com.github.martyanovav.otuskotlin.fitbridge.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.common.models.clientcard.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.common.models.trainingplan.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.common.stubs.Stubs
import org.junit.Test
import kotlin.test.assertEquals

class MapperTest {

    @Test
    fun fromTransport() {
        val req = TrainerProfileCreateOrUpdateRequest(
            requestType = "trainerProfile.createOrUpdate",
            requestId = "12345",
            debug = Debug(
                mode = RequestDebugMode.STUB,
                stub = RequestDebugStubs.SUCCESS
            ),
            trainerProfile = TrainerProfileCreateOrUpdateObject(
                publicName = "Trainer John",
                specialization = "Yoga"
            )
        )

        val context = req.fromTransport() as TrainerProfileContext

        assertEquals(Stubs.SUCCESS, context.stubCase)
        assertEquals(WorkMode.STUB, context.workMode)
        assertEquals("Trainer John", context.trainerProfileRequest.publicName)
        assertEquals("Yoga", context.trainerProfileRequest.specialization)
    }

    @Test
    fun toTransport() {
        val context = TrainerProfileContext(
            requestId = RequestId("1234"),
            command = TrainerProfileCommand.CREATE_OR_UPDATE,
            trainerProfileResponse = TrainerProfile(
                publicName = "Trainer John",
                specialization = "Yoga"
            ),
            state = State.RUNNING
        ).apply {
            addError(
                FBError(
                    code = "err",
                    group = "request",
                    field = "publicName",
                    message = "wrong name"
                )
            )
        }

        val req = context.toTransport() as TrainerProfileCreateOrUpdateResponse

        assertEquals("1234", req.requestId)
        assertEquals("Trainer John", req.trainerProfile?.publicName)
        assertEquals("Yoga", req.trainerProfile?.specialization)
        assertEquals(1, req.errors?.size)
        assertEquals("err", req.errors?.firstOrNull()?.code)
        assertEquals("request", req.errors?.firstOrNull()?.group)
        assertEquals("publicName", req.errors?.firstOrNull()?.field)
        assertEquals("wrong name", req.errors?.firstOrNull()?.message)
    }

    @Test
    fun clientCardSearchFromTransport() {
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

        val context = req.fromTransport() as ClientCardContext

        assertEquals(ClientCardCommand.SEARCH, context.command)
        assertEquals(WorkMode.TEST, context.workMode)
        assertEquals("ARCHIVED", context.clientCardFilter.status)
        assertEquals("Ann", context.clientCardFilter.searchString)
        assertEquals(2, context.clientCardFilter.pageNumber)
        assertEquals(25, context.clientCardFilter.pageSize)
        assertEquals(2, context.clientCardsResponse.pageNumber)
        assertEquals(25, context.clientCardsResponse.pageSize)
    }

    @Test
    fun trainingPlanSearchFromTransport() {
        val req = TrainingPlanSearchRequest(
            requestType = "trainingPlan.search",
            requestId = "tp-search-1",
            debug = Debug(mode = RequestDebugMode.STUB, stub = RequestDebugStubs.SUCCESS),
            trainingPlanFilter = TrainingPlanSearchFilter(
                clientCardId = "client-42",
                status = TrainingPlanStatus.ACTIVE,
                searchString = "legs",
                pageNumber = 3,
                pageSize = 15,
            )
        )

        val context = req.fromTransport() as TrainingPlanContext

        assertEquals(TrainingPlanCommand.SEARCH, context.command)
        assertEquals(Stubs.SUCCESS, context.stubCase)
        assertEquals("client-42", context.trainingPlanFilter.clientCardId.asString())
        assertEquals("ACTIVE", context.trainingPlanFilter.status)
        assertEquals("legs", context.trainingPlanFilter.searchString)
        assertEquals(3, context.trainingPlanFilter.pageNumber)
        assertEquals(15, context.trainingPlanFilter.pageSize)
        assertEquals(3, context.trainingPlansResponse.pageNumber)
        assertEquals(15, context.trainingPlansResponse.pageSize)
    }

    @Test
    fun searchResponsesToTransport() {
        val clientCardContext = ClientCardContext(
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
        val trainingPlanContext = TrainingPlanContext(
            requestId = RequestId("tp-res-1"),
            command = TrainingPlanCommand.SEARCH,
            state = State.RUNNING,
            trainingPlansResponse = Page(
                items = listOf(TrainingPlan(id = TrainingPlanId("plan-1"), title = "Leg day", clientCardId = ClientCardId("client-1"))),
                totalSize = 1,
                pageNumber = 2,
                pageSize = 20,
            )
        )

        val clientCardResponse = clientCardContext.toTransport() as ClientCardSearchResponse
        val trainingPlanResponse = trainingPlanContext.toTransport() as TrainingPlanSearchResponse

        assertEquals("cc-res-1", clientCardResponse.requestId)
        assertEquals("Ann", clientCardResponse.clientCards?.firstOrNull()?.displayName)
        assertEquals(1, clientCardResponse.totalSize)
        assertEquals("tp-res-1", trainingPlanResponse.requestId)
        assertEquals("Leg day", trainingPlanResponse.trainingPlans?.firstOrNull()?.title)
        assertEquals("client-1", trainingPlanResponse.trainingPlans?.firstOrNull()?.clientCardId)
        assertEquals(2, trainingPlanResponse.pageNumber)
    }
}
