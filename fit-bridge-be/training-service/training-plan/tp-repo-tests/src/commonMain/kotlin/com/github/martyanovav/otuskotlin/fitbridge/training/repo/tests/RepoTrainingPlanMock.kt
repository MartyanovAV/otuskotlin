package com.github.martyanovav.otuskotlin.fitbridge.training.repo.tests

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlanResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlansResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan

class RepoTrainingPlanMock(
    private val invokeCreateTrainingPlan: (DbTrainingPlanRequest) -> IDbTrainingPlanResponse = { DEFAULT_TP_SUCCESS_EMPTY_MOCK },
    private val invokeReadTrainingPlan: (DbTrainingPlanIdRequest) -> IDbTrainingPlanResponse = { DEFAULT_TP_SUCCESS_EMPTY_MOCK },
    private val invokeUpdateTrainingPlan: (DbTrainingPlanRequest) -> IDbTrainingPlanResponse = { DEFAULT_TP_SUCCESS_EMPTY_MOCK },
    private val invokeArchiveTrainingPlan: (DbTrainingPlanIdRequest) -> IDbTrainingPlanResponse = { DEFAULT_TP_SUCCESS_EMPTY_MOCK },
    private val invokeCompleteTrainingPlan: (DbTrainingPlanRequest) -> IDbTrainingPlanResponse = { DEFAULT_TP_SUCCESS_EMPTY_MOCK },
    private val invokeSearchTrainingPlans: (DbTrainingPlanFilterRequest) -> IDbTrainingPlansResponse = { DEFAULT_TPS_SUCCESS_EMPTY_MOCK },
) : IRepoTrainingPlan {
    override suspend fun createTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
        return invokeCreateTrainingPlan(rq)
    }

    override suspend fun readTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse {
        return invokeReadTrainingPlan(rq)
    }

    override suspend fun updateTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
        return invokeUpdateTrainingPlan(rq)
    }

    override suspend fun archiveTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse {
        return invokeArchiveTrainingPlan(rq)
    }

    override suspend fun completeTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
        return invokeCompleteTrainingPlan(rq)
    }

    override suspend fun searchTrainingPlans(rq: DbTrainingPlanFilterRequest): IDbTrainingPlansResponse {
        return invokeSearchTrainingPlans(rq)
    }

    companion object {
        val DEFAULT_TP_SUCCESS_EMPTY_MOCK = DbTrainingPlanResponseOk(TrainingPlan())
        val DEFAULT_TPS_SUCCESS_EMPTY_MOCK = DbTrainingPlansResponseOk(Page(emptyList()))
    }
}
