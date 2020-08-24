package ru.kontur.kinfra.io.utils

import java.nio.ByteBuffer

/**
 * Transfers bytes remaining in this buffer to another buffer.
 *
 * Number of transferred bytes is `minOf(this.remaining(), dst.remaining())`.
 *
 * An invocation of this method of the form `src.transferTo(dst)` has exactly the same effect as the loop:
 *
 * ```
 * while (src.hasRemaining() && dst.hasRemaining()) {
 *     dst.put(src.get())
 * }
 * ```
 *
 * @return number of transferred bytes
 */
fun ByteBuffer.transferTo(dst: ByteBuffer): Int {
    val startPosition = position()
    withRemainingAtMost(dst.remaining()) {
        dst.put(it)
    }
    return position() - startPosition
}

/**
 * Executes a given [block] of code while limit of this buffer is set to the following value:
 *
 * `min(limit(), position() + count)`
 *
 * That is, at most [count] bytes are [remaining][ByteBuffer.remaining] in the buffer.
 *
 * Before return from this method the limit is restored.
 */
inline fun <R> ByteBuffer.withRemainingAtMost(count: Int, block: (ByteBuffer) -> R): R {
    val oldLimit = limit()
    return try {
        limit(minOf(oldLimit, position() + count))
        block(this)
    } finally {
        limit(oldLimit)
    }
}
