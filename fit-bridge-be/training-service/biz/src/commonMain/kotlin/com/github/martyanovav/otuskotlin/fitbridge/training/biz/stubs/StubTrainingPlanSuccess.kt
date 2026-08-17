package com.github.martyanovav.otuskotlin.fitbridge.training.biz.stubs

import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.worker
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanStatus
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.stubs.TrainingPlanStub

fun ICorChainDsl<IFBContext>.stubTrainingPlanSuccess(title: String) =
    worker {
        this.title = title
        this.description =
            """
            Кейс успеха для тренировочного плана
            """.trimIndent()
        on { stubCase == Stubs.SUCCESS && state == State.RUNNING && this is TrainingPlanContext }
        handle {
            val ctx = this as TrainingPlanContext
            ctx.state = State.FINISHING
            if (ctx.command == TrainingPlanCommand.SEARCH) {
                ctx.trainingPlansResponse = Page(items = TrainingPlanStub.getList(), totalSize = TrainingPlanStub.getList().size)
            } else {
                ctx.trainingPlanResponse =
                    TrainingPlanStub.get().also {
                        it.status =
                            when (ctx.command) {
                                TrainingPlanCommand.ARCHIVE -> TrainingPlanStatus.ARCHIVED
                                else -> it.status
                            }
                    }
            }
        }
    }
