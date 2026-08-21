package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.BaseInitClientCards
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.BaseInitTrainingPlans
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardCreateTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardDeleteTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardReadTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardSearchTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardUpdateTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanCreateTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanDeleteTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanReadTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanSearchTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanUpdateTest

private val parentClientCard =
    ClientCard(
        id = ClientCardId("cc-123"),
        ownerUserId = "owner-123",
        createdByUserId = "owner-123",
        displayName = "parent card for TP tests",
        lock = ClientCardLock("lock-parent"),
    )

private val searchParentClientCard =
    ClientCard(
        id = ClientCardId("cc-search-owner"),
        ownerUserId = "owner-search",
        createdByUserId = "owner-search",
        displayName = "search parent card",
        lock = ClientCardLock("lock-search-parent"),
    )

class RepoClientCardPgCreateTest : RepoClientCardCreateTest() {
    override val repo = testClientCardRepo(initObjects, randomUuid = { uuidNew.asString() }).repo

    companion object : BaseInitClientCards("create") {
        override val initObjects: List<ClientCard> = emptyList()
    }
}

class RepoClientCardPgReadTest : RepoClientCardReadTest() {
    override val repo = testClientCardRepo(initObjects).repo

    companion object : BaseInitClientCards("read") {
        override val initObjects: List<ClientCard> = listOf(createInitTestModel("read"))
    }
}

class RepoClientCardPgUpdateTest : RepoClientCardUpdateTest() {
    override val repo = testClientCardRepo(initObjects).repo

    companion object : BaseInitClientCards("update") {
        override val initObjects: List<ClientCard> = listOf(createInitTestModel("update"))
    }
}

class RepoClientCardPgDeleteTest : RepoClientCardDeleteTest() {
    override val repo = testClientCardRepo(initObjects).repo

    companion object : BaseInitClientCards("delete") {
        override val initObjects: List<ClientCard> = listOf(createInitTestModel("delete"))
    }
}

class RepoClientCardPgSearchTest : RepoClientCardSearchTest() {
    override val repo = testClientCardRepo(initObjects).repo

    companion object : BaseInitClientCards("search") {
        override val initObjects: List<ClientCard> =
            listOf(
                createInitTestModel("ad1"),
                createInitTestModel("ad2", ownerUserId = "owner-124"),
                createInitTestModel("ad3"),
                createInitTestModel("ad4", ownerUserId = "owner-124"),
                createInitTestModel("ad5"),
            )
    }
}

class RepoTrainingPlanPgCreateTest : RepoTrainingPlanCreateTest() {
    override val repo =
        testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard), randomUuid = {
            uuidNew.asString()
        }).repo

    companion object : BaseInitTrainingPlans("create") {
        override val initObjects: List<TrainingPlan> = emptyList()
    }
}

class RepoTrainingPlanPgReadTest : RepoTrainingPlanReadTest() {
    override val repo = testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard)).repo

    companion object : BaseInitTrainingPlans("read") {
        override val initObjects: List<TrainingPlan> = listOf(createInitTestModel("read"))
    }
}

class RepoTrainingPlanPgUpdateTest : RepoTrainingPlanUpdateTest() {
    override val repo = testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard)).repo

    companion object : BaseInitTrainingPlans("update") {
        override val initObjects: List<TrainingPlan> = listOf(createInitTestModel("update"))
    }
}

class RepoTrainingPlanPgDeleteTest : RepoTrainingPlanDeleteTest() {
    override val repo = testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard)).repo

    companion object : BaseInitTrainingPlans("delete") {
        override val initObjects: List<TrainingPlan> = listOf(createInitTestModel("delete"))
    }
}

class RepoTrainingPlanPgSearchTest : RepoTrainingPlanSearchTest() {
    override val repo = testTrainingPlanRepo(initObjects, parentClientCards = listOf(parentClientCard, searchParentClientCard)).repo

    companion object : BaseInitTrainingPlans("search") {
        val searchClientCardId = ClientCardId("cc-search-owner")
        override val initObjects: List<TrainingPlan> =
            listOf(
                createInitTestModel("ad1"),
                createInitTestModel("ad2", clientCardId = searchClientCardId, ownerUserId = "owner-124"),
                createInitTestModel("ad3"),
                createInitTestModel("ad4", clientCardId = searchClientCardId, ownerUserId = "owner-124"),
                createInitTestModel("ad5"),
            )
    }
}
