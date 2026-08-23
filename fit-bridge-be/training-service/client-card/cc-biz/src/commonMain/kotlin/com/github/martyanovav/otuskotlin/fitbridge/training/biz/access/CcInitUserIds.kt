package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State

fun ICorChainDsl<ClientCardContext>.initClientCardUserIds(title: String) =
    worker {
        this.title = title
        description = "Инициализация владельцев"
        on { state == State.RUNNING && command == ClientCardCommand.CREATE }
        handle {
            clientCardValidated.ownerUserId = principal.userId
            clientCardValidated.createdByUserId = principal.userId
        }
    }
