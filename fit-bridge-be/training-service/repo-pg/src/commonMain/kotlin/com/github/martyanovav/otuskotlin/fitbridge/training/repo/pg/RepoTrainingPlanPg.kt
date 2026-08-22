package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkoutDifficulty
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlansResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlanResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlansResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.RepoTrainingPlanBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorEmptyTrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorEmptyTrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorInvalidTrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorNotFoundTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorRepoConcurrencyTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorRepoDbTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.IRepoTrainingPlanInitializable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

class RepoTrainingPlanPg(
    private val properties: PgProperties = PgProperties(),
    private val db: Database? = null,
    private val randomUuid: () -> String = { UUID.randomUUID().toString() },
) : RepoTrainingPlanBase(), IRepoTrainingPlanInitializable {
    private var dsRef: HikariDataSource? = null

    private val database: Database by lazy {
        if (db != null) {
            db
        } else {
            val dataSource = buildDataSource()
            dsRef = dataSource
            Database.connect(dataSource)
        }
    }

    private fun buildDataSource(): HikariDataSource {
        val config =
            HikariConfig().apply {
                jdbcUrl = properties.url
                username = properties.user
                password = properties.password
                maximumPoolSize = properties.maxConnections
                isAutoCommit = true
            }
        return HikariDataSource(config)
    }

    override suspend fun createTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse =
        tryMethod {
            val plan =
                rq.trainingPlan.copy(
                    id = TrainingPlanId(randomUuid()),
                    lock = TrainingPlanLock(randomUuid()),
                )
            val now = Instant.now()
            val status = statusToString(plan.status)
            val result =
                transaction(database) {
                    TrainingPlanTable.insertReturning {
                        it[id] = plan.id.asString()
                        it[clientCardId] = plan.clientCardId.asString()
                        it[ownerUserId] = plan.ownerUserId
                        it[createdByUserId] = plan.createdByUserId
                        it[title] = plan.title
                        it[planItems] = plan.planItems
                        it[TrainingPlanTable.status] = status
                        it[version] = plan.version
                        it[lock] = plan.lock.asString()
                        it[createdAt] = now
                        it[updatedAt] = now
                        it[completedAt] = if (plan.completedAt.isNotBlank()) Instant.parse(plan.completedAt) else null
                        it[difficulty] = if (plan.difficulty != WorkoutDifficulty.NONE) plan.difficulty.name else null
                        it[coachComment] = plan.coachComment
                    }.single().toTrainingPlan()
                }
            DbTrainingPlanResponseOk(result)
        }

    override suspend fun readTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse =
        tryMethod {
            val result =
                transaction(database) {
                    TrainingPlanTable
                        .selectAll()
                        .where { TrainingPlanTable.id eq rq.id.asString() }
                        .singleOrNull()
                        ?.toTrainingPlan()
                }
            if (result != null) {
                DbTrainingPlanResponseOk(result)
            } else {
                errorNotFoundTrainingPlan(rq.id)
            }
        }

    override suspend fun updateTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse =
        tryMethod {
            val newPlan = rq.trainingPlan.copy(lock = TrainingPlanLock(randomUuid()))
            val now = Instant.now()
            val status = statusToString(newPlan.status)
            val result =
                transaction(database) {
                    val affected =
                        TrainingPlanTable.update(
                            where = {
                                (TrainingPlanTable.id eq newPlan.id.asString()) and
                                    (TrainingPlanTable.lock eq rq.trainingPlan.lock.asString())
                            },
                        ) {
                            it[title] = newPlan.title
                            it[planItems] = newPlan.planItems
                            it[TrainingPlanTable.status] = status
                            it[version] = newPlan.version
                            it[lock] = newPlan.lock.asString()
                            it[updatedAt] = now
                        }
                    if (affected > 0) {
                        val returned =
                            TrainingPlanTable
                                .selectAll()
                                .where { TrainingPlanTable.id eq newPlan.id.asString() }
                                .single()
                                .toTrainingPlan()
                        DbTrainingPlanResponseOk(returned)
                    } else {
                        val current =
                            TrainingPlanTable
                                .selectAll()
                                .where { TrainingPlanTable.id eq newPlan.id.asString() }
                                .singleOrNull()
                                ?.toTrainingPlan()
                        if (current != null) {
                            errorRepoConcurrencyTrainingPlan(current, rq.trainingPlan.lock)
                        } else {
                            errorNotFoundTrainingPlan(newPlan.id)
                        }
                    }
                }
            result
        }

    override suspend fun archiveTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse =
        tryMethod {
            val id = rq.id.takeIf { it != TrainingPlanId.NONE } ?: return@tryMethod errorEmptyTrainingPlanId
            val oldLock = rq.lock.takeIf { it != TrainingPlanLock.NONE } ?: return@tryMethod errorEmptyTrainingPlanLock(id)
            val newLock = TrainingPlanLock(randomUuid())
            val now = Instant.now()
            val result =
                transaction(database) {
                    val affected =
                        TrainingPlanTable.update(
                            where = {
                                (TrainingPlanTable.id eq id.asString()) and
                                    (TrainingPlanTable.lock eq oldLock.asString())
                            },
                        ) {
                            it[status] = STATUS_ARCHIVED
                            it[lock] = newLock.asString()
                            it[archivedAt] = now
                            it[updatedAt] = now
                        }
                    if (affected > 0) {
                        val returned =
                            TrainingPlanTable
                                .selectAll()
                                .where { TrainingPlanTable.id eq id.asString() }
                                .single()
                                .toTrainingPlan()
                        if (returned.lock == newLock) {
                            DbTrainingPlanResponseOk(returned)
                        } else {
                            errorRepoConcurrencyTrainingPlan(returned, oldLock)
                        }
                    } else {
                        val current =
                            TrainingPlanTable
                                .selectAll()
                                .where { TrainingPlanTable.id eq id.asString() }
                                .singleOrNull()
                                ?.toTrainingPlan()
                        if (current != null) {
                            errorRepoConcurrencyTrainingPlan(current, oldLock)
                        } else {
                            errorNotFoundTrainingPlan(id)
                        }
                    }
                }
            result
        }

    override suspend fun completeTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse =
        tryMethod {
            val plan = rq.trainingPlan
            val id = plan.id.takeIf { it != TrainingPlanId.NONE } ?: return@tryMethod errorEmptyTrainingPlanId
            val oldLock = plan.lock.takeIf { it != TrainingPlanLock.NONE } ?: return@tryMethod errorEmptyTrainingPlanLock(id)
            val newLock = TrainingPlanLock(randomUuid())
            val now = Instant.now()
            val completedTime = if (plan.completedAt.isNotBlank()) Instant.parse(plan.completedAt) else now
            val result =
                transaction(database) {
                    val affected =
                        TrainingPlanTable.update(
                            where = {
                                (TrainingPlanTable.id eq id.asString()) and
                                    (TrainingPlanTable.lock eq oldLock.asString()) and
                                    (TrainingPlanTable.status eq STATUS_ACTIVE)
                            },
                        ) {
                            it[status] = "COMPLETED"
                            it[lock] = newLock.asString()
                            it[completedAt] = completedTime
                            it[difficulty] = if (plan.difficulty != WorkoutDifficulty.NONE) plan.difficulty.name else null
                            it[coachComment] = plan.coachComment
                            it[updatedAt] = now
                        }
                    if (affected > 0) {
                        val returned =
                            TrainingPlanTable
                                .selectAll()
                                .where { TrainingPlanTable.id eq id.asString() }
                                .single()
                                .toTrainingPlan()
                        if (returned.lock == newLock) {
                            DbTrainingPlanResponseOk(returned)
                        } else {
                            errorRepoConcurrencyTrainingPlan(returned, oldLock)
                        }
                    } else {
                        val current =
                            TrainingPlanTable
                                .selectAll()
                                .where { TrainingPlanTable.id eq id.asString() }
                                .singleOrNull()
                                ?.toTrainingPlan()
                        if (current != null) {
                            when {
                                current.lock != oldLock -> errorRepoConcurrencyTrainingPlan(current, oldLock)
                                current.status != TrainingPlanStatus.ACTIVE -> errorInvalidTrainingPlanStatus(current)
                                else -> errorRepoConcurrencyTrainingPlan(current, oldLock)
                            }
                        } else {
                            errorNotFoundTrainingPlan(id)
                        }
                    }
                }
            result
        }

    override suspend fun searchTrainingPlans(rq: DbTrainingPlanFilterRequest): IDbTrainingPlansResponse =
        tryListMethod {
            val hasClientCardId = rq.clientCardId != ClientCardId.NONE
            val hasTitle = rq.searchString.isNotBlank()
            val hasStatus = rq.status != TrainingPlanStatus.NONE
            val hasOwnerUserId = rq.ownerUserId.isNotBlank()
            val (result, totalSize) =
                transaction(database) {
                    val conditions = mutableListOf<Op<Boolean>>()
                    if (hasClientCardId) {
                        conditions +=
                            TrainingPlanTable.clientCardId eq rq.clientCardId.asString()
                    }
                    if (hasStatus) {
                        conditions +=
                            TrainingPlanTable.status eq statusToString(rq.status)
                    }
                    if (hasTitle) {
                        conditions +=
                            TrainingPlanTable.title like "%${rq.searchString}%"
                    }
                    if (hasOwnerUserId) {
                        conditions += TrainingPlanTable.ownerUserId eq rq.ownerUserId
                    }
                    val query =
                        if (conditions.isNotEmpty()) {
                            TrainingPlanTable.selectAll().where { conditions.reduce { acc, op -> acc and op } }
                        } else {
                            TrainingPlanTable.selectAll()
                        }
                    val totalSize = query.count().toInt()
                    val offset = (rq.pageNumber - 1).coerceAtLeast(0).toLong() * rq.pageSize
                    val items =
                        query
                            .orderBy(TrainingPlanTable.createdAt to SortOrder.DESC, TrainingPlanTable.id to SortOrder.ASC)
                            .limit(rq.pageSize)
                            .offset(offset)
                            .map { it.toTrainingPlan() }
                    items to totalSize
                }
            DbTrainingPlansResponseOk(
                Page(
                    items = result,
                    totalSize = totalSize,
                    pageNumber = rq.pageNumber,
                    pageSize = rq.pageSize,
                ),
            )
        }

    override fun save(plans: Collection<TrainingPlan>): Collection<TrainingPlan> {
        val now = Instant.now()
        return transaction(database) {
            plans.map { plan ->
                val savedPlan =
                    plan.copy(
                        id = if (plan.id == TrainingPlanId.NONE) TrainingPlanId(randomUuid()) else plan.id,
                        lock = if (plan.lock == TrainingPlanLock.NONE) TrainingPlanLock(randomUuid()) else plan.lock,
                    )
                TrainingPlanTable.insertReturning {
                    it[id] = savedPlan.id.asString()
                    it[clientCardId] = savedPlan.clientCardId.asString()
                    it[ownerUserId] = savedPlan.ownerUserId
                    it[createdByUserId] = savedPlan.createdByUserId
                    it[title] = savedPlan.title
                    it[planItems] = savedPlan.planItems
                    it[TrainingPlanTable.status] = statusToString(savedPlan.status)
                    it[version] = savedPlan.version
                    it[lock] = savedPlan.lock.asString()
                    it[createdAt] = now
                    it[updatedAt] = now
                    it[completedAt] = if (savedPlan.completedAt.isNotBlank()) Instant.parse(savedPlan.completedAt) else null
                    it[difficulty] = if (savedPlan.difficulty != WorkoutDifficulty.NONE) savedPlan.difficulty.name else null
                    it[coachComment] = savedPlan.coachComment
                }.single().toTrainingPlan()
            }
        }
    }

    fun clear() {
        transaction(database) {
            TrainingPlanTable.deleteAll()
        }
    }

    fun close() {
        dsRef?.close()
    }

    private inline fun tryMethod(block: () -> IDbTrainingPlanResponse): IDbTrainingPlanResponse =
        try {
            block()
        } catch (e: Throwable) {
            errorRepoDbTrainingPlan(e)
        }

    private inline fun tryListMethod(block: () -> IDbTrainingPlansResponse): IDbTrainingPlansResponse =
        try {
            block()
        } catch (e: Throwable) {
            DbTrainingPlansResponseErr()
        }

    companion object {
        private const val STATUS_ACTIVE = "ACTIVE"
        private const val STATUS_ARCHIVED = "ARCHIVED"

        private fun statusToString(status: TrainingPlanStatus): String =
            when (status) {
                TrainingPlanStatus.ARCHIVED -> STATUS_ARCHIVED
                TrainingPlanStatus.COMPLETED -> "COMPLETED"
                else -> STATUS_ACTIVE
            }
    }
}

