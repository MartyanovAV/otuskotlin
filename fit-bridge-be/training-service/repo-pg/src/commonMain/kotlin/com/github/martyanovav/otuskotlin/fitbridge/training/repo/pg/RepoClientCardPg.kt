package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbClientCardResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbClientCardsResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.RepoClientCardBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorEmptyClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorEmptyClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorNotFoundClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorRepoConcurrencyClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorRepoDbClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.IRepoClientCardInitializable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RepoClientCardPg(
    private val properties: PgProperties = PgProperties(),
    private val randomUuid: () -> String = { UUID.randomUUID().toString() },
) : RepoClientCardBase(), IRepoClientCardInitializable {
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

    private val dbName = "\"${properties.schema}\".\"client_card\""
    private val cols = PgClientCardFields.allFields.joinToString { it.quoted() }

    override suspend fun createClientCard(rq: DbClientCardRequest): IDbClientCardResponse =
        tryMethod {
            val card =
                rq.clientCard.copy(
                    id = ClientCardId(randomUuid()),
                    lock = ClientCardLock(randomUuid()),
                )
            val now = nowTimestamp()
            val sql = PgQueryBuilder.insertClientCard(dbName, cols).replaceNamedParams()
            val status = if (card.isArchived) PgClientCardFields.STATUS_ARCHIVED else PgClientCardFields.STATUS_ACTIVE
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    ps.setString(i++, card.id.asString())
                    ps.setString(i++, card.ownerId)
                    ps.setString(i++, card.displayName)
                    ps.setString(i++, card.note)
                    ps.setString(i++, status)
                    ps.setString(i++, card.lock.asString())
                    ps.setString(i++, now)
                    ps.setString(i++, now)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            DbClientCardResponseOk(ClientCardRowMapper.map(rs))
                        } else {
                            throw RuntimeException("DB error: insert returned no rows")
                        }
                    }
                }
            }
        }

    override suspend fun readClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse =
        tryMethod {
            val sql = PgQueryBuilder.readClientCard(dbName, cols).replaceNamedParams()
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.setString(1, rq.id.asString())
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            DbClientCardResponseOk(ClientCardRowMapper.map(rs))
                        } else {
                            errorNotFoundClientCard(rq.id)
                        }
                    }
                }
            }
        }

    override suspend fun updateClientCard(rq: DbClientCardRequest): IDbClientCardResponse =
        tryMethod {
            val newCard = rq.clientCard.copy(lock = ClientCardLock(randomUuid()))
            val now = nowTimestamp()
            val status = if (newCard.isArchived) PgClientCardFields.STATUS_ARCHIVED else PgClientCardFields.STATUS_ACTIVE
            val sql = PgQueryBuilder.updateClientCard(dbName, cols).replaceNamedParams()
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    ps.setString(i++, newCard.displayName)
                    ps.setString(i++, newCard.note)
                    ps.setString(i++, status)
                    ps.setString(i++, newCard.lock.asString())
                    ps.setString(i++, now)
                    ps.setString(i++, newCard.id.asString())
                    ps.setString(i++, rq.clientCard.lock.asString())
                    ps.setString(i++, newCard.id.asString())
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val returned = ClientCardRowMapper.map(rs)
                            if (returned.lock == newCard.lock) {
                                DbClientCardResponseOk(returned)
                            } else {
                                errorRepoConcurrencyClientCard(returned, rq.clientCard.lock)
                            }
                        } else {
                            errorNotFoundClientCard(newCard.id)
                        }
                    }
                }
            }
        }

    override suspend fun archiveClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse =
        tryMethod {
            val id = rq.id.takeIf { it != ClientCardId.NONE } ?: return@tryMethod errorEmptyClientCardId
            val oldLock = rq.lock.takeIf { it != ClientCardLock.NONE } ?: return@tryMethod errorEmptyClientCardLock(id)
            val now = nowTimestamp()
            val sql = PgQueryBuilder.archiveClientCard(dbName, cols).replaceNamedParams()
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
                            val returned = ClientCardRowMapper.map(rs)
                            if (returned.lock == oldLock) {
                                DbClientCardResponseOk(returned)
                            } else {
                                errorRepoConcurrencyClientCard(returned, oldLock)
                            }
                        } else {
                            errorNotFoundClientCard(id)
                        }
                    }
                }
            }
        }

    override suspend fun searchClientCards(rq: DbClientCardFilterRequest): IDbClientCardsResponse =
        tryListMethod {
            val hasDisplayName = rq.searchString.isNotBlank()
            val hasStatus = rq.status != ClientCardStatus.NONE
            val sql =
                PgQueryBuilder.searchClientCards(dbName, cols, ownerId = false, status = hasStatus, displayName = hasDisplayName)
                    .replaceNamedParams()
            ds.connection.use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    if (hasStatus) {
                        val statusStr =
                            when (rq.status) {
                                ClientCardStatus.ARCHIVED -> PgClientCardFields.STATUS_ARCHIVED
                                else -> PgClientCardFields.STATUS_ACTIVE
                            }
                        ps.setString(i++, statusStr)
                    }
                    if (hasDisplayName) {
                        ps.setString(i++, "%${rq.searchString}%")
                    }
                    ps.executeQuery().use { rs ->
                        val result = mutableListOf<ClientCard>()
                        while (rs.next()) {
                            result += ClientCardRowMapper.map(rs)
                        }
                        DbClientCardsResponseOk(result)
                    }
                }
            }
        }

    override fun save(cards: Collection<ClientCard>): Collection<ClientCard> {
        val now = nowTimestamp()
        val sql = PgQueryBuilder.insertClientCard(dbName, cols).replaceNamedParams()
        ds.connection.use { conn ->
            return cards.map { card ->
                val savedCard =
                    card.copy(
                        id = if (card.id == ClientCardId.NONE) ClientCardId(randomUuid()) else card.id,
                        lock = if (card.lock == ClientCardLock.NONE) ClientCardLock(randomUuid()) else card.lock,
                    )
                conn.prepareStatement(sql).use { ps ->
                    var i = 1
                    ps.setString(i++, savedCard.id.asString())
                    ps.setString(i++, savedCard.ownerId)
                    ps.setString(i++, savedCard.displayName)
                    ps.setString(i++, savedCard.note)
                    ps.setString(i++, if (savedCard.isArchived) PgClientCardFields.STATUS_ARCHIVED else PgClientCardFields.STATUS_ACTIVE)
                    ps.setString(i++, savedCard.lock.asString())
                    ps.setString(i++, now)
                    ps.setString(i++, now)
                    ps.executeQuery().use { rs ->
                        rs.next()
                        ClientCardRowMapper.map(rs)
                    }
                }
            }
        }
    }

    fun clear() {
        ds.connection.use { conn ->
            conn.createStatement().use { st ->
                st.execute("TRUNCATE TABLE $dbName CASCADE")
            }
        }
    }

    fun close() {
        _ds?.close()
    }

    private fun nowTimestamp(): String = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    private inline fun tryMethod(block: () -> IDbClientCardResponse): IDbClientCardResponse =
        try {
            block()
        } catch (e: Throwable) {
            errorRepoDbClientCard(e)
        }

    private inline fun tryListMethod(block: () -> IDbClientCardsResponse): IDbClientCardsResponse =
        try {
            block()
        } catch (e: Throwable) {
            DbClientCardsResponseErr()
        }
}

internal fun String.replaceNamedParams(): String = replace(Regex("(?<!:):[a-zA-Z_]+"), "?")
