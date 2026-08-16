package com.github.martyanovav.otuskotlin.fitbridge.training.common.models

data class AuthPrincipal(
    val userId: String = "",
    val username: String = "",
    val roles: Set<String> = emptySet(),
) {
    fun isTrainer(): Boolean = TRAINER_ROLE in roles

    companion object {
        const val TRAINER_ROLE = "TRAINER"
        val NONE = AuthPrincipal()
    }
}
