package com.github.martyanovav.otuskotlin.fitbridge.cor

/**
 * A named block that processes a context.
 */
interface ICorExec<T> {
    val title: String
    val description: String

    suspend fun exec(context: T)
}
