package ru.kontur.kinfra.io.impl

import ru.kontur.kinfra.io.ByteStream

internal abstract class AbstractByteStream : ByteStream {

    private var closed: Boolean = false

    protected fun checkOpened() {
        check(!closed) { "Stream closed" }
    }

    protected fun tryClose(): Boolean {
        return if (closed) {
            false
        } else {
            closed = true
            true
        }
    }

    override suspend fun close() {
        tryClose()
    }

}
