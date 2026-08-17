package com.github.martyanovav.otuskotlin.fitbridge.cor.handlers

import com.github.martyanovav.otuskotlin.fitbridge.cor.CorDslMarker
import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorChainDsl
import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorExec
import com.github.martyanovav.otuskotlin.fitbridge.cor.ICorExecDsl

/** A chain that executes its children sequentially. */
class CorChain<T>(
    private val execs: List<ICorExec<T>>,
    title: String,
    description: String = "",
    blockOn: suspend T.() -> Boolean = { true },
    blockExcept: suspend T.(Throwable) -> Unit = {},
) : AbstractCorExec<T>(title, description, blockOn, blockExcept) {
    override suspend fun handle(context: T) {
        execs.forEach { it.exec(context) }
    }
}

@CorDslMarker
class CorChainDsl<T> : CorExecDsl<T>(), ICorChainDsl<T> {
    private val workers = mutableListOf<ICorExecDsl<T>>()

    override fun add(worker: ICorExecDsl<T>) {
        workers.add(worker)
    }

    override fun build(): ICorExec<T> =
        CorChain(
            title = title,
            description = description,
            execs = workers.map { it.build() },
            blockOn = blockOn,
            blockExcept = blockExcept,
        )
}
