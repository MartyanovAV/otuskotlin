package com.github.martyanovav.otuskotlin.fitbridge.training.common.helpers

import com.github.martyanovav.otuskotlin.fitbridge.training.common.IFBContext
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.FBError
import com.github.martyanovav.otuskotlin.fitbridge.training.common.models.State
import kotlin.reflect.KClass

fun Throwable.asFBError(
    code: String = "unknown",
    group: String = "exceptions",
    message: String = this.message.orEmpty(),
) = FBError(
    code = code,
    group = group,
    field = "",
    message = message,
    exception = this,
)

suspend inline fun <C : IFBContext, T> executePipeline(
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
        try {
            ctx.respond()
        } catch (respondError: Throwable) {
            respondError.addSuppressed(e)
            throw respondError
        }
    }
}
