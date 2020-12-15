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
 * Transfers data remaining in this buffer to a newly created byte array.
 *
 * @return the array containing remaining data of the buffer
 */
fun ByteBuffer.collectToArray(): ByteArray {
    return ByteArray(remaining()).also { get(it) }
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
    val limit = coerceRemainingAtMost(count)
    return withLimit(limit, block)
}

@PublishedApi
internal fun ByteBuffer.coerceRemainingAtMost(count: Int) = minOf(limit(), position() + count)

/**
 * Executes a given [block] of code while limit of this buffer is set to the [specified value][limit].
 *
 * Before return from this method the limit is restored.
 */
inline fun <R> ByteBuffer.withLimit(limit: Int, block: (ByteBuffer) -> R): R {
    val oldLimit = limit()
    return try {
        limit(limit)
        block(this)
    } finally {
        limit(oldLimit)
    }
}
