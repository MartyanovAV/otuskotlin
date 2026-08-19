package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

object PgQueryBuilder {
    // ==================== ClientCard ====================

    fun insertClientCard(dbName: String, cols: String): String =
        """
        INSERT INTO $dbName (
          ${PgClientCardFields.ID.quoted()},
          ${PgClientCardFields.OWNER_ID.quoted()},
          ${PgClientCardFields.DISPLAY_NAME.quoted()},
          ${PgClientCardFields.NOTE.quoted()},
          ${PgClientCardFields.STATUS.quoted()},
          ${PgClientCardFields.LOCK.quoted()},
          ${PgClientCardFields.CREATED_AT.quoted()},
          ${PgClientCardFields.UPDATED_AT.quoted()}
        ) VALUES (
          :${PgClientCardFields.ID},
          :${PgClientCardFields.OWNER_ID},
          :${PgClientCardFields.DISPLAY_NAME},
          :${PgClientCardFields.NOTE},
          :${PgClientCardFields.STATUS}::client_card_status_type,
          :${PgClientCardFields.LOCK},
          :${PgClientCardFields.CREATED_AT}::timestamptz,
          :${PgClientCardFields.UPDATED_AT}::timestamptz
        )
        RETURNING $cols
        """.trimIndent()

    fun readClientCard(dbName: String, cols: String): String =
        """
        SELECT $cols
        FROM $dbName
        WHERE ${PgClientCardFields.ID.quoted()} = :${PgClientCardFields.ID}
        """.trimIndent()

    fun updateClientCard(dbName: String, cols: String): String =
        """
        WITH update_obj AS (
            UPDATE $dbName a
            SET ${PgClientCardFields.DISPLAY_NAME.quoted()} = :${PgClientCardFields.DISPLAY_NAME}
            , ${PgClientCardFields.NOTE.quoted()} = :${PgClientCardFields.NOTE}
            , ${PgClientCardFields.STATUS.quoted()} = :${PgClientCardFields.STATUS}::client_card_status_type
            , ${PgClientCardFields.LOCK.quoted()} = :${PgClientCardFields.LOCK}
            , ${PgClientCardFields.UPDATED_AT.quoted()} = :${PgClientCardFields.UPDATED_AT}::timestamptz
            WHERE a.${PgClientCardFields.ID.quoted()} = :${PgClientCardFields.ID}
            AND a.${PgClientCardFields.LOCK.quoted()} = :${PgClientCardFields.LOCK_OLD}
            RETURNING $cols
        ),
        select_obj AS (
            SELECT $cols FROM $dbName
            WHERE ${PgClientCardFields.ID.quoted()} = :${PgClientCardFields.ID}
        )
        (SELECT * FROM update_obj UNION ALL SELECT * FROM select_obj) LIMIT 1
        """.trimIndent()

    fun archiveClientCard(dbName: String, cols: String): String =
        """
        WITH archive_obj AS (
            UPDATE $dbName a
            SET ${PgClientCardFields.STATUS.quoted()} = '${PgClientCardFields.STATUS_ARCHIVED}'::client_card_status_type
            , ${PgClientCardFields.ARCHIVED_AT.quoted()} = :${PgClientCardFields.ARCHIVED_AT}::timestamptz
            , ${PgClientCardFields.UPDATED_AT.quoted()} = :${PgClientCardFields.UPDATED_AT}::timestamptz
            WHERE a.${PgClientCardFields.ID.quoted()} = :${PgClientCardFields.ID}
            AND a.${PgClientCardFields.LOCK.quoted()} = :${PgClientCardFields.LOCK_OLD}
            RETURNING $cols
        ),
        select_obj AS (
            SELECT $cols FROM $dbName
            WHERE ${PgClientCardFields.ID.quoted()} = :${PgClientCardFields.ID}
        )
        (SELECT * FROM archive_obj UNION ALL SELECT * FROM select_obj) LIMIT 1
        """.trimIndent()

    fun searchClientCards(
        dbName: String,
        cols: String,
        ownerId: Boolean,
        status: Boolean,
        displayName: Boolean,
    ): String {
        val where =
            listOfNotNull(
                if (ownerId) "${PgClientCardFields.OWNER_ID.quoted()} = :${PgClientCardFields.OWNER_ID}" else null,
                if (status) {
                    "${PgClientCardFields.STATUS.quoted()} = :${PgClientCardFields.STATUS}::client_card_status_type"
                } else {
                    null
                },
                if (displayName) "${PgClientCardFields.DISPLAY_NAME.quoted()} LIKE :${PgClientCardFields.DISPLAY_NAME}" else null,
            )
                .takeIf { it.isNotEmpty() }
                ?.let { "WHERE ${it.joinToString(separator = " AND ")}" }
                ?: ""
        return """
            SELECT $cols
            FROM $dbName $where
            """.trimIndent()
    }

    // ==================== TrainingPlan ====================

