package com.github.martyanovav.otuskotlin.fitbridge.training.biz.access

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.chain
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers.asFBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.permissions.FtcPrincipalRelation

fun ICorChainDsl<TrainingPlanContext>.accessValidationTrainingPlan(title: String) =
    chain {
        this.title = title
        description = "Проверка прав доступа к тренировкам"

        on { state == State.RUNNING }

        worker("Проверка прав") {
            val hasAccess =
                when (command) {
                    TrainingPlanCommand.CREATE ->
                        principal.isTrainer() &&
                            (
                                principalRelation == FtcPrincipalRelation.OWNER ||
                                    principalRelation == FtcPrincipalRelation.NEW
                            )
                    TrainingPlanCommand.READ ->
                        principal.isTrainer() && principalRelation == FtcPrincipalRelation.OWNER
                    TrainingPlanCommand.UPDATE ->
                        principal.isTrainer() && principalRelation == FtcPrincipalRelation.OWNER
                    TrainingPlanCommand.ARCHIVE ->
                        principal.isTrainer() && principalRelation == FtcPrincipalRelation.OWNER
                    TrainingPlanCommand.COMPLETE ->
                        principal.isTrainer() && principalRelation == FtcPrincipalRelation.OWNER
                    TrainingPlanCommand.SEARCH -> principal.isTrainer() && principal.userId.isNotBlank()
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
