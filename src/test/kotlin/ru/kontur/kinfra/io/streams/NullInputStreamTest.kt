package ru.kontur.kinfra.io.streams

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import ru.kontur.kinfra.io.InputByteStream
import ru.kontur.kinfra.io.OutputByteStream
import java.nio.ByteBuffer

class NullInputStreamTest {

    @Test
    fun read_returns_false() {
        val stream = InputByteStream.nullStream()
        val buffer = ByteBuffer.allocate(1)
        val result = runBlocking {
            stream.read(buffer)
        }
        assertFalse(result)
        assertEquals(0, buffer.position())
    }

    @Test
    fun transferTo_does_nothing() {
        val stream = InputByteStream.nullStream()
        val count = runBlocking {
            stream.transferTo(UntouchableOutputStream)
        }
        assertEquals(0, count)
    }

    object UntouchableOutputStream : OutputByteStream {
        override suspend fun write(buffer: ByteBuffer) {
            fail("should not be called")
        }

        override suspend fun close() {
            fail("should not be called")
        }

    }

}
