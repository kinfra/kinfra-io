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
interface OutputByteStream : ByteStream {

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
     * The buffer **must not** be accessed by the caller thereafter.
     * Implementation is free to store it internally and use it at any time,
     * but **must not** modify its contents.
     *
     * @throws IOException if an I/O error occurs
     */
    suspend fun put(buffer: ByteBuffer) {
        write(buffer)
    }

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
