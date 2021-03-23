package ru.kontur.kinfra.io.impl

import ru.kontur.kinfra.io.SuspendingCloseable

internal abstract class AbstractCloseable : SuspendingCloseable {

    protected var closed: Boolean = false

    protected fun checkOpened() {
        check(!closed) { "Stream closed" }
    }

    final override suspend fun close() {
        if (closed) return
        closed = true
        closeImpl()
    }

    protected open suspend fun closeImpl() = Unit

}
