package com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs

import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker

fun ICorChainDsl<IFBContext>.stubNoCase(title: String) = worker {
    this.title = title
    this.description = """
        Обработка ошибки: запрошенный стаб не поддерживается или не существует
    """.trimIndent()
    on { state == State.RUNNING }
    handle {
        state = State.FAILING
        addError(FBError(code = "stub-not-configured", group = "business", field = "", message = "Select a stub case in debug settings or unsupported case: ${stubCase.name}"))
    }
}
