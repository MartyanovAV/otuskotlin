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

fun IFBContext.fail(errors: Collection<FBError>) {
    errors.forEach { addError(it) }
    state = State.FAILING
}

fun errorSystem(
    violationCode: String,
    level: LogLevel = LogLevel.ERROR,
    e: Throwable,
) = FBError(
    code = "system-$violationCode",
    group = "system",
    message = "System error occurred. Our stuff has been informed, please retry later",
    level = level,
    exception = e,
)
