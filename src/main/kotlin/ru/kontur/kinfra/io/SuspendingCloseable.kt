package ru.kontur.kinfra.io

import kotlinx.coroutines.CancellationException

interface SuspendingCloseable {

    /**
     * Releases resources associated with this object.
     *
     * Implementation should take in account that the current coroutine may be cancelled.
     */
    suspend fun close()

}

suspend fun <T : SuspendingCloseable, R> T.use(block: suspend (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        try {
            close()
        } catch (e: CancellationException) {
            // just ignore, no need to collect it
        } catch (e: Throwable) {
            if (exception != null) {
                exception.addSuppressed(e)
            } else {
                throw e
            }
        }
    }
}

suspend fun Collection<SuspendingCloseable>.closeAll() {
    var exception: Throwable? = null

    for (closeable in this@closeAll) {
        try {
            closeable.close()
        } catch (e: CancellationException) {
            // just ignore, no need to collect it
        } catch (e: Throwable) {
            if (exception == null) {
                exception = e
            } else {
                exception.addSuppressed(e)
            }
        }
    }

    if (exception != null) {
        throw exception
    }
}
