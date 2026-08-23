package com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs

fun <C : IFBContext> ICorChainDsl<C>.stubNotFound(title: String) =
    worker {
        this.title = title
        this.description =
            """
            Кейс ошибки: объект не найден
            """.trimIndent()
        on { stubCase == Stubs.NOT_FOUND && state == State.RUNNING }
        handle {
            state = State.FAILING
            addError(FBError(code = "not-found", group = "business", field = "", message = "Entity was not found"))
        }
    }
