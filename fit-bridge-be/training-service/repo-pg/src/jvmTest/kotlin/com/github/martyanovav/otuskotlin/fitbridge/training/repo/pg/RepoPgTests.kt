package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.BaseInitClientCards
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.BaseInitTrainingPlans

private val parentClientCard =
    ClientCard(
        id = ClientCardId("cc-123"),
        ownerId = "owner-123",
        displayName = "parent card for TP tests",
        lock = ClientCardLock("lock-parent"),
    )

private val searchParentClientCard =
    ClientCard(
        id = ClientCardId("cc-search-owner"),
        ownerId = "owner-search",
        displayName = "search parent card",
        lock = ClientCardLock("lock-search-parent"),
    )

class RepoClientCardPgCreateTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardCreateTest() {
    override val repo = testClientCardRepo(initObjects, randomUuid = { uuidNew.asString() }).repo

    companion object : BaseInitClientCards("create") {
        override val initObjects: List<ClientCard> = emptyList()
    }
}

class RepoClientCardPgReadTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardReadTest() {
    override val repo = testClientCardRepo(initObjects).repo

    companion object : BaseInitClientCards("read") {
        override val initObjects: List<ClientCard> = listOf(createInitTestModel("read"))
    }
}

class RepoClientCardPgUpdateTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardUpdateTest() {
    override val repo = testClientCardRepo(initObjects).repo

    companion object : BaseInitClientCards("update") {
        override val initObjects: List<ClientCard> = listOf(createInitTestModel("update"))
    }
}

class RepoClientCardPgDeleteTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardDeleteTest() {
    override val repo = testClientCardRepo(initObjects).repo

    companion object : BaseInitClientCards("delete") {
        override val initObjects: List<ClientCard> = listOf(createInitTestModel("delete"))
    }
}

class RepoClientCardPgSearchTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardSearchTest() {
    override val repo = testClientCardRepo(initObjects).repo

    companion object : BaseInitClientCards("search") {
        override val initObjects: List<ClientCard> =
            listOf(
                createInitTestModel("ad1"),
                createInitTestModel("ad2", ownerId = "owner-124"),
                createInitTestModel("ad3"),
                createInitTestModel("ad4", ownerId = "owner-124"),
                createInitTestModel("ad5"),
            )
    }
}

class RepoTrainingPlanPgCreateTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanCreateTest() {
    override val repo =
        testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard), randomUuid = {
            uuidNew.asString()
        }).repo

    companion object : BaseInitTrainingPlans("create") {
        override val initObjects: List<TrainingPlan> = emptyList()
    }
}

class RepoTrainingPlanPgReadTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanReadTest() {
    override val repo = testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard)).repo

    companion object : BaseInitTrainingPlans("read") {
        override val initObjects: List<TrainingPlan> = listOf(createInitTestModel("read"))
    }
}

class RepoTrainingPlanPgUpdateTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanUpdateTest() {
    override val repo = testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard)).repo

    companion object : BaseInitTrainingPlans("update") {
        override val initObjects: List<TrainingPlan> = listOf(createInitTestModel("update"))
    }
}

class RepoTrainingPlanPgDeleteTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanDeleteTest() {
    override val repo = testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard)).repo

    companion object : BaseInitTrainingPlans("delete") {
        override val initObjects: List<TrainingPlan> = listOf(createInitTestModel("delete"))
    }
}

class RepoTrainingPlanPgSearchTest : com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanSearchTest() {
    override val repo = testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard, searchParentClientCard)).repo

    companion object : BaseInitTrainingPlans("search") {
        val searchClientCardId = com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId("cc-search-owner")
        override val initObjects: List<TrainingPlan> =
            listOf(
                createInitTestModel("ad1"),
                createInitTestModel("ad2", clientCardId = searchClientCardId),
                createInitTestModel("ad3"),
                createInitTestModel("ad4", clientCardId = searchClientCardId),
                createInitTestModel("ad5"),
            )
    }
}
