package com.github.martyanovav.otuskotlin.fitbridge.training.app.ktor.configs

import io.ktor.server.config.ApplicationConfig

data class PostgresConfig(
    val host: String = "localhost",
    val port: Int = 5432,
    val user: String = "postgres",
    val password: String = "postgres",
    val database: String = "fitbridge_training",
    val schema: String = "public",
) {
    constructor(config: ApplicationConfig) : this(
        host =
            config.propertyOrNull("$PATH.host")?.getString()
                ?: System.getenv("DB_HOST") ?: "localhost",
        port =
            config.propertyOrNull("$PATH.port")?.getString()?.toIntOrNull()
                ?: System.getenv("DB_PORT")?.toIntOrNull() ?: 5432,
        user =
            config.propertyOrNull("$PATH.user")?.getString()
                ?: System.getenv("DB_USER") ?: "postgres",
        password =
            config.propertyOrNull("$PATH.password")?.getString()
                ?: System.getenv("DB_PASSWORD") ?: "postgres",
        database =
            config.propertyOrNull("$PATH.database")?.getString()
                ?: System.getenv("DB_NAME") ?: "fitbridge_training",
        schema = config.propertyOrNull("$PATH.schema")?.getString() ?: "public",
    )

    companion object {
        const val PATH = "${ConfigPaths.REPOSITORY}.psql"
    }
}
