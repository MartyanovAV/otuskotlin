package com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory

import com.benasher44.uuid.uuid4
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlanResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlansResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.RepoTrainingPlanBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorEmptyTrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorNotFoundTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorRepoConcurrencyTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.IRepoTrainingPlanInitializable
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RepoTrainingPlanInMemory(
    ttl: Duration = 2.minutes,
    val randomUuid: () -> String = { uuid4().toString() },
) : RepoTrainingPlanBase(), IRepoTrainingPlan, IRepoTrainingPlanInitializable {
    private val mutex: Mutex = Mutex()
    private val cache =
        Cache.Builder<String, TrainingPlanEntity>()
            .expireAfterWrite(ttl)
            .build()

    override fun save(plans: Collection<TrainingPlan>) =
        plans.map { plan ->
            val entity = TrainingPlanEntity(plan)
            require(entity.id != null)
            cache.put(entity.id, entity)
            plan
        }

    override suspend fun createTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse =
        tryTrainingPlanMethod {
            val key = randomUuid()
            val plan = rq.trainingPlan.copy(id = TrainingPlanId(key))
            val entity = TrainingPlanEntity(plan)
            mutex.withLock {
                cache.put(key, entity)
            }
            DbTrainingPlanResponseOk(plan)
        }

    override suspend fun readTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse =
        tryTrainingPlanMethod {
            val key =
                rq.id.takeIf { it != TrainingPlanId.NONE }?.asString()
                    ?: return@tryTrainingPlanMethod errorEmptyTrainingPlanId
            mutex.withLock {
                cache.get(key)
                    ?.let { DbTrainingPlanResponseOk(it.toInternal()) }
                    ?: errorNotFoundTrainingPlan(rq.id)
            }
        }

    override suspend fun updateTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse =
        tryTrainingPlanMethod {
            val rqPlan = rq.trainingPlan
            val id = rqPlan.id.takeIf { it != TrainingPlanId.NONE } ?: return@tryTrainingPlanMethod errorEmptyTrainingPlanId
            val key = id.asString()

            mutex.withLock {
                val oldEntity = cache.get(key)
                val oldPlan = oldEntity?.toInternal()
                when {
                    oldPlan == null -> errorNotFoundTrainingPlan(id)
                    oldPlan.lock != TrainingPlanLock.NONE && oldPlan.lock != rqPlan.lock ->
                        errorRepoConcurrencyTrainingPlan(oldPlan, rqPlan.lock)
                    else -> {
                        val newPlan = rqPlan.copy(lock = TrainingPlanLock(randomUuid()))
                        val entity = TrainingPlanEntity(newPlan)
                        cache.put(key, entity)
                        DbTrainingPlanResponseOk(newPlan)
                    }
                }
            }
        }

    override suspend fun archiveTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse =
        tryTrainingPlanMethod {
            val id = rq.id.takeIf { it != TrainingPlanId.NONE } ?: return@tryTrainingPlanMethod errorEmptyTrainingPlanId
            val key = id.asString()

            mutex.withLock {
                val oldEntity = cache.get(key)
                val oldPlan = oldEntity?.toInternal()
                when {
                    oldPlan == null -> errorNotFoundTrainingPlan(id)
                    oldPlan.lock != TrainingPlanLock.NONE && oldPlan.lock != rq.lock ->
                        errorRepoConcurrencyTrainingPlan(oldPlan, rq.lock)
                    else -> {
                        val archivedPlan = oldPlan.copy(status = TrainingPlanStatus.ARCHIVED, lock = TrainingPlanLock(randomUuid()))
                        val entity = TrainingPlanEntity(archivedPlan)
                        cache.put(key, entity)
                        DbTrainingPlanResponseOk(archivedPlan)
                    }
                }
            }
        }

    override suspend fun searchTrainingPlans(rq: DbTrainingPlanFilterRequest): IDbTrainingPlansResponse =
        tryTrainingPlansMethod {
            val result: List<TrainingPlan> =
                cache.asMap().asSequence()
                    .filter { entry ->
                        rq.clientCardId.takeIf { it != ClientCardId.NONE }?.let {
                            it.asString() == entry.value.clientCardId
                        } ?: true
                    }
                    .filter { entry ->
                        rq.status.takeIf { it != TrainingPlanStatus.NONE }?.let {
                            it.name == entry.value.status
                        } ?: true
                    }
                    .filter { entry ->
                        rq.searchString.takeIf { it.isNotBlank() }?.let {
                            entry.value.title?.contains(it) ?: false
                        } ?: true
                    }
                    .map { it.value.toInternal() }
                    .toList()
            DbTrainingPlansResponseOk(result)
        }
}
