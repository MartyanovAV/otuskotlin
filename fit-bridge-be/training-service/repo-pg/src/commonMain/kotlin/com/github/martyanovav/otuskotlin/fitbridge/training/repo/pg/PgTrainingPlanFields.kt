package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

object PgTrainingPlanFields {
    const val ID = "id"
    const val CLIENT_CARD_ID = "client_card_id"
    const val OWNER_ID = "owner_id"
    const val TITLE = "title"
    const val PLAN_ITEMS = "plan_items"
    const val STATUS = "status"
    const val VERSION = "version"
    const val LOCK = "lock"
    const val LOCK_OLD = "lock_old"
    const val CREATED_AT = "created_at"
    const val UPDATED_AT = "updated_at"
    const val ARCHIVED_AT = "archived_at"

    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_ARCHIVED = "ARCHIVED"

    val allFields =
        listOf(
            ID, CLIENT_CARD_ID, OWNER_ID, TITLE, PLAN_ITEMS, STATUS, VERSION, LOCK,
            CREATED_AT, UPDATED_AT,
        )
}
