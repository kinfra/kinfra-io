package ru.kontur.kinfra.io.streams

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kontur.kinfra.io.OutputByteStream
import ru.kontur.kinfra.io.test.byteBufferOfHex
import java.nio.ByteBuffer

class OutputByteStreamTest {

    @Test
    fun default_put_writes_until_end() {
        val testBuffer = byteBufferOfHex("007f80ff")
        val expectedData = testBuffer.duplicate()
        val stream = object : OutputByteStream {
            override suspend fun write(buffer: ByteBuffer) {
                // get 1 byte from the buffer each time
                assertEquals(expectedData.get(), buffer.get())
            }

            override suspend fun close() = Unit
        }
        runBlocking {
            stream.put(testBuffer)
            assertEquals(0, testBuffer.remaining())
            assertEquals(0, expectedData.remaining())
        }
    }

}
