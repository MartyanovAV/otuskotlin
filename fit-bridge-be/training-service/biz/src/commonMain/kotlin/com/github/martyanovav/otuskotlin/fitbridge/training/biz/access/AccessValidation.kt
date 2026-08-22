package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.asFBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.permissions.FtcPrincipalRelation

fun ICorChainDsl<IFBContext>.accessValidationClientCard(title: String) =
    chain {
        this.title = title
        description = "Валидация прав доступа для карточки клиента"

        on { state == State.RUNNING }

        worker("Проверка прав") {
            val ctx = this as ClientCardContext
            val hasAccess =
                when (ctx.command) {
                    ClientCardCommand.CREATE ->
                        ctx.principal.isTrainer() && ctx.principalRelation == FtcPrincipalRelation.NEW
                    ClientCardCommand.READ ->
                        ctx.principal.isTrainer() && ctx.principalRelation == FtcPrincipalRelation.OWNER
                    ClientCardCommand.UPDATE ->
                        ctx.principal.isTrainer() && ctx.principalRelation == FtcPrincipalRelation.OWNER
                    ClientCardCommand.ARCHIVE ->
                        ctx.principal.isTrainer() && ctx.principalRelation == FtcPrincipalRelation.OWNER
                    ClientCardCommand.SEARCH -> ctx.principal.isTrainer() && ctx.principal.userId.isNotBlank()
                    else -> false
                }

            if (!hasAccess) {
                ctx.state = State.FAILING
                ctx.addError(
                    RuntimeException("Access Denied").asFBError(
                        code = "access-denied",
                        group = "security",
                        message = "У вас нет прав для выполнения этой операции"
                    )
                )
            }
        }
    }

fun ICorChainDsl<IFBContext>.accessValidationTrainingPlan(title: String) =
    chain {
        this.title = title
        description = "Валидация прав доступа для плана тренировок"

        on { state == State.RUNNING }

        worker("Проверка прав") {
            val ctx = this as TrainingPlanContext
            val hasAccess =
                when (ctx.command) {
                    TrainingPlanCommand.CREATE ->
                        ctx.principal.isTrainer() &&
                            (
                                ctx.principalRelation == FtcPrincipalRelation.OWNER ||
                                    ctx.principalRelation == FtcPrincipalRelation.NEW
                            )
                    TrainingPlanCommand.READ ->
                        ctx.principal.isTrainer() && ctx.principalRelation == FtcPrincipalRelation.OWNER
                    TrainingPlanCommand.UPDATE ->
                        ctx.principal.isTrainer() && ctx.principalRelation == FtcPrincipalRelation.OWNER
                    TrainingPlanCommand.ARCHIVE ->
                        ctx.principal.isTrainer() && ctx.principalRelation == FtcPrincipalRelation.OWNER
                    TrainingPlanCommand.COMPLETE ->
                        ctx.principal.isTrainer() && ctx.principalRelation == FtcPrincipalRelation.OWNER
                    TrainingPlanCommand.SEARCH -> ctx.principal.isTrainer() && ctx.principal.userId.isNotBlank()
                    else -> false
                }

            if (!hasAccess) {
                ctx.state = State.FAILING
                ctx.addError(
                    RuntimeException("Access Denied").asFBError(
                        code = "access-denied",
                        group = "security",
                        message = "У вас нет прав для выполнения этой операции"
                    )
                )
            }
        }
    }
