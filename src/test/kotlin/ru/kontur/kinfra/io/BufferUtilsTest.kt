package ru.kontur.kinfra.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.kontur.kinfra.commons.binary.byteArrayOfHex
import java.nio.ByteBuffer

class BufferUtilsTest {

    @Nested
    inner class WithRemainingAtMost {

        private val buffer = ByteBuffer.allocate(16).limit(8).position(4)

        @Test
        fun count_less_than_remaining() {
            buffer.withRemainingAtMost(count = 2) {
                assertEquals(2, it.remaining())
            }
        }

        @Test
        fun count_equal_to_remaining() {
            buffer.withRemainingAtMost(count = 4) {
                assertEquals(4, it.remaining())
            }
        }

        @Test
        fun count_greater_than_remaining() {
            buffer.withRemainingAtMost(count = 8) {
                assertEquals(4, it.remaining())
            }
        }

        @Test
        fun restore_normally() {
            buffer.withRemainingAtMost(count = 2) { }
            assertEquals(4, buffer.remaining())
        }

        @Test
        fun restore_exceptionally() {
            lateinit var expectedException: Throwable
            val actualException = assertThrows<Throwable> {
                buffer.withRemainingAtMost(count = 2) {
                    expectedException = Throwable("test")
                    throw expectedException
                }
            }
            assertEquals(expectedException, actualException)
            assertEquals(4, buffer.remaining())
        }

    }

    @Nested
    inner class TransferTo {

        private val sampleData = byteArrayOfHex("cafebabedeadbeef")

        @Test
        fun overflow() {
            val src = ByteBuffer.allocate(8)
                .put(sampleData)
                .flip()
            val dst = ByteBuffer.allocate(4)

            check(src.remaining() > dst.remaining())
            val count = src.transferTo(dst)

            assertEquals(4, count)
            assertEquals(count, src.position())
            assertEquals(count, dst.position())

            dst.flip()
            assertEqualBytes(dst, sampleData, count)
        }

        @Test
        fun underflow() {
            val src = ByteBuffer.allocate(8)
                .put(sampleData)
                .flip()
                .limit(4)
            val dst = ByteBuffer.allocate(16)

            check(dst.remaining() > src.remaining())
            val count = src.transferTo(dst)

            assertEquals(4, count)
            assertEquals(count, src.position())
            assertEquals(count, dst.position())

            dst.flip()
            assertEqualBytes(dst, sampleData, count)
        }

    }

}
