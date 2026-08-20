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
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
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

class RepoClientCardPg(
    private val properties: PgProperties = PgProperties(),
    private val db: Database? = null,
    private val randomUuid: () -> String = { UUID.randomUUID().toString() },
) : RepoClientCardBase(), IRepoClientCardInitializable {
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

    override suspend fun createClientCard(rq: DbClientCardRequest): IDbClientCardResponse =
        tryMethod {
            val card =
                rq.clientCard.copy(
                    id = ClientCardId(randomUuid()),
                    lock = ClientCardLock(randomUuid()),
                )
            val now = Instant.now()
            val status = statusToString(card.isArchived)
            val result =
                transaction(database) {
                    ClientCardTable.insertReturning {
                        it[id] = card.id.asString()
                        it[ownerUserId] = card.ownerUserId
                        it[createdByUserId] = card.createdByUserId
                        it[displayName] = card.displayName
                        it[note] = card.note
                        it[ClientCardTable.status] = status
                        it[lock] = card.lock.asString()
                        it[createdAt] = now
                        it[updatedAt] = now
                    }.single().toClientCard()
                }
            DbClientCardResponseOk(result)
        }

    override suspend fun readClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse =
        tryMethod {
            val result =
                transaction(database) {
                    ClientCardTable
                        .selectAll()
                        .where { ClientCardTable.id eq rq.id.asString() }
                        .singleOrNull()
                        ?.toClientCard()
                }
            if (result != null) {
                DbClientCardResponseOk(result)
            } else {
                errorNotFoundClientCard(rq.id)
            }
        }

    override suspend fun updateClientCard(rq: DbClientCardRequest): IDbClientCardResponse =
        tryMethod {
            val newCard = rq.clientCard.copy(lock = ClientCardLock(randomUuid()))
            val now = Instant.now()
            val status = statusToString(newCard.isArchived)
            val result =
                transaction(database) {
                    val affected =
                        ClientCardTable.update(
                            where = {
                                (ClientCardTable.id eq newCard.id.asString()) and
                                    (ClientCardTable.lock eq rq.clientCard.lock.asString())
                            },
                        ) {
                            it[displayName] = newCard.displayName
                            it[note] = newCard.note
                            it[ClientCardTable.status] = status
                            it[lock] = newCard.lock.asString()
                            it[updatedAt] = now
                        }
                    if (affected > 0) {
                        val returned =
                            ClientCardTable
                                .selectAll()
                                .where { ClientCardTable.id eq newCard.id.asString() }
                                .single()
                                .toClientCard()
                        DbClientCardResponseOk(returned)
                    } else {
                        val current =
                            ClientCardTable
                                .selectAll()
                                .where { ClientCardTable.id eq newCard.id.asString() }
                                .singleOrNull()
                                ?.toClientCard()
                        if (current != null) {
                            errorRepoConcurrencyClientCard(current, rq.clientCard.lock)
                        } else {
                            errorNotFoundClientCard(newCard.id)
                        }
                    }
                }
            result
        }

    override suspend fun archiveClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse =
        tryMethod {
            val id = rq.id.takeIf { it != ClientCardId.NONE } ?: return@tryMethod errorEmptyClientCardId
            val oldLock = rq.lock.takeIf { it != ClientCardLock.NONE } ?: return@tryMethod errorEmptyClientCardLock(id)
            val newLock = ClientCardLock(randomUuid())
            val now = Instant.now()
            val result =
                transaction(database) {
                    val affected =
                        ClientCardTable.update(
                            where = {
                                (ClientCardTable.id eq id.asString()) and
                                    (ClientCardTable.lock eq oldLock.asString())
                            },
                        ) {
                            it[status] = STATUS_ARCHIVED
                            it[lock] = newLock.asString()
                            it[archivedAt] = now
                            it[updatedAt] = now
                        }
                    if (affected > 0) {
                        val returned =
                            ClientCardTable
                                .selectAll()
                                .where { ClientCardTable.id eq id.asString() }
                                .single()
                                .toClientCard()
                        if (returned.lock == newLock) {
                            DbClientCardResponseOk(returned)
                        } else {
                            errorRepoConcurrencyClientCard(returned, oldLock)
                        }
                    } else {
                        val current =
                            ClientCardTable
                                .selectAll()
                                .where { ClientCardTable.id eq id.asString() }
                                .singleOrNull()
                                ?.toClientCard()
                        if (current != null) {
                            errorRepoConcurrencyClientCard(current, oldLock)
                        } else {
                            errorNotFoundClientCard(id)
                        }
                    }
                }
            result
        }

    override suspend fun searchClientCards(rq: DbClientCardFilterRequest): IDbClientCardsResponse =
        tryListMethod {
            val hasDisplayName = rq.searchString.isNotBlank()
            val hasStatus = rq.status != ClientCardStatus.NONE
            val hasOwnerUserId = rq.ownerUserId.isNotBlank()
            val result =
                transaction(database) {
                    val conditions = mutableListOf<Op<Boolean>>()
                    if (hasStatus) {
                        conditions +=
                            ClientCardTable.status eq
                            statusToString(rq.status == ClientCardStatus.ARCHIVED)
                    }
                    if (hasDisplayName) {
                        conditions +=
                            ClientCardTable.displayName like "%${rq.searchString}%"
                    }
                    if (hasOwnerUserId) {
                        conditions += ClientCardTable.ownerUserId eq rq.ownerUserId
                    }
                    val query =
                        if (conditions.isNotEmpty()) {
                            ClientCardTable.selectAll().where { conditions.reduce { acc, op -> acc and op } }
                        } else {
                            ClientCardTable.selectAll()
                        }
                    query.map { it.toClientCard() }
                }
            DbClientCardsResponseOk(result)
        }

    override fun save(cards: Collection<ClientCard>): Collection<ClientCard> {
        val now = Instant.now()
        return transaction(database) {
            cards.map { card ->
                val savedCard =
                    card.copy(
                        id = if (card.id == ClientCardId.NONE) ClientCardId(randomUuid()) else card.id,
                        lock = if (card.lock == ClientCardLock.NONE) ClientCardLock(randomUuid()) else card.lock,
                    )
                ClientCardTable.insertReturning {
                    it[id] = savedCard.id.asString()
                    it[ownerUserId] = savedCard.ownerUserId
                    it[createdByUserId] = savedCard.createdByUserId
                    it[displayName] = savedCard.displayName
                    it[note] = savedCard.note
                    it[ClientCardTable.status] = statusToString(savedCard.isArchived)
                    it[lock] = savedCard.lock.asString()
                    it[createdAt] = now
                    it[updatedAt] = now
                }.single().toClientCard()
            }
        }
    }

    fun clear() {
        transaction(database) {
            ClientCardTable.deleteAll()
        }
    }

    fun close() {
        dsRef?.close()
    }

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

    companion object {
        private const val STATUS_ACTIVE = "ACTIVE"
        private const val STATUS_ARCHIVED = "ARCHIVED"

        private fun statusToString(isArchived: Boolean): String =
            if (isArchived) STATUS_ARCHIVED else STATUS_ACTIVE
    }
}

private fun ResultRow.toClientCard(): ClientCard =
    ClientCard(
        id = ClientCardId(this[ClientCardTable.id]),
        ownerUserId = this[ClientCardTable.ownerUserId],
        createdByUserId = this[ClientCardTable.createdByUserId],
        displayName = this[ClientCardTable.displayName],
        isArchived = this[ClientCardTable.status] == "ARCHIVED",
        note = this[ClientCardTable.note],
        lock = ClientCardLock(this[ClientCardTable.lock]),
        createdAt = this[ClientCardTable.createdAt].toString(),
        updatedAt = this[ClientCardTable.updatedAt].toString(),
    )
