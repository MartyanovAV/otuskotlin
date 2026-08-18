package com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory

import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.RepoTrainingPlanInitialized
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanCreateTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanDeleteTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanReadTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanSearchTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoTrainingPlanUpdateTest

class RepoTrainingPlanInMemoryCreateTest : RepoTrainingPlanCreateTest() {
    override val repo =
        RepoTrainingPlanInitialized(
            RepoTrainingPlanInMemory(randomUuid = { uuidNew.asString() }),
            initObjects = initObjects,
        )
}

class RepoTrainingPlanInMemoryDeleteTest : RepoTrainingPlanDeleteTest() {
    override val repo =
        RepoTrainingPlanInitialized(
            RepoTrainingPlanInMemory(),
            initObjects = initObjects,
        )
}

class RepoTrainingPlanInMemoryReadTest : RepoTrainingPlanReadTest() {
    override val repo =
        RepoTrainingPlanInitialized(
            RepoTrainingPlanInMemory(),
            initObjects = initObjects,
        )
}

class RepoTrainingPlanInMemorySearchTest : RepoTrainingPlanSearchTest() {
    override val repo =
        RepoTrainingPlanInitialized(
            RepoTrainingPlanInMemory(),
            initObjects = initObjects,
        )
}

class RepoTrainingPlanInMemoryUpdateTest : RepoTrainingPlanUpdateTest() {
    override val repo =
        RepoTrainingPlanInitialized(
            RepoTrainingPlanInMemory(),
            initObjects = initObjects,
        )
}
