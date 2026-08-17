package com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers

import com.github.martyanovav.otuskotlin.fitbridge.logging.common.LogLevel
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

fun errorValidation(
    field: String,
    violationCode: String,
    description: String,
    level: LogLevel = LogLevel.ERROR,
) = FBError(
    code = "validation-$field-$violationCode",
    group = "validation",
    field = field,
    message = "Validation error for field $field: $description",
    level = level,
)

fun IFBContext.fail(error: FBError) {
    addError(error)
    state = State.FAILING
}
