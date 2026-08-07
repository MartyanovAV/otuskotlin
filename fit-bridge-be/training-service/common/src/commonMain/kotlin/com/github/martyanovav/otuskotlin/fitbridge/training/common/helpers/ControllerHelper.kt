package com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers

import com.github.martyanovav.otuskotlin.fitbridge.training.common.ClientCardContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.TrainingPlanContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.ClientCardCommand
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBCommandBase
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.TrainingPlanCommand
import kotlin.reflect.KClass

fun Throwable.asFBError(
    code: String = "unknown",
    group: String = "exceptions",
    message: String = this.message ?: "",
) = FBError(
    code = code,
    group = group,
    field = "",
    message = message,
    exception = this,
)

suspend inline fun <C : IFBContext, T> ControllerHelper(
    crossinline getContext: () -> C,
    @Suppress("UNUSED_PARAMETER") clazz: KClass<*>,
    crossinline receive: suspend C.() -> Unit,
    crossinline exec: suspend C.() -> Unit,
    crossinline respond: suspend C.() -> T,
    crossinline toLog: (C) -> Any,
): T {
    val ctx = getContext()
    return try {
        ctx.receive()
        ctx.exec()
        ctx.respond()
    } catch (e: Throwable) {
        ctx.state = State.FAILING
        ctx.addError(e.asFBError())
        if (ctx.command == FBCommandBase.NONE) {
            when (ctx) {
                is ClientCardContext -> ctx.command = ClientCardCommand.READ
                is TrainingPlanContext -> ctx.command = TrainingPlanCommand.READ
            }
        }
        ctx.respond()
    }
}
