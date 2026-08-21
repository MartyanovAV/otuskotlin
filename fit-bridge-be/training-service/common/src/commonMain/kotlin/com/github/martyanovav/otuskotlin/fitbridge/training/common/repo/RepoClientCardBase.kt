package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.errorSystem
import kotlinx.coroutines.CancellationException

abstract class RepoClientCardBase : IRepoClientCard {
    protected suspend fun tryClientCardMethod(block: suspend () -> IDbClientCardResponse) =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            DbClientCardResponseErr(errorSystem("methodException", e = e))
        }

    protected suspend fun tryClientCardsMethod(block: suspend () -> IDbClientCardsResponse) =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            DbClientCardsResponseErr(errorSystem("methodException", e = e))
        }
}
