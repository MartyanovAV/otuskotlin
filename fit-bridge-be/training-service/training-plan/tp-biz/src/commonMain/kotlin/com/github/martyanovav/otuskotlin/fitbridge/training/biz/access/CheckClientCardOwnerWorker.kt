package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.asFBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardLock
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.permissions.FtcPrincipalRelation
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardIdRequest
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseErr
import com.github.martyanovav.otuskotlin.fitbridge.training.common.repo.DbClientCardResponseOk

fun ICorChainDsl<TrainingPlanContext>.checkClientCardOwner(title: String) =
    worker {
        this.title = title
        description = "Проверка того, что план тренировок привязывается к карточке клиента, принадлежащей пользователю"
        on { state == State.RUNNING && command == TrainingPlanCommand.CREATE }
        handle {
            val ctx = this@handle
            val response =
                ctx.clientCardRepo.readClientCard(
                    DbClientCardIdRequest(ctx.trainingPlanValidating.clientCardId, ClientCardLock.NONE),
                )
            when (response) {
                is DbClientCardResponseOk -> {
                    val clientCard = response.data
                    if (clientCard.ownerUserId != ctx.principal.userId) {
                        ctx.state = State.FAILING
                        ctx.addError(
                            RuntimeException("Access Denied").asFBError(
                                code = "access-denied-client-card",
                                group = "security",
                                message = "У вас нет прав для создания плана к данной карточке клиента"
                            )
                        )
                    } else {
                        ctx.principalRelation = FtcPrincipalRelation.OWNER
                    }
                }
                else -> {
                    ctx.state = State.FAILING
                    if (response is DbClientCardResponseErr) {
                        response.errors.forEach { ctx.addError(it) }
                    } else {
                        ctx.addError(
                            RuntimeException("Unknown DB Response").asFBError(
                                code = "db-unknown-response",
                                group = "repo",
                                message = "Неизвестный ответ БД"
                            )
                        )
                    }
                }
            }
        }
    }
