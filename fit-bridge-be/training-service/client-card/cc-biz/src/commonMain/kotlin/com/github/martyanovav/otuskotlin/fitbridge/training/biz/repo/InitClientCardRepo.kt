package com.github.martyanovav.otuskotlin.fitbridge.training.biz.repo

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.biz.exceptions.FbDbNotConfiguredException
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.errorSystem
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.fail
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.IRepoClientCard

fun ICorChainDsl<ClientCardContext>.initClientCardRepo(title: String) =
    worker {
        this.title = title
        description = "Вычисление рабочего репозитория для карточек клиентов"
        handle {
            clientCardRepo =
                when {
                    workMode == WorkMode.TEST -> corSettings.repoClientCardTest
                    workMode == WorkMode.STUB -> corSettings.repoClientCardStub
                    else -> corSettings.repoClientCardProd
                }
            if (workMode != WorkMode.STUB && clientCardRepo == IRepoClientCard.NONE) {
                fail(
                    errorSystem(
                        violationCode = "dbNotConfigured",
                        e = FbDbNotConfiguredException(workMode),
                    ),
                )
            }
        }
    }
