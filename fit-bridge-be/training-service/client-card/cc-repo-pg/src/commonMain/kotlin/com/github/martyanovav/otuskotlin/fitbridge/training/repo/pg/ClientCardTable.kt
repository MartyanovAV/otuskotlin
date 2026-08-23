package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.javatime.timestamp

object ClientCardTable : Table("client_card") {
    val id = varchar("id", 64)
    val ownerUserId = varchar("owner_user_id", 128)
    val createdByUserId = varchar("created_by_user_id", 128)
    val displayName = varchar("display_name", 120)
    val note = varchar("note", 1000)
    val status = registerColumn("status", PgEnumColumnType("client_card_status_type"))
    val lock = varchar("lock", 64)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val archivedAt = timestamp("archived_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

class ClientCardStatusColumnType : VarCharColumnType(20) {
    override fun sqlType(): String = "client_card_status_type"
}
