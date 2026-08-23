package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.minutes

fun runRepoTest(testBody: suspend TestScope.() -> Unit) =
    runTest(timeout = 2.minutes) {
        testBody()
    }
