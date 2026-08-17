package com.github.martyanovav.otuskotlin.fitbridge.training.biz

import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.CorSettings
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.Page
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.WorkMode
import com.github.martyanovav.otuskotlin.fitbridge.training.common.stubs.Stubs
import com.github.martyanovav.otuskotlin.fitbridge.training.stubs.ClientCardStub
import com.github.martyanovav.otuskotlin.fitbridge.training.stubs.TrainingPlanStub

class TrainingProcessor(
    @Suppress("unused") private val corSettings: CorSettings,
) {
    suspend fun exec(ctx: IFBContext) {
        when (ctx.command) {
            FBCommandBase.INIT -> ctx.state = State.RUNNING
            FBCommandBase.FINISH -> ctx.state = State.FINISHING
            else ->
                when (ctx) {
                    is ClientCardContext -> ctx.processClientCard()
                    is TrainingPlanContext -> ctx.processTrainingPlan()
                    else -> ctx.fail("unsupported-context", "Unsupported training context")
                }
        }
    }

    private fun ClientCardContext.processClientCard() {
        if (command !in ClientCardCommand.entries || command == ClientCardCommand.NONE) {
            fail("unsupported-command", "Unsupported client card command")
            return
        }
        if (!stubMode()) return

        when (stubCase) {
            Stubs.SUCCESS -> {
                if (command == ClientCardCommand.SEARCH) {
                    clientCardsResponse = Page(items = ClientCardStub.getList(), totalSize = ClientCardStub.getList().size)
                } else {
                    clientCardResponse = ClientCardStub.get().also { it.isArchived = command == ClientCardCommand.ARCHIVE }
                }
                state = State.RUNNING
            }
            Stubs.NOT_FOUND -> fail("not-found", "Client card was not found")
            Stubs.BAD_ID -> fail("bad-id", "Client card id is invalid", "id")
            Stubs.CANNOT_ARCHIVE -> fail("cannot-archive", "Client card cannot be archived")
            Stubs.BAD_PLAN_TITLE -> fail("unsupported-stub", "This stub belongs to training plans")
            Stubs.NONE -> fail("stub-not-configured", "Select a stub case in debug settings")
        }
    }

    private fun TrainingPlanContext.processTrainingPlan() {
        if (command !in TrainingPlanCommand.entries || command == TrainingPlanCommand.NONE) {
            fail("unsupported-command", "Unsupported training plan command")
            return
        }
        if (!stubMode()) return

        when (stubCase) {
            Stubs.SUCCESS -> {
                if (command == TrainingPlanCommand.SEARCH) {
                    trainingPlansResponse = Page(items = TrainingPlanStub.getList(), totalSize = TrainingPlanStub.getList().size)
                } else {
                    trainingPlanResponse = TrainingPlanStub.get().also { it.isArchived = command == TrainingPlanCommand.ARCHIVE }
                }
                state = State.RUNNING
            }
            Stubs.NOT_FOUND -> fail("not-found", "Training plan was not found")
            Stubs.BAD_ID -> fail("bad-id", "Training plan id is invalid", "id")
            Stubs.BAD_PLAN_TITLE -> fail("bad-plan-title", "Training plan title is invalid", "title")
            Stubs.CANNOT_ARCHIVE -> fail("cannot-archive", "Training plan cannot be archived")
            Stubs.NONE -> fail("stub-not-configured", "Select a stub case in debug settings")
        }
    }

    private fun IFBContext.stubMode(): Boolean {
        if (workMode == WorkMode.STUB) return true
        fail("not-implemented", "Training business logic is not implemented yet")
        return false
    }

    private fun IFBContext.fail(
        code: String,
        message: String,
        field: String = "",
    ) {
        state = State.FAILING
        addError(FBError(code = code, group = "business", field = field, message = message))
    }
}
