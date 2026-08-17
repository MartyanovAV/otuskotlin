package com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs

fun ICorChainDsl<IFBContext>.stubCannotArchive(title: String) =
    worker {
        this.title = title
        this.description =
            """
            Кейс ошибки: невозможно заархивировать
            """.trimIndent()
        on { stubCase == Stubs.CANNOT_ARCHIVE && state == State.RUNNING }
        handle {
            state = State.FAILING
            addError(FBError(code = "cannot-archive", group = "business", field = "", message = "Entity cannot be archived"))
        }
    }
