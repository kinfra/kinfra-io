package ru.kontur.kinfra.io.impl

import ru.kontur.kinfra.io.ByteStream
import ru.kontur.kinfra.io.InputByteStream
import ru.kontur.kinfra.io.OutputByteStream
import ru.kontur.kinfra.io.utils.transferTo
import java.nio.ByteBuffer

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

internal class NullOutputStream : AbstractByteStream(), OutputByteStream {

    override suspend fun write(buffer: ByteBuffer) {
        checkOpened()
        // skip buffer's remaining data
        buffer.position(buffer.limit())
    }

    override suspend fun put(buffer: ByteBuffer) {
        checkOpened()
    }

}

internal class NullInputStream : AbstractByteStream(), InputByteStream {

    override suspend fun read(buffer: ByteBuffer): Boolean {
        checkOpened()
        return false
    }

    override suspend fun transferTo(output: OutputByteStream) {
        checkOpened()
    }

}

@PublishedApi
internal class BufferOutputStream private constructor(expectedSize: Int?) : AbstractByteStream(), OutputByteStream {

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
        check(tryClose()) { "Stream closed" }
        return ByteBuffer.wrap(data, 0, offset)
    }

    override suspend fun close() {
        error("This stream should not be closed via this method")
    }

    companion object {

        private const val DEFAULT_INITIAL_SIZE = 1 * 1024

        fun create(expectedSize: Int?): BufferOutputStream {
            return BufferOutputStream(expectedSize)
        }

    }

}

internal class BufferInputStream(private val data: ByteBuffer) : AbstractByteStream(), InputByteStream {

    init {
        require(data.isReadOnly) { "Buffer must be read-only" }
    }

    override suspend fun read(buffer: ByteBuffer): Boolean {
        checkOpened()
        val count = data.transferTo(buffer)
        return count > 0 || data.hasRemaining()
    }

    override suspend fun transferTo(output: OutputByteStream) {
        checkOpened()
        while (data.hasRemaining()) {
            output.write(data)
        }
    }

}

