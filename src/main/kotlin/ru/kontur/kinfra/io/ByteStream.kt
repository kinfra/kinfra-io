package ru.kontur.kinfra.io

import java.nio.ByteBuffer

/**
 * Stream of binary data.
 *
 * @see InputByteStream
 * @see OutputByteStream
 */
interface ByteStream : SuspendingCloseable {

    /**
     * Closes this stream.
     * No further actions on it are allowed.
     *
     * @throws java.io.IOException if an I/O error occurs during closing or after the last operation on the stream
     */
    override suspend fun close()

    companion object {

        internal suspend fun transfer(input: InputByteStream, output: OutputByteStream, buffer: ByteBuffer) {
            while (input.read(buffer)) {
                buffer.flip()
                output.write(buffer)
                buffer.compact()
            }
        }

    }

}