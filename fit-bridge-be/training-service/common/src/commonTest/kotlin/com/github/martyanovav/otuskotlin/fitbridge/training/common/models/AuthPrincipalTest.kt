package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthPrincipalTest {
    @Test
    fun trainerRoleIsRecognized() {
        val principal = AuthPrincipal(roles = setOf(AuthPrincipal.TRAINER_ROLE))

        assertTrue(principal.isTrainer())
    }

    @Test
    fun missingTrainerRoleIsRejected() {
        assertFalse(AuthPrincipal.NONE.isTrainer())
    }
}
