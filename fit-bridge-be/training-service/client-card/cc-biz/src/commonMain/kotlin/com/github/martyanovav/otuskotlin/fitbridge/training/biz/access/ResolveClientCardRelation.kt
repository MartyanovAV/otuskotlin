package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.permissions.FtcPrincipalRelation

fun ICorChainDsl<ClientCardContext>.resolveClientCardRelation(title: String) =
    worker {
        this.title = title
        description = "Вычисление отношения пользователя к карточке клиента"
        on { principal.userId.isNotBlank() }
        handle {
            principalRelation =
                when {
                    command == ClientCardCommand.CREATE -> FtcPrincipalRelation.NEW
                    clientCardRepoRead.ownerUserId == principal.userId -> FtcPrincipalRelation.OWNER
                    else -> FtcPrincipalRelation.NONE
                }
        }
    }
