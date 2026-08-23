package com.github.martyanovav.otuskotlin.fitbridge.training.biz.general

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode

fun <C : IFBContext> ICorChainDsl<C>.stubs(title: String, block: ICorChainDsl<C>.() -> Unit) =
    chain {
        block()
        this.title = title
        on { workMode == WorkMode.STUB && state == State.RUNNING }
    }
