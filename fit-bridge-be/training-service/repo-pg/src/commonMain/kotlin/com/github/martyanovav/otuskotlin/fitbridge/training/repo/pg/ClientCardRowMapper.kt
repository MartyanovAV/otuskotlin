package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCard
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardId
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import java.sql.ResultSet

object ClientCardRowMapper {
    fun map(rs: ResultSet): ClientCard {
        val status = rs.getString(PgClientCardFields.STATUS)
        val isArchived = status == PgClientCardFields.STATUS_ARCHIVED
        return ClientCard(
            id = ClientCardId(rs.getString(PgClientCardFields.ID)),
            ownerId = rs.getString(PgClientCardFields.OWNER_ID),
            displayName = rs.getString(PgClientCardFields.DISPLAY_NAME),
            isArchived = isArchived,
            note = rs.getString(PgClientCardFields.NOTE),
            lock = ClientCardLock(rs.getString(PgClientCardFields.LOCK)),
            createdAt = rs.getTimestamp(PgClientCardFields.CREATED_AT)?.toInstant()?.toString().orEmpty(),
            updatedAt = rs.getTimestamp(PgClientCardFields.UPDATED_AT)?.toInstant()?.toString().orEmpty(),
        )
    }
}