    fun insertTrainingPlan(dbName: String, cols: String): String =
        """
        INSERT INTO $dbName (
          ${PgTrainingPlanFields.ID.quoted()},
          ${PgTrainingPlanFields.CLIENT_CARD_ID.quoted()},
          ${PgTrainingPlanFields.OWNER_ID.quoted()},
          ${PgTrainingPlanFields.TITLE.quoted()},
          ${PgTrainingPlanFields.PLAN_ITEMS.quoted()},
          ${PgTrainingPlanFields.STATUS.quoted()},
          ${PgTrainingPlanFields.VERSION.quoted()},
          ${PgTrainingPlanFields.LOCK.quoted()},
          ${PgTrainingPlanFields.CREATED_AT.quoted()},
          ${PgTrainingPlanFields.UPDATED_AT.quoted()}
        ) VALUES (
          :${PgTrainingPlanFields.ID},
          :${PgTrainingPlanFields.CLIENT_CARD_ID},
          :${PgTrainingPlanFields.OWNER_ID},
          :${PgTrainingPlanFields.TITLE},
          :${PgTrainingPlanFields.PLAN_ITEMS}::jsonb,
          :${PgTrainingPlanFields.STATUS}::training_plan_status_type,
          :${PgTrainingPlanFields.VERSION},
          :${PgTrainingPlanFields.LOCK},
          :${PgTrainingPlanFields.CREATED_AT}::timestamptz,
          :${PgTrainingPlanFields.UPDATED_AT}::timestamptz
        )
        RETURNING $cols
        """.trimIndent()

    fun readTrainingPlan(dbName: String, cols: String): String =
        """
        SELECT $cols
        FROM $dbName
        WHERE ${PgTrainingPlanFields.ID.quoted()} = :${PgTrainingPlanFields.ID}
        """.trimIndent()

    fun updateTrainingPlan(dbName: String, cols: String): String =
        """
        WITH update_obj AS (
            UPDATE $dbName a
            SET ${PgTrainingPlanFields.TITLE.quoted()} = :${PgTrainingPlanFields.TITLE}
            , ${PgTrainingPlanFields.PLAN_ITEMS.quoted()} = :${PgTrainingPlanFields.PLAN_ITEMS}::jsonb
            , ${PgTrainingPlanFields.STATUS.quoted()} = :${PgTrainingPlanFields.STATUS}::training_plan_status_type
            , ${PgTrainingPlanFields.VERSION.quoted()} = :${PgTrainingPlanFields.VERSION}
            , ${PgTrainingPlanFields.LOCK.quoted()} = :${PgTrainingPlanFields.LOCK}
            , ${PgTrainingPlanFields.UPDATED_AT.quoted()} = :${PgTrainingPlanFields.UPDATED_AT}::timestamptz
            WHERE a.${PgTrainingPlanFields.ID.quoted()} = :${PgTrainingPlanFields.ID}
            AND a.${PgTrainingPlanFields.LOCK.quoted()} = :${PgTrainingPlanFields.LOCK_OLD}
            RETURNING $cols
        ),
        select_obj AS (
            SELECT $cols FROM $dbName
            WHERE ${PgTrainingPlanFields.ID.quoted()} = :${PgTrainingPlanFields.ID}
        )
        (SELECT * FROM update_obj UNION ALL SELECT * FROM select_obj) LIMIT 1
        """.trimIndent()

    fun archiveTrainingPlan(dbName: String, cols: String): String =
        """
        WITH archive_obj AS (
            UPDATE $dbName a
            SET ${PgTrainingPlanFields.STATUS.quoted()} = '${PgTrainingPlanFields.STATUS_ARCHIVED}'::training_plan_status_type
            , ${PgTrainingPlanFields.ARCHIVED_AT.quoted()} = :${PgTrainingPlanFields.ARCHIVED_AT}::timestamptz
            , ${PgTrainingPlanFields.UPDATED_AT.quoted()} = :${PgTrainingPlanFields.UPDATED_AT}::timestamptz
            WHERE a.${PgTrainingPlanFields.ID.quoted()} = :${PgTrainingPlanFields.ID}
            AND a.${PgTrainingPlanFields.LOCK.quoted()} = :${PgTrainingPlanFields.LOCK_OLD}
            RETURNING $cols
        ),
        select_obj AS (
            SELECT $cols FROM $dbName
            WHERE ${PgTrainingPlanFields.ID.quoted()} = :${PgTrainingPlanFields.ID}
        )
        (SELECT * FROM archive_obj UNION ALL SELECT * FROM select_obj) LIMIT 1
        """.trimIndent()

    fun searchTrainingPlans(
        dbName: String,
        cols: String,
        ownerId: Boolean,
        clientCardId: Boolean,
        status: Boolean,
        title: Boolean,
    ): String {
        val where =
            listOfNotNull(
                if (ownerId) "${PgTrainingPlanFields.OWNER_ID.quoted()} = :${PgTrainingPlanFields.OWNER_ID}" else null,
                if (clientCardId) "${PgTrainingPlanFields.CLIENT_CARD_ID.quoted()} = :${PgTrainingPlanFields.CLIENT_CARD_ID}" else null,
                if (status) {
                    "${PgTrainingPlanFields.STATUS.quoted()} = :${PgTrainingPlanFields.STATUS}::training_plan_status_type"
                } else {
                    null
                },
                if (title) "${PgTrainingPlanFields.TITLE.quoted()} LIKE :${PgTrainingPlanFields.TITLE}" else null,
            )
                .takeIf { it.isNotEmpty() }
                ?.let { "WHERE ${it.joinToString(separator = " AND ")}" }
                ?: ""
        return """
            SELECT $cols
            FROM $dbName $where
            """.trimIndent()
    }

    fun clear(tableName: String): String = "DELETE FROM $tableName"
}
