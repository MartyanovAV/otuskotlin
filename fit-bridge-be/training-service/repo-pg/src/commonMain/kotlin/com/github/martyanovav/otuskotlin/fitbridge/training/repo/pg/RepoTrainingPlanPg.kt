package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
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
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorNotFoundTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorRepoConcurrencyTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorRepoDbTrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.IRepoTrainingPlanInitializable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RepoTrainingPlanPg(
    private val properties: PgProperties = PgProperties(),
    private val randomUuid: () -> String = { UUID.randomUUID().toString() },
) : RepoTrainingPlanBase(), IRepoTrainingPlanInitializable {
    private var _ds: HikariDataSource? = null
    private val ds: HikariDataSource
        get() {
            if (_ds == null) {
                _ds =
                    HikariConfig().apply {
                        jdbcUrl = properties.url
                        username = properties.user
                        password = properties.password
                        maximumPoolSize = properties.maxConnections
                        isAutoCommit = true
                    }.let { HikariDataSource(it) }
            }
            return _ds!!
        }

    private val dbName = "\"${properties.schema}\".\"training_plan\""
    private val cols = PgTrainingPlanFields.allFields.joinToString { it.quoted() }

    override suspend fun createTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse =
        tryMethod {
            val plan =
                rq.trainingPlan.copy(
                    id = TrainingPlanId(randomUuid()),
                    lock = TrainingPlanLock(randomUuid()),
                )
            val now = nowTimestamp()
            val sql = PgQueryBuilder.insertTrainingPlan(dbName, cols).replaceNamedParams()
            val status =
                when (plan.status) {
                    TrainingPlanStatus.ARCHIVED -> PgTrainingPlanFields.STATUS_ARCHIVED
                    else -> PgTrainingPlanFields.STATUS_ACTIVE
                }
            val planItemsJson = TrainingPlanRowMapper.serializePlanItems(plan.planItems)
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    ps.setString(i++, plan.id.asString())
                    ps.setString(i++, plan.clientCardId.asString())
                    ps.setString(i++, plan.ownerId)
                    ps.setString(i++, plan.title)
                    ps.setString(i++, planItemsJson)
                    ps.setString(i++, status)
                    ps.setInt(i++, plan.version)
                    ps.setString(i++, plan.lock.asString())
                    ps.setString(i++, now)
                    ps.setString(i++, now)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            DbTrainingPlanResponseOk(TrainingPlanRowMapper.map(rs))
                        } else {
                            throw RuntimeException("DB error: insert returned no rows")
                        }
                    }
                }
            }
        }

    override suspend fun readTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse =
        tryMethod {
            val sql = PgQueryBuilder.readTrainingPlan(dbName, cols).replaceNamedParams()
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.setString(1, rq.id.asString())
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            DbTrainingPlanResponseOk(TrainingPlanRowMapper.map(rs))
                        } else {
                            errorNotFoundTrainingPlan(rq.id)
                        }
                    }
                }
            }
        }

    override suspend fun updateTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse =
        tryMethod {
            val newPlan = rq.trainingPlan.copy(lock = TrainingPlanLock(randomUuid()))
            val now = nowTimestamp()
            val status =
                when (newPlan.status) {
                    TrainingPlanStatus.ARCHIVED -> PgTrainingPlanFields.STATUS_ARCHIVED
                    else -> PgTrainingPlanFields.STATUS_ACTIVE
                }
            val planItemsJson = TrainingPlanRowMapper.serializePlanItems(newPlan.planItems)
            val sql = PgQueryBuilder.updateTrainingPlan(dbName, cols).replaceNamedParams()
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    ps.setString(i++, newPlan.title)
                    ps.setString(i++, planItemsJson)
                    ps.setString(i++, status)
                    ps.setInt(i++, newPlan.version)
                    ps.setString(i++, newPlan.lock.asString())
                    ps.setString(i++, now)
                    ps.setString(i++, newPlan.id.asString())
                    ps.setString(i++, rq.trainingPlan.lock.asString())
                    ps.setString(i++, newPlan.id.asString())
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val returned = TrainingPlanRowMapper.map(rs)
                            if (returned.lock == newPlan.lock) {
                                DbTrainingPlanResponseOk(returned)
                            } else {
                                errorRepoConcurrencyTrainingPlan(returned, rq.trainingPlan.lock)
                            }
                        } else {
                            errorNotFoundTrainingPlan(newPlan.id)
                        }
                    }
                }
            }
        }

    override suspend fun archiveTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse =
        tryMethod {
            val id = rq.id.takeIf { it != TrainingPlanId.NONE } ?: return@tryMethod errorEmptyTrainingPlanId
            val oldLock = rq.lock.takeIf { it != TrainingPlanLock.NONE } ?: return@tryMethod errorEmptyTrainingPlanLock(id)
            val now = nowTimestamp()
            val sql = PgQueryBuilder.archiveTrainingPlan(dbName, cols).replaceNamedParams()
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    ps.setString(i++, now)
                    ps.setString(i++, now)
                    ps.setString(i++, id.asString())
                    ps.setString(i++, oldLock.asString())
                    ps.setString(i++, id.asString())
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val returned = TrainingPlanRowMapper.map(rs)
                            if (returned.lock == oldLock) {
                                DbTrainingPlanResponseOk(returned)
                            } else {
                                errorRepoConcurrencyTrainingPlan(returned, oldLock)
                            }
                        } else {
                            errorNotFoundTrainingPlan(id)
                        }
                    }
                }
            }
        }

    override suspend fun searchTrainingPlans(rq: DbTrainingPlanFilterRequest): IDbTrainingPlansResponse =
        tryListMethod {
            val hasClientCardId = rq.clientCardId != ClientCardId.NONE
            val hasTitle = rq.searchString.isNotBlank()
            val hasStatus = rq.status != TrainingPlanStatus.NONE
            val sql =
                PgQueryBuilder.searchTrainingPlans(
                    dbName, cols,
                    ownerId = false,
                    clientCardId = hasClientCardId,
                    status = hasStatus,
                    title = hasTitle,
                ).replaceNamedParams()
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    if (hasClientCardId) {
                        ps.setString(i++, rq.clientCardId.asString())
                    }
                    if (hasStatus) {
                        val statusStr =
                            when (rq.status) {
                                TrainingPlanStatus.ARCHIVED -> PgTrainingPlanFields.STATUS_ARCHIVED
                                else -> PgTrainingPlanFields.STATUS_ACTIVE
                            }
                        ps.setString(i++, statusStr)
                    }
                    if (hasTitle) {
                        ps.setString(i++, "%${rq.searchString}%")
                    }
                    ps.executeQuery().use { rs ->
                        val result = mutableListOf<TrainingPlan>()
                        while (rs.next()) {
                            result += TrainingPlanRowMapper.map(rs)
                        }
                        DbTrainingPlansResponseOk(result)
                    }
                }
            }
        }

    override fun save(plans: Collection<TrainingPlan>): Collection<TrainingPlan> {
        val now = nowTimestamp()
        val sql = PgQueryBuilder.insertTrainingPlan(dbName, cols).replaceNamedParams()
        ds.connection.use { conn ->
            return plans.map { plan ->
                val savedPlan =
                    plan.copy(
                        id = if (plan.id == TrainingPlanId.NONE) TrainingPlanId(randomUuid()) else plan.id,
                        lock = if (plan.lock == TrainingPlanLock.NONE) TrainingPlanLock(randomUuid()) else plan.lock,
                    )
                val status =
                    when (savedPlan.status) {
                        TrainingPlanStatus.ARCHIVED -> PgTrainingPlanFields.STATUS_ARCHIVED
                        else -> PgTrainingPlanFields.STATUS_ACTIVE
                    }
                val planItemsJson = TrainingPlanRowMapper.serializePlanItems(savedPlan.planItems)
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    ps.setString(i++, savedPlan.id.asString())
                    ps.setString(i++, savedPlan.clientCardId.asString())
                    ps.setString(i++, savedPlan.ownerId)
                    ps.setString(i++, savedPlan.title)
                    ps.setString(i++, planItemsJson)
                    ps.setString(i++, status)
                    ps.setInt(i++, savedPlan.version)
                    ps.setString(i++, savedPlan.lock.asString())
                    ps.setString(i++, now)
                    ps.setString(i++, now)
                    ps.executeQuery().use { rs ->
                        rs.next()
                        TrainingPlanRowMapper.map(rs)
                    }
                }
            }
        }
    }

    fun clear() {
        ds.connection.use { conn ->
            conn.createStatement().use { st ->
                st.execute(PgQueryBuilder.clear(dbName))
            }
        }
    }

    fun close() {
        _ds?.close()
    }

    private fun nowTimestamp(): String = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

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
}
