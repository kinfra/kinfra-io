package ru.kontur.kinfra.io.impl

import ru.kontur.kinfra.io.InputByteStream
import ru.kontur.kinfra.io.OutputByteStream
import ru.kontur.kinfra.io.utils.transferTo
import java.nio.ByteBuffer

@PublishedApi
internal class BufferOutputStream private constructor(expectedSize: Int?) : AbstractCloseable(), OutputByteStream {

    private var data = ByteArray(expectedSize ?: DEFAULT_INITIAL_SIZE)
    private var offset = 0

    override suspend fun write(buffer: ByteBuffer) {
        val count = buffer.remaining()
        write(count) {
            buffer.get(data, offset, count)
        }
    }

    private inline fun write(count: Int, block: () -> Unit) {
        checkOpened()
        ensureCapacity(count)
        block()
        offset += count
    }

    private fun ensureCapacity(additionalSize: Int) {
        val capacity = data.size
        val minCapacity = Math.addExact(offset, additionalSize)
        if (capacity < minCapacity) {
            data = data.copyOf(maxOf(capacity * 2, minCapacity))
        }
    }

    fun toBuffer(): ByteBuffer {
        checkOpened()
        closed = true
        return ByteBuffer.wrap(data, 0, offset)
    }

    companion object {

        private const val DEFAULT_INITIAL_SIZE = 1 * 1024

        fun create(expectedSize: Int?): BufferOutputStream {
            return BufferOutputStream(expectedSize)
        }

    }

}

internal class BufferInputStream(private val data: ByteBuffer) : AbstractCloseable(), InputByteStream {

    init {
        require(data.isReadOnly) { "Buffer must be read-only" }
    }

    override suspend fun read(buffer: ByteBuffer): Boolean {
        checkOpened()
        val count = data.transferTo(buffer)
        return count > 0 || data.hasRemaining()
    }

    override suspend fun transferTo(output: OutputByteStream): Long {
        checkOpened()
        val totalCount = data.remaining().toLong()
        output.put(data.duplicate())
        data.position(data.limit())
        return totalCount
    }

}

