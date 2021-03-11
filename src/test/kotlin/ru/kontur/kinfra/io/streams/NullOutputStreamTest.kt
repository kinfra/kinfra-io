package ru.kontur.kinfra.io.streams

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kontur.kinfra.io.OutputByteStream
import java.nio.ByteBuffer

class NullOutputStreamTest {

    @Test
    fun write_discards_buffer_content() {
        val stream = OutputByteStream.nullStream()
        val buffer = ByteBuffer.allocate(1024)
        runBlocking {
            stream.write(buffer)
        }
        assertEquals(0, buffer.remaining())
    }

    @Test
    fun put_returns_normally() {
        val stream = OutputByteStream.nullStream()
        val buffer = ByteBuffer.allocate(1024)
        runBlocking {
            stream.put(buffer)
        }
    }

}
