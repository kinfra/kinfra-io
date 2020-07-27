package ru.kontur.kinfra.io

import ru.kontur.kinfra.io.impl.BufferInputStream
import ru.kontur.kinfra.io.impl.FileInputStream
import ru.kontur.kinfra.io.impl.NullInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Path

interface InputByteStream : ByteStream {

    /**
     * Reads from this stream into supplied [buffer].
     *
     * At most `buffer.remaining()` bytes will be transferred into the buffer.
     *
     * @return `false` if end of stream is reached **and** no bytes were read, `true` otherwise
     */
    suspend fun read(buffer: ByteBuffer): Boolean

    /**
     * Writes all bytes from this stream to an [output stream][output].
     *
     * @throws IOException if an I/O error occurs when reading from this stream or writing into the output stream
     */
    @JvmDefault
    suspend fun transferTo(output: OutputByteStream) {
        val buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        ByteStream.transfer(this, output, buffer)
    }

    companion object {

        private const val DEFAULT_BUFFER_SIZE = 8 * 1024

        /**
         * Returns a stream that reads data from a specified file.
         *
         * @param path path of the file
         * @param range range of file's bytes to read
         *
         * @throws IllegalArgumentException if the [range] is invalid
         * @throws IOException if the file cannot be opened due to I/O error
         */
        suspend fun fromFile(path: Path, range: LongRange? = null): InputByteStream {
            return FileInputStream.open(path, range)
        }

        /**
         * Returns a stream that reads remaining data of a [buffer].
         *
         * A [duplicate][ByteBuffer.duplicate] of the buffer is used,
         * so any operations on the returned stream do not affect the [buffer]'s position, limit or mark.
         *
         * Buffer's content **must not** be modified until the returned stream is closed.
         */
        fun fromBuffer(buffer: ByteBuffer): InputByteStream {
            return BufferInputStream(buffer.asReadOnlyBuffer())
        }

        /**
         * Return a stream that has no data to read.
         */
        fun nullStream(): InputByteStream {
            return NullInputStream()
        }

    }

}
