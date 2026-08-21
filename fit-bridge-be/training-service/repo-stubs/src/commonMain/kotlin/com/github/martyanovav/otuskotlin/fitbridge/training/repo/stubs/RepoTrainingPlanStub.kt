package com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs

import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlanResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlansResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.stubs.TrainingPlanStub

class RepoTrainingPlanStub : IRepoTrainingPlan {
    override suspend fun createTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
        return DbTrainingPlanResponseOk(data = TrainingPlanStub.get())
    }

    override suspend fun readTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse {
        return DbTrainingPlanResponseOk(data = TrainingPlanStub.get())
    }

    override suspend fun updateTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
        return DbTrainingPlanResponseOk(data = TrainingPlanStub.get())
    }

    override suspend fun archiveTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse {
        return DbTrainingPlanResponseOk(data = TrainingPlanStub.get())
    }

    override suspend fun searchTrainingPlans(rq: DbTrainingPlanFilterRequest): IDbTrainingPlansResponse {
        return DbTrainingPlansResponseOk(data = TrainingPlanStub.getList())
    }
}
