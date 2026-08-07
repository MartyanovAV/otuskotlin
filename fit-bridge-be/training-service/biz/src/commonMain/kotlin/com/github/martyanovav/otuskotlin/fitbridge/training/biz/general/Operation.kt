package com.github.martyanovav.otuskotlin.fitbridge.training.biz.general

import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.chain

fun ICorChainDsl<IFBContext>.clientCardOperation(
    title: String,
    command: ClientCardCommand,
    block: ICorChainDsl<IFBContext>.() -> Unit
) = chain {
    block()
    this.title = title
    on { this.command == command && state == State.RUNNING && this is ClientCardContext }
}

fun ICorChainDsl<IFBContext>.trainingPlanOperation(
    title: String,
    command: TrainingPlanCommand,
    block: ICorChainDsl<IFBContext>.() -> Unit
) = chain {
    block()
    this.title = title
    on { this.command == command && state == State.RUNNING && this is TrainingPlanContext }
}
