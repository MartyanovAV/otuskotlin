package com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.stubs.ClientCardStub

fun ICorChainDsl<ClientCardContext>.stubClientCardSuccess(title: String) =
    worker {
        this.title = title
        this.description =
            """
            Кейс успеха для карточки клиента
            """.trimIndent()
        on { stubCase == Stubs.SUCCESS && state == State.RUNNING }
        handle {
            state = State.FINISHING
            if (command == ClientCardCommand.SEARCH) {
                clientCardsResponse = Page(items = ClientCardStub.getList(), totalSize = ClientCardStub.getList().size)
            } else {
                clientCardResponse = ClientCardStub.get().also { it.isArchived = command == ClientCardCommand.ARCHIVE }
            }
        }
    }
