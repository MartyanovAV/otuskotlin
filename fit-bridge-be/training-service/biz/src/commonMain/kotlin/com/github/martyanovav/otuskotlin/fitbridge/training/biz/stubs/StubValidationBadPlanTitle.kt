package com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs

fun ICorChainDsl<IFBContext>.stubValidationBadPlanTitle(title: String) =
    worker {
        this.title = title
        this.description =
            """
            Кейс ошибки валидации: название плана невалидно
            """.trimIndent()
        on { stubCase == Stubs.BAD_PLAN_TITLE && state == State.RUNNING }
        handle {
            state = State.FAILING
            addError(FBError(code = "bad-plan-title", group = "business", field = "title", message = "Training plan title is invalid"))
        }
    }
