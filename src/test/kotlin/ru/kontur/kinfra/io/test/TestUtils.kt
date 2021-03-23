package ru.kontur.kinfra.io.test

import org.junit.jupiter.api.Assertions.assertArrayEquals
import ru.kontur.kinfra.commons.binary.byteArrayOfHex
import java.nio.ByteBuffer

internal fun assertEqualBytes(buffer: ByteBuffer, expected: ByteArray, count: Int) {
    val initialPosition = buffer.position()
    try {
        val expectedRange = expected.copyOf(count)
        val actual = ByteArray(count).also {
            buffer.get(it)
        }
        assertArrayEquals(expectedRange, actual)
    } finally {
        buffer.position(initialPosition)
    }
}

internal fun byteBufferOfHex(hex: String) = ByteBuffer.wrap(byteArrayOfHex(hex))
