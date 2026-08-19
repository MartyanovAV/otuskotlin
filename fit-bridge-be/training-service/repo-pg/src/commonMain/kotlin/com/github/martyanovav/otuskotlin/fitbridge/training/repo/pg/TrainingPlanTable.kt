package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.PlanItem
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import org.jetbrains.exposed.v1.json.jsonb

internal val planItemsJson =
    Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

object TrainingPlanTable : Table("training_plan") {
    val id = varchar("id", 64)
    val clientCardId = varchar("client_card_id", 64)
    val ownerId = varchar("owner_id", 128)
    val title = varchar("title", 120)
    val planItems =
        jsonb<List<PlanItem>>(
            "plan_items",
            serialize = { planItemsJson.encodeToString(it) },
            deserialize = { planItemsJson.decodeFromString(it) },
        )
    val status = registerColumn("status", PgEnumColumnType("training_plan_status_type"))
    val version = integer("version")
    val lock = varchar("lock", 64)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    val archivedAt = timestampWithTimeZone("archived_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

class TrainingPlanStatusColumnType : VarCharColumnType(20) {
    override fun sqlType(): String = "training_plan_status_type"
}
