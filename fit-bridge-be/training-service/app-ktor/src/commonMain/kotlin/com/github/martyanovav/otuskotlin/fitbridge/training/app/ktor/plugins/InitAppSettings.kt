package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.FbLoggerProvider
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionRepo
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.configs.ConfigPaths
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.configs.PostgresConfig
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoClientCardInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.inmemory.RepoTrainingPlanInMemory
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg.PgProperties
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg.RepoClientCardPg
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg.RepoTrainingPlanPg
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoClientCardStub
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoTrainingPlanStub
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.initAppSettings(
    loggerProvider: FbLoggerProvider = FbLoggerProvider()
): AppSettings {
    val prodType = environment.config.propertyOrNull("${ConfigPaths.REPOSITORY}.prod")?.getString()?.lowercase() ?: "inmemory"
    val testType = environment.config.propertyOrNull("${ConfigPaths.REPOSITORY}.test")?.getString()?.lowercase() ?: "inmemory"

    val isProdPg = prodType in listOf("postgres", "postgresql", "pg", "sql", "psql")
    val isTestPg = testType in listOf("postgres", "postgresql", "pg", "sql", "psql")

    var dsToClose: HikariDataSource? = null
    val sharedDb: Database? by lazy {
        val pgConfig = PostgresConfig(environment.config)
        val pgProps =
            PgProperties(
                host = pgConfig.host,
                port = pgConfig.port,
                user = pgConfig.user,
                password = pgConfig.password,
                database = pgConfig.database,
                schema = pgConfig.schema,
            )
        val ds =
            HikariConfig().apply {
                jdbcUrl = pgProps.url
                username = pgProps.user
                password = pgProps.password
                maximumPoolSize = pgProps.maxConnections
                isAutoCommit = true
            }.let { HikariDataSource(it) }
        dsToClose = ds
        Database.connect(ds)
    }

    val repoClientCardProd = if (isProdPg) RepoClientCardPg(db = sharedDb) else RepoClientCardInMemory()
    val repoTrainingPlanProd = if (isProdPg) RepoTrainingPlanPg(db = sharedDb) else RepoTrainingPlanInMemory()

    val repoClientCardTest = if (isTestPg) RepoClientCardPg(db = sharedDb) else RepoClientCardInMemory()
    val repoTrainingPlanTest = if (isTestPg) RepoTrainingPlanPg(db = sharedDb) else RepoTrainingPlanInMemory()

    environment.monitor.subscribe(ApplicationStopped) {
        dsToClose?.close()
    }

    return AppSettings(
        corSettings =
            CorSettings(
                loggerProvider = loggerProvider,
                wsSessionsV1 = KtorWsSessionRepo(),
                wsSessionsV2 = KtorWsSessionRepo(),
                repoClientCardTest = repoClientCardTest,
                repoClientCardProd = repoClientCardProd,
                repoClientCardStub = RepoClientCardStub(),
                repoTrainingPlanTest = repoTrainingPlanTest,
                repoTrainingPlanProd = repoTrainingPlanProd,
                repoTrainingPlanStub = RepoTrainingPlanStub(),
            ),
    )
}
