package com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory

import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.RepoClientCardInitialized
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardCreateTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardDeleteTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardReadTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardSearchTest
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests.RepoClientCardUpdateTest

class RepoClientCardInMemoryCreateTest : RepoClientCardCreateTest() {
    override val repo =
        RepoClientCardInitialized(
            RepoClientCardInMemory(randomUuid = { uuidNew.asString() }),
            initObjects = initObjects,
        )
}

class RepoClientCardInMemoryDeleteTest : RepoClientCardDeleteTest() {
    override val repo =
        RepoClientCardInitialized(
            RepoClientCardInMemory(),
            initObjects = initObjects,
        )
}

class RepoClientCardInMemoryReadTest : RepoClientCardReadTest() {
    override val repo =
        RepoClientCardInitialized(
            RepoClientCardInMemory(),
            initObjects = initObjects,
        )
}

class RepoClientCardInMemorySearchTest : RepoClientCardSearchTest() {
    override val repo =
        RepoClientCardInitialized(
            RepoClientCardInMemory(),
            initObjects = initObjects,
        )
}

class RepoClientCardInMemoryUpdateTest : RepoClientCardUpdateTest() {
    override val repo =
        RepoClientCardInitialized(
            RepoClientCardInMemory(),
            initObjects = initObjects,
        )
}
