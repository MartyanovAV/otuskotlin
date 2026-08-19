package com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory

import com.benasher44.uuid.uuid4
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardsResponseOk
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbClientCardResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbClientCardsResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.RepoClientCardBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorEmptyClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorNotFoundClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.errorRepoConcurrencyClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.IRepoClientCardInitializable
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RepoClientCardInMemory(
    ttl: Duration = 2.minutes,
    val randomUuid: () -> String = { uuid4().toString() },
) : RepoClientCardBase(), IRepoClientCard, IRepoClientCardInitializable {
    private val mutex: Mutex = Mutex()
    private val cache =
        Cache.Builder<String, ClientCardEntity>()
            .expireAfterWrite(ttl)
            .build()

    override fun save(cards: Collection<ClientCard>) =
        cards.map { card ->
            val entity = ClientCardEntity(card)
            require(entity.id != null)
            cache.put(entity.id, entity)
            card
        }

    override suspend fun createClientCard(rq: DbClientCardRequest): IDbClientCardResponse =
        tryClientCardMethod {
            val key = randomUuid()
            val card = rq.clientCard.copy(id = ClientCardId(key))
            val entity = ClientCardEntity(card)
            mutex.withLock {
                cache.put(key, entity)
            }
            DbClientCardResponseOk(card)
        }

    override suspend fun readClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse =
        tryClientCardMethod {
            val key =
                rq.id.takeIf { it != ClientCardId.NONE }?.asString()
                    ?: return@tryClientCardMethod errorEmptyClientCardId
            mutex.withLock {
                cache.get(key)
                    ?.let { DbClientCardResponseOk(it.toInternal()) }
                    ?: errorNotFoundClientCard(rq.id)
            }
        }

    override suspend fun updateClientCard(rq: DbClientCardRequest): IDbClientCardResponse =
        tryClientCardMethod {
            val rqCard = rq.clientCard
            val id = rqCard.id.takeIf { it != ClientCardId.NONE } ?: return@tryClientCardMethod errorEmptyClientCardId
            val key = id.asString()

            mutex.withLock {
                val oldEntity = cache.get(key)
                val oldCard = oldEntity?.toInternal()
                when {
                    oldCard == null -> errorNotFoundClientCard(id)
                    oldCard.lock != ClientCardLock.NONE && oldCard.lock != rqCard.lock ->
                        errorRepoConcurrencyClientCard(oldCard, rqCard.lock)
                    else -> {
                        val newCard = rqCard.copy(lock = ClientCardLock(randomUuid()))
                        val entity = ClientCardEntity(newCard)
                        cache.put(key, entity)
                        DbClientCardResponseOk(newCard)
                    }
                }
            }
        }

    override suspend fun archiveClientCard(rq: DbClientCardIdRequest): IDbClientCardResponse =
        tryClientCardMethod {
            val id = rq.id.takeIf { it != ClientCardId.NONE } ?: return@tryClientCardMethod errorEmptyClientCardId
            val key = id.asString()

            mutex.withLock {
                val oldEntity = cache.get(key)
                val oldCard = oldEntity?.toInternal()
                when {
                    oldCard == null -> errorNotFoundClientCard(id)
                    oldCard.lock != ClientCardLock.NONE && oldCard.lock != rq.lock ->
                        errorRepoConcurrencyClientCard(oldCard, rq.lock)
                    else -> {
                        val archivedCard = oldCard.copy(isArchived = true, lock = ClientCardLock(randomUuid()))
                        val entity = ClientCardEntity(archivedCard)
                        cache.put(key, entity)
                        DbClientCardResponseOk(archivedCard)
                    }
                }
            }
        }

    override suspend fun searchClientCards(rq: DbClientCardFilterRequest): IDbClientCardsResponse =
        tryClientCardsMethod {
            val result: List<ClientCard> =
                cache.asMap().asSequence()
                    .filter { entry ->
                        rq.status.takeIf { it != ClientCardStatus.NONE }?.let {
                            val card = entry.value.toInternal()
                            if (rq.status == ClientCardStatus.ARCHIVED) card.isArchived else !card.isArchived
                        } ?: true
                    }
                    .filter { entry ->
                        rq.searchString.takeIf { it.isNotBlank() }?.let {
                            entry.value.displayName?.contains(it) ?: false
                        } ?: true
                    }
                    .map { it.value.toInternal() }
                    .toList()
            DbClientCardsResponseOk(result)
        }
}
