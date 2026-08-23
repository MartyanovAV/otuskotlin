package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.asFBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.permissions.FtcPrincipalRelation

fun ICorChainDsl<ClientCardContext>.accessValidationClientCard(title: String) =
    chain {
        this.title = title
        description = "Проверка прав доступа к анкете"

        on { state == State.RUNNING }

        worker("Проверка прав") {
            val hasAccess =
                when (command) {
                    ClientCardCommand.CREATE ->
                        principal.isTrainer() && principalRelation == FtcPrincipalRelation.NEW
                    ClientCardCommand.READ ->
                        principal.isTrainer() && principalRelation == FtcPrincipalRelation.OWNER
                    ClientCardCommand.UPDATE ->
                        principal.isTrainer() && principalRelation == FtcPrincipalRelation.OWNER
                    ClientCardCommand.ARCHIVE ->
                        principal.isTrainer() && principalRelation == FtcPrincipalRelation.OWNER
                    ClientCardCommand.SEARCH -> principal.isTrainer() && principal.userId.isNotBlank()
                    else -> false
                }

            if (!hasAccess) {
                state = State.FAILING
                addError(
                    RuntimeException("Access Denied").asFBError(
                        code = "access-denied",
                        group = "security",
                        message = "Нет прав на выполнение этой операции"
                    )
                )
            }
        }
    }
