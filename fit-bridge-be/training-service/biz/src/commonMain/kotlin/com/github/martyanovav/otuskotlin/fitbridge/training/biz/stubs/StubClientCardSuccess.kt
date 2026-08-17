package com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.stubs.ClientCardStub

fun ICorChainDsl<IFBContext>.stubClientCardSuccess(title: String) =
    worker {
        this.title = title
        this.description =
            """
            Кейс успеха для карточки клиента
            """.trimIndent()
        on { stubCase == Stubs.SUCCESS && state == State.RUNNING && this is ClientCardContext }
        handle {
            val ctx = this as ClientCardContext
            ctx.state = State.FINISHING
            if (ctx.command == ClientCardCommand.SEARCH) {
                ctx.clientCardsResponse = Page(items = ClientCardStub.getList(), totalSize = ClientCardStub.getList().size)
            } else {
                ctx.clientCardResponse = ClientCardStub.get().also { it.isArchived = ctx.command == ClientCardCommand.ARCHIVE }
            }
        }
    }
