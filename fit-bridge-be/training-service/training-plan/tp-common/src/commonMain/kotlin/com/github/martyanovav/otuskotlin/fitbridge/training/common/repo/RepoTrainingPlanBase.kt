package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.errorSystem
import kotlinx.coroutines.CancellationException

abstract class RepoTrainingPlanBase : IRepoTrainingPlan {
    protected suspend fun tryTrainingPlanMethod(block: suspend () -> IDbTrainingPlanResponse) =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            DbTrainingPlanResponseErr(errorSystem("methodException", e = e))
        }

    protected suspend fun tryTrainingPlansMethod(block: suspend () -> IDbTrainingPlansResponse) =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            DbTrainingPlansResponseErr(errorSystem("methodException", e = e))
        }
}
