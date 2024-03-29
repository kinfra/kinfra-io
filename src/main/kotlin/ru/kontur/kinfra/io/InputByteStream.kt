package ru.kontur.kinfra.io

import ru.kontur.kinfra.io.impl.BufferInputStream
import ru.kontur.kinfra.io.impl.FileInputStream
import ru.kontur.kinfra.io.impl.NullInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Path

/**
 * Source of binary data.
 */
@JvmDefaultWithoutCompatibility
interface InputByteStream : SuspendingCloseable {

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
     * @return number of transferred bytes
     */
    suspend fun transferTo(output: OutputByteStream): Long

    /**
     * Closes this stream.
     * No further actions on it are allowed.
     *
     * @throws java.io.IOException if an I/O error occurs during closing
     */
    override suspend fun close()

    companion object {

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

        /**
         * Transfer all data from an [input stream][input] to an [output stream][output].
         *
         * The operation is performed using [InputByteStream.read] and [OutputByteStream.write] on a supplied [buffer].
         */
        internal suspend fun transfer(input: InputByteStream, output: OutputByteStream, buffer: ByteBuffer): Long {
            var totalCount = 0L
            while (input.read(buffer) || buffer.position() > 0) {
                buffer.flip()
                output.write(buffer)
                totalCount += buffer.position()
                buffer.compact()
            }
            return totalCount
        }

    }

}
