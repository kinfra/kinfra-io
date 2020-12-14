package ru.kontur.kinfra.io.utils

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * An output stream similar to [ByteArrayOutputStream] that
 * can return written data as a [ByteBuffer] without additional copying
 * (unlike [ByteArrayOutputStream.toByteArray]).
 *
 * Usage:
 * ```
 * val buffer: ByteBuffer = ByteBufferOutputStream.collect { stream ->
 *     stream.write(...)
 *     stream.write(...)
 * }
 * ```
 */
class ByteBufferOutputStream : ByteArrayOutputStream {

    private constructor() : super()
    private constructor(size: Int) : super(size)

    @PublishedApi
    internal fun toBuffer(): ByteBuffer {
        return ByteBuffer.wrap(buf, 0, count)
    }

    companion object {

        /**
         * Runs a [block] of code that writes data to a stream, and returns a buffer containing that data.
         *
         * @param expectedSize expected amount of data to be collected (in bytes)
         */
        inline fun collect(expectedSize: Int? = null, block: (OutputStream) -> Unit): ByteBuffer {
            return create(expectedSize).apply(block).toBuffer()
        }

        @PublishedApi
        internal fun create(expectedSize: Int?): ByteBufferOutputStream {
            return if (expectedSize != null) {
                ByteBufferOutputStream(expectedSize)
            } else {
                ByteBufferOutputStream()
            }
        }

    }

}
