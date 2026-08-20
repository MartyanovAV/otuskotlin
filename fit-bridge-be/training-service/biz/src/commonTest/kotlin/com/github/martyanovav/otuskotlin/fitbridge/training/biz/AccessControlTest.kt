package com.github.martyanovav.otuskotlin.fitbridge.training.biz

import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.AuthPrincipal
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ExerciseItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoClientCardInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoTrainingPlanInMemory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AccessControlTest {
    private val userA = AuthPrincipal(userId = "user-a", roles = setOf(AuthPrincipal.TRAINER_ROLE))
    private val userB = AuthPrincipal(userId = "user-b", roles = setOf(AuthPrincipal.TRAINER_ROLE))
    private val clientCardA = clientCard("client-a", userA.userId)
    private val clientCardB = clientCard("client-b", userB.userId)
    private val trainingPlanA = trainingPlan("plan-a", clientCardA.id, userA.userId)
    private val trainingPlanB = trainingPlan("plan-b", clientCardB.id, userB.userId)
    private val clientCardRepo = RepoClientCardInMemory().apply { save(listOf(clientCardA, clientCardB)) }
    private val trainingPlanRepo = RepoTrainingPlanInMemory().apply { save(listOf(trainingPlanA, trainingPlanB)) }
    private val processor =
        TrainingProcessor(
            CorSettings(
                repoClientCardTest = clientCardRepo,
                repoTrainingPlanTest = trainingPlanRepo,
            ),
        )

    @Test
    fun searchReturnsOnlyPrincipalOwnedEntities() =
        runTest {
            val cardsContext =
                ClientCardContext(
                    command = ClientCardCommand.SEARCH,
                    workMode = WorkMode.TEST,
                    principal = userA,
                )
            val plansContext =
                TrainingPlanContext(
                    command = TrainingPlanCommand.SEARCH,
                    workMode = WorkMode.TEST,
                    principal = userA,
                )

            processor.exec(cardsContext)
            processor.exec(plansContext)

            assertEquals(listOf(clientCardA.id), cardsContext.clientCardsResponse.items.map { it.id })
            assertEquals(listOf(trainingPlanA.id), plansContext.trainingPlansResponse.items.map { it.id })
        }

    @Test
    fun anonymousSearchIsDenied() =
        runTest {
            val cardsContext =
                ClientCardContext(
                    command = ClientCardCommand.SEARCH,
                    workMode = WorkMode.TEST,
                )
            val plansContext =
                TrainingPlanContext(
                    command = TrainingPlanCommand.SEARCH,
                    workMode = WorkMode.TEST,
                )

            processor.exec(cardsContext)
            processor.exec(plansContext)

            assertEquals(State.FAILING, cardsContext.state)
            assertEquals("access-denied", cardsContext.errors.single().code)
            assertEquals(State.FAILING, plansContext.state)
            assertEquals("access-denied", plansContext.errors.single().code)
        }

    @Test
    fun authenticatedUserWithoutTrainerRoleIsDenied() =
        runTest {
            val context =
                ClientCardContext(
                    command = ClientCardCommand.SEARCH,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-without-role"),
                )

            processor.exec(context)

            assertEquals(State.FAILING, context.state)
            assertEquals("access-denied", context.errors.single().code)
        }

    @Test
    fun unauthorizedTrainingPlanCreateDoesNotRevealClientCardExistence() =
        runTest {
            val context =
                TrainingPlanContext(
                    command = TrainingPlanCommand.CREATE,
                    workMode = WorkMode.TEST,
                    principal = AuthPrincipal(userId = "user-without-role"),
                    trainingPlanRequest =
                        trainingPlan(
                            id = "new-plan",
                            clientCardId = ClientCardId("missing-client-card"),
                            userId = "user-without-role",
                        ),
                )

            processor.exec(context)

            assertEquals(State.FAILING, context.state)
            assertEquals("access-denied", context.errors.single().code)
        }

    @Test
    fun readingAnotherUsersEntityIsDenied() =
        runTest {
            val context =
                ClientCardContext(
                    command = ClientCardCommand.READ,
                    workMode = WorkMode.TEST,
                    principal = userA,
                    clientCardRequest = ClientCard(id = clientCardB.id),
                )

            processor.exec(context)

            assertEquals(State.FAILING, context.state)
            assertEquals("access-denied", context.errors.single().code)
        }

    @Test
    fun trainingPlanUpdateKeepsOriginalClientCardAndAuditFields() =
        runTest {
            val context =
                TrainingPlanContext(
                    command = TrainingPlanCommand.UPDATE,
                    workMode = WorkMode.TEST,
                    principal = userA,
                    trainingPlanRequest =
                        trainingPlanA.copy(
                            clientCardId = clientCardB.id,
                            ownerUserId = userB.userId,
                            createdByUserId = userB.userId,
                            title = "Updated plan",
                        ),
                )

            processor.exec(context)

            assertEquals(State.FINISHING, context.state)
            assertEquals(clientCardA.id, context.trainingPlanResponse.clientCardId)
            assertEquals(userA.userId, context.trainingPlanResponse.ownerUserId)
            assertEquals(userA.userId, context.trainingPlanResponse.createdByUserId)
        }

    private fun clientCard(
        id: String,
        userId: String,
    ) = ClientCard(
        id = ClientCardId(id),
        ownerUserId = userId,
        createdByUserId = userId,
        displayName = "Client $id",
        lock = ClientCardLock("lock-$id"),
    )

    private fun trainingPlan(
        id: String,
        clientCardId: ClientCardId,
        userId: String,
    ) = TrainingPlan(
        id = TrainingPlanId(id),
        clientCardId = clientCardId,
        ownerUserId = userId,
        createdByUserId = userId,
        title = "Plan $id",
        lock = TrainingPlanLock("lock-$id"),
        planItems =
            listOf(
                ExerciseItem(
                    id = "00000000-0000-0000-0000-000000000301",
                    title = "Exercise",
                ),
            ),
    )
}
