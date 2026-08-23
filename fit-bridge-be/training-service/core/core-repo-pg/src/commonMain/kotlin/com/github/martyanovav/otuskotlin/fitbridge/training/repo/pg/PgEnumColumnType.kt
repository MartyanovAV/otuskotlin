package com.github.martyanovav.otuskotlin.fitbridge.training.repo.pg

import org.jetbrains.exposed.v1.core.ColumnType
import org.postgresql.util.PGobject

class PgEnumColumnType(
    private val enumTypeName: String,
) : ColumnType<String>() {
    override fun sqlType(): String = enumTypeName

    override fun valueFromDB(value: Any): String = value.toString()

    override fun notNullValueToDB(value: String): Any =
        PGobject().apply {
            type = enumTypeName
            this.value = value
        }
}
