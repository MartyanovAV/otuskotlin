package com.github.martyanovav.otuskotlin.fitbridge.training.common.repo

interface IRepoTrainingPlan {
    suspend fun createTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse

    suspend fun readTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse

    suspend fun updateTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse

    suspend fun archiveTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse

    suspend fun searchTrainingPlans(rq: DbTrainingPlanFilterRequest): IDbTrainingPlansResponse

    companion object {
        val NONE =
            object : IRepoTrainingPlan {
                override suspend fun createTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
                    throw NotImplementedError("Must not be used")
                }

                override suspend fun readTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse {
                    throw NotImplementedError("Must not be used")
                }

                override suspend fun updateTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
                    throw NotImplementedError("Must not be used")
                }

                override suspend fun archiveTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse {
                    throw NotImplementedError("Must not be used")
                }

                override suspend fun searchTrainingPlans(rq: DbTrainingPlanFilterRequest): IDbTrainingPlansResponse {
                    throw NotImplementedError("Must not be used")
                }
            }
    }
}
