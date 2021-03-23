package ru.kontur.kinfra.io.impl

import ru.kontur.kinfra.io.InputByteStream
import ru.kontur.kinfra.io.OutputByteStream
import java.nio.ByteBuffer

internal class NullOutputStream : AbstractCloseable(), OutputByteStream {

    override suspend fun write(buffer: ByteBuffer) {
        checkOpened()
        // skip buffer's remaining data
        buffer.position(buffer.limit())
    }

    override suspend fun put(buffer: ByteBuffer) {
        checkOpened()
    }

}

internal class NullInputStream : AbstractCloseable(), InputByteStream {

    override suspend fun read(buffer: ByteBuffer): Boolean {
        checkOpened()
        return false
    }

    override suspend fun transferTo(output: OutputByteStream): Long {
        checkOpened()
        return 0
    }

}
