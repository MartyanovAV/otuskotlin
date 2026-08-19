package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.plugins

import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.AppSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.configs.ConfigPaths
import com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.configs.PostgresConfig
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg.PgProperties
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg.RepoClientCardPg
import com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg.RepoTrainingPlanPg
import io.ktor.server.application.Application

enum class DbType(val confName: String) {
    PROD("prod"),
    TEST("test"),
}

fun Application.getDatabaseConfClientCard(type: DbType) =
    getDatabaseConf(type) { config -> RepoClientCardPg(config) }

fun Application.getDatabaseConfTrainingPlan(type: DbType) =
    getDatabaseConf(type) { config -> RepoTrainingPlanPg(config) }

private fun <T> Application.getDatabaseConf(
    type: DbType,
    pgFactory: (PgProperties) -> T,
): T {
    val dbSettingPath = "${ConfigPaths.REPOSITORY}.${type.confName}"
    val dbSetting = environment.config.propertyOrNull(dbSettingPath)?.getString()?.lowercase()
    return when (dbSetting) {
        "in-memory", "inmemory", "memory", "mem" -> throw IllegalStateException("Use in-memory factory instead")
        "postgres", "postgresql", "pg", "sql", "psql" -> {
            val config = PostgresConfig(environment.config)
            pgFactory(
                PgProperties(
                    host = config.host,
                    port = config.port,
                    user = config.user,
                    password = config.password,
                    database = config.database,
                    schema = config.schema,
                ),
            )
        }
        else -> throw IllegalStateException(
            "$dbSettingPath must be set in application.yaml to one of: 'inmemory', 'postgres'",
        )
    }
}

fun Application.initAppSettings(): AppSettings {
    val dbTypePath = "${ConfigPaths.REPOSITORY}.prod"
    val dbType = environment.config.propertyOrNull(dbTypePath)?.getString()?.lowercase()
    val usePostgres = dbType in listOf("postgres", "postgresql", "pg", "sql", "psql")

    return if (usePostgres) {
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
        AppSettings(
            corSettings =
                com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings(
                    wsSessionsV1 = com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionRepo(),
                    wsSessionsV2 = com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.base.KtorWsSessionRepo(),
                    repoClientCardTest = RepoClientCardPg(pgProps),
                    repoClientCardProd = RepoClientCardPg(pgProps),
                    repoClientCardStub = com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoClientCardStub(),
                    repoTrainingPlanTest = RepoTrainingPlanPg(pgProps),
                    repoTrainingPlanProd = RepoTrainingPlanPg(pgProps),
                    repoTrainingPlanStub = com.github.martyanovav.otuskotlin.fitbridge.training.repo.stubs.RepoTrainingPlanStub(),
                ),
        )
    } else {
        AppSettings()
    }
}
