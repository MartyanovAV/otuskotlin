package com.github.martyanovav.otuskotlin.fitbridge.training.biz.general

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

fun <C : IFBContext> ICorChainDsl<C>.initStatus(title: String) =
    worker {
        this.title = title
        this.description =
            """
            Этот обработчик устанавливает стартовый статус обработки. Запускается только в случае не заданного статуса.
            """.trimIndent()
        on { state == State.NONE }
        handle { state = State.RUNNING }
    }
