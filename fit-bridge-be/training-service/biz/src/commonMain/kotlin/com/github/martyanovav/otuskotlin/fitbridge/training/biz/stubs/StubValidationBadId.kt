package com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs

import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker

fun ICorChainDsl<IFBContext>.stubValidationBadId(title: String) = worker {
    this.title = title
    this.description = """
        Кейс ошибки валидации: ID невалиден
    """.trimIndent()
    on { stubCase == Stubs.BAD_ID && state == State.RUNNING }
    handle {
        state = State.FAILING
        addError(FBError(code = "bad-id", group = "business", field = "id", message = "Id is invalid"))
    }
}
