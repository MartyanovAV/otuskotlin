package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanFilterRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbTrainingPlanRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlanResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IDbTrainingPlansResponse
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.IRepoTrainingPlanInitializable
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.RepoClientCardInitialized
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.common.RepoTrainingPlanInitialized
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.MountableFile
import java.sql.SQLException

object PgTestContainer {
    val container: PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("fitbridge_test")
            .withUsername("postgres")
            .withPassword("postgres")
            .withCommand("postgres", "-c", "max_connections=300")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("init.sql"),
                "/docker-entrypoint-initdb.d/init.sql",
            )

    private val reposToClose = mutableListOf<() -> Unit>()

    init {
        container.start()
        Runtime.getRuntime().addShutdownHook(
            Thread {
                reposToClose.forEach { it() }
            }
        )
    }

    fun registerForCleanup(closeFn: () -> Unit) {
        reposToClose.add(closeFn)
    }

    val properties =
        PgProperties(
            host = container.host,
            port = container.firstMappedPort,
            user = container.username,
            password = container.password,
            database = container.databaseName,
        )

    val sharedDb: Database by lazy {
        val dataSource =
            HikariConfig().apply {
                jdbcUrl = properties.url
                username = properties.user
                password = properties.password
                maximumPoolSize = properties.maxConnections
                isAutoCommit = true
            }.let { HikariDataSource(it) }
        registerForCleanup { dataSource.close() }
        Database.connect(dataSource)
    }
}

fun testClientCardRepo(
    initObjects: List<ClientCard> = emptyList(),
    randomUuid: () -> String = { java.util.UUID.randomUUID().toString() },
) = RepoClientCardInitialized(
    repo =
        RepoClientCardPg(PgTestContainer.properties, db = PgTestContainer.sharedDb, randomUuid = randomUuid).apply {
            clear()
        },
    initObjects = initObjects,
)

class TpRepoWithFkGuard(
    private val delegate: RepoTrainingPlanPg,
    private val ccRepo: RepoClientCardPg,
    private val parentClientCards: List<ClientCard>,
) : IRepoTrainingPlanInitializable by delegate {
    private fun ensureParents() {
        parentClientCards.forEach { card ->
            try {
                ccRepo.save(listOf(card))
            } catch (_: SQLException) {
                // Parent card already exists — ignore duplicate key
            }
        }
    }

    override suspend fun createTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
        ensureParents()
        return delegate.createTrainingPlan(rq)
    }

    override suspend fun readTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse {
        ensureParents()
        return delegate.readTrainingPlan(rq)
    }

    override suspend fun updateTrainingPlan(rq: DbTrainingPlanRequest): IDbTrainingPlanResponse {
        ensureParents()
        return delegate.updateTrainingPlan(rq)
    }

    override suspend fun archiveTrainingPlan(rq: DbTrainingPlanIdRequest): IDbTrainingPlanResponse {
        ensureParents()
        return delegate.archiveTrainingPlan(rq)
    }

    override suspend fun searchTrainingPlans(rq: DbTrainingPlanFilterRequest): IDbTrainingPlansResponse {
        ensureParents()
        return delegate.searchTrainingPlans(rq)
    }

    override fun save(plans: Collection<TrainingPlan>): Collection<TrainingPlan> {
        ensureParents()
        return delegate.save(plans)
    }
}

fun testTrainingPlanRepo(
    initObjects: List<TrainingPlan> = emptyList(),
    parentClientCards: List<ClientCard> = emptyList(),
    randomUuid: () -> String = { java.util.UUID.randomUUID().toString() },
): RepoTrainingPlanInitialized {
    val tpRepo = RepoTrainingPlanPg(PgTestContainer.properties, db = PgTestContainer.sharedDb, randomUuid = randomUuid)
    val ccRepo = RepoClientCardPg(PgTestContainer.properties, db = PgTestContainer.sharedDb)
    tpRepo.clear()
    ccRepo.clear()
    parentClientCards.forEach { ccRepo.save(listOf(it)) }
    val guarded = TpRepoWithFkGuard(tpRepo, ccRepo, parentClientCards)
    return RepoTrainingPlanInitialized(repo = guarded, initObjects = initObjects)
}
