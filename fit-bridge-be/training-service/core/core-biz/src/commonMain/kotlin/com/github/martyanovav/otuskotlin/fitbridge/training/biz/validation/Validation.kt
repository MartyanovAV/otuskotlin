package com.github.martyanovav.otuskotlin.fitbridge.training.biz.validation

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.errorValidation
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

val ID_PATTERN = Regex("^[0-9a-zA-Z#:_-]+$")
val HAS_LETTER = Regex("\\p{L}")

fun <C : IFBContext> ICorChainDsl<C>.validation(
    title: String,
    block: ICorChainDsl<C>.() -> Unit,
) = chain {
    this.title = title
    on { state == State.RUNNING }
    block()
}

fun <C : IFBContext> ICorChainDsl<C>.validationWorker(
    title: String,
    field: String,
    violationCode: String,
    description: String,
    predicate: suspend C.() -> Boolean,
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