private fun ResultRow.toTrainingPlan(): TrainingPlan =
    TrainingPlan(
        id = TrainingPlanId(this[TrainingPlanTable.id]),
        clientCardId = ClientCardId(this[TrainingPlanTable.clientCardId]),
        ownerUserId = this[TrainingPlanTable.ownerUserId],
        createdByUserId = this[TrainingPlanTable.createdByUserId],
        title = this[TrainingPlanTable.title],
        status =
            when (this[TrainingPlanTable.status]) {
                "ARCHIVED" -> TrainingPlanStatus.ARCHIVED
                "COMPLETED" -> TrainingPlanStatus.COMPLETED
                else -> TrainingPlanStatus.ACTIVE
            },
        lock = TrainingPlanLock(this[TrainingPlanTable.lock]),
        planItems = this[TrainingPlanTable.planItems],
        version = this[TrainingPlanTable.version],
        createdAt = this[TrainingPlanTable.createdAt].toString(),
        updatedAt = this[TrainingPlanTable.updatedAt].toString(),
        completedAt = this[TrainingPlanTable.completedAt]?.toString() ?: "",
        difficulty = this[TrainingPlanTable.difficulty]?.let { WorkoutDifficulty.valueOf(it) } ?: WorkoutDifficulty.NONE,
        coachComment = this[TrainingPlanTable.coachComment],
    )
