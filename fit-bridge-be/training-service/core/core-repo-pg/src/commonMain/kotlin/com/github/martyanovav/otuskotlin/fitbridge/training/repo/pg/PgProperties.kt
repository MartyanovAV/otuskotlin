package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

data class PgProperties(
    val host: String = "localhost",
    val port: Int = 5432,
    val user: String = "postgres",
    val password: String = "postgres",
    val database: String = "fitbridge_training",
    val schema: String = "public",
    val maxConnections: Int = 4,
) {
    val url: String
        get() = "jdbc:postgresql://$host:$port/$database?currentSchema=$schema"
}
