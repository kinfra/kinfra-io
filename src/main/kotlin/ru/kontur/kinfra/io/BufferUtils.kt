package ru.kontur.kinfra.io

import ru.kontur.kinfra.io.utils.transferTo
import ru.kontur.kinfra.io.utils.withRemainingAtMost
import java.nio.ByteBuffer

// todo: hide/remove before next release

@Deprecated(
    "Moved to utils package",
    replaceWith = ReplaceWith(
        "transferTo(dst)",
        "ru.kontur.kinfra.io.utils.transferTo"
    ),
    level = DeprecationLevel.ERROR
)
@Suppress("DEPRECATION_ERROR")
fun ByteBuffer.transferTo(dst: ByteBuffer): Int {
    return transferTo(dst)
}

@Deprecated(
    "Moved to utils package",
    replaceWith = ReplaceWith(
        "withRemainingAtMost(count, block)",
        "ru.kontur.kinfra.io.utils.withRemainingAtMost"
    ),
    level = DeprecationLevel.ERROR
)
inline fun <R> ByteBuffer.withRemainingAtMost(count: Int, block: (ByteBuffer) -> R): R {
    return withRemainingAtMost(count, block)
}
