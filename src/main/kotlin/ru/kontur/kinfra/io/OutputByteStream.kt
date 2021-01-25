package ru.kontur.kinfra.io

import ru.kontur.kinfra.io.impl.BufferOutputStream
import ru.kontur.kinfra.io.impl.FileOutputStream
import ru.kontur.kinfra.io.impl.NullOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Path

/**
 * Sink of binary data.
 */
@JvmDefaultWithoutCompatibility
interface OutputByteStream : SuspendingCloseable {

    /**
     * Writes remaining contents of a [buffer] to this stream.
     *
     * Implementation should fill the entire buffer before return, but is not required to do so.
     *
     * @throws IOException if an I/O error occurs
     */
    suspend fun write(buffer: ByteBuffer)

    /**
     * Writes a [buffer] to this stream.
     *
     * Thereafter, the caller **must not** access the buffer or modify its content.
     * Implementation is free to store it internally and use it anytime,
     * but likewise **not allowed** to modify its content.
     *
     * This method generally can be implemented more efficiently than [write],
     * so it is preferred to use over the latter when suitable.
     *
     * @throws IOException if an I/O error occurs
     */
    suspend fun put(buffer: ByteBuffer) {
        while (buffer.hasRemaining()) {
            write(buffer)
        }
    }

    /**
     * Closes this stream.
     * No further actions on it are allowed.
     *
     * @throws java.io.IOException if an I/O error occurs during closing or flushing written data
     */
    override suspend fun close()

    companion object {

        /**
         * Runs a [block] of code that writes data to a stream, and returns a buffer containing that data.
         *
         * @param expectedSize expected amount of data to be collected (in bytes)
         */
        inline fun collectToBuffer(expectedSize: Int? = null, block: (stream: OutputByteStream) -> Unit): ByteBuffer {
            return BufferOutputStream.create(expectedSize).apply(block).toBuffer()
        }

        /**
         * Returns a stream that writes incoming data to a specified file.
         *
         * If the file does not exist, it will be created.
         *
         * @param path path of the file
         * @param append whether the data will be appended to the end of file or the file will be truncated
         * @throws IOException if the file cannot be opened due to I/O error
         */
        suspend fun intoFile(path: Path, append: Boolean = false): OutputByteStream {
            return FileOutputStream.open(path, append)
        }

        /**
         * Returns a stream that discards any data written to it.
         */
        fun nullStream(): OutputByteStream {
            return NullOutputStream()
        }

    }

}
