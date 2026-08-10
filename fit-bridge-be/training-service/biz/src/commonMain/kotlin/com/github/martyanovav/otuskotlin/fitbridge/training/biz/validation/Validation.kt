package com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.errorValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

internal val ID_PATTERN = Regex("^[0-9a-zA-Z#:_-]+$")
internal val HAS_LETTER = Regex("\\p{L}")

fun ICorChainDsl<IFBContext>.clientCardValidation(
    title: String,
    block: ICorChainDsl<IFBContext>.() -> Unit,
) = chain {
    this.title = title
    on { state == State.RUNNING && this is ClientCardContext }
    block()
}

fun ICorChainDsl<IFBContext>.trainingPlanValidation(
    title: String,
    block: ICorChainDsl<IFBContext>.() -> Unit,
) = chain {
    this.title = title
    on { state == State.RUNNING && this is TrainingPlanContext }
    block()
}

internal fun ICorChainDsl<IFBContext>.validationWorker(
    title: String,
    field: String,
    violationCode: String,
    description: String,
    predicate: suspend IFBContext.() -> Boolean,
) = worker {
    this.title = title
    on(predicate)
    handle {
        fail(
            errorValidation(
                field = field,
                violationCode = violationCode,
                description = description,
            ),
        )
    }
}
