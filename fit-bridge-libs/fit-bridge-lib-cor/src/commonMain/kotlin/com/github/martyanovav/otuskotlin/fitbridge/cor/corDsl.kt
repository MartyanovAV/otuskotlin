package com.github.martyanovav.otuskotlin.fitbridge.cor

import com.github.martyanovav.otuskotlin.fitbridge.cor.handlers.CorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.handlers.CorWorkerDsl

@CorDslMarker
interface ICorExecDsl<T> {
    var title: String
    var description: String

    fun on(function: suspend T.() -> Boolean)
    fun except(function: suspend T.(Throwable) -> Unit)
    fun build(): ICorExec<T>
}

@CorDslMarker
interface ICorChainDsl<T> : ICorExecDsl<T> {
    fun add(worker: ICorExecDsl<T>)
}

@CorDslMarker
interface ICorWorkerDsl<T> : ICorExecDsl<T> {
    fun handle(function: suspend T.() -> Unit)
}

/**
 * DSL entry point for a chain whose elements execute sequentially.
 */
fun <T> rootChain(function: ICorChainDsl<T>.() -> Unit): ICorChainDsl<T> =
    CorChainDsl<T>().apply(function)

/** Creates a nested sequential chain. */
fun <T> ICorChainDsl<T>.chain(function: ICorChainDsl<T>.() -> Unit) {
    add(CorChainDsl<T>().apply(function))
}

/** Creates a worker. */
fun <T> ICorChainDsl<T>.worker(function: ICorWorkerDsl<T>.() -> Unit) {
    add(CorWorkerDsl<T>().apply(function))
}

/** Creates a worker with its metadata and handler. */
fun <T> ICorChainDsl<T>.worker(
    title: String,
    description: String = "",
    blockHandle: T.() -> Unit,
) {
    add(CorWorkerDsl<T>().also {
        it.title = title
        it.description = description
        it.handle(blockHandle)
    })
}
