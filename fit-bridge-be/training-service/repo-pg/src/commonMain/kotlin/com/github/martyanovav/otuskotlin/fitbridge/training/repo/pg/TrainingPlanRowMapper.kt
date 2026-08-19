package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.PlanItem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlan
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.sql.ResultSet

object TrainingPlanRowMapper {
    private val json =
        Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }

    fun map(rs: ResultSet): TrainingPlan {
        val statusStr = rs.getString(PgTrainingPlanFields.STATUS)
        val planItemsJson = rs.getString(PgTrainingPlanFields.PLAN_ITEMS)
        val planItems =
            if (planItemsJson.isNotBlank()) {
                json.decodeFromString<List<PlanItem>>(planItemsJson)
            } else {
                emptyList()
            }
        return TrainingPlan(
            id = TrainingPlanId(rs.getString(PgTrainingPlanFields.ID)),
            clientCardId = ClientCardId(rs.getString(PgTrainingPlanFields.CLIENT_CARD_ID)),
            ownerId = rs.getString(PgTrainingPlanFields.OWNER_ID),
            title = rs.getString(PgTrainingPlanFields.TITLE),
            status =
                when (statusStr) {
                    PgTrainingPlanFields.STATUS_ARCHIVED -> TrainingPlanStatus.ARCHIVED
                    else -> TrainingPlanStatus.ACTIVE
                },
            lock = TrainingPlanLock(rs.getString(PgTrainingPlanFields.LOCK)),
            planItems = planItems,
            version = rs.getInt(PgTrainingPlanFields.VERSION),
            createdAt = rs.getTimestamp(PgTrainingPlanFields.CREATED_AT)?.toInstant()?.toString().orEmpty(),
            updatedAt = rs.getTimestamp(PgTrainingPlanFields.UPDATED_AT)?.toInstant()?.toString().orEmpty(),
        )
    }

    fun serializePlanItems(items: List<PlanItem>): String = json.encodeToString(items)
}
