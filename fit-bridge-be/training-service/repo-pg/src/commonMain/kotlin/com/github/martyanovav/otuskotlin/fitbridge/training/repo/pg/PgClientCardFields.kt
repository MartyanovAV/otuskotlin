package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

object PgClientCardFields {
    const val ID = "id"
    const val OWNER_ID = "owner_id"
    const val DISPLAY_NAME = "display_name"
    const val NOTE = "note"
    const val STATUS = "status"
    const val LOCK = "lock"
    const val LOCK_OLD = "lock_old"
    const val CREATED_AT = "created_at"
    const val UPDATED_AT = "updated_at"
    const val ARCHIVED_AT = "archived_at"

    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_ARCHIVED = "ARCHIVED"

    val allFields =
        listOf(
            ID, OWNER_ID, DISPLAY_NAME, NOTE, STATUS, LOCK, CREATED_AT, UPDATED_AT,
        )
}

internal fun String.quoted() = "\"$this\""
