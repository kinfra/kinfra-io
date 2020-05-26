package ru.kontur.kinfra.io.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ru.kontur.kinfra.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption

private const val BLOCK_SIZE = 4096

internal abstract class FileByteStream(
    protected val channel: FileChannel
) : AbstractByteStream() {

    final override suspend fun close() {
        if (!tryClose()) return
        withContext(Dispatchers.IO + NonCancellable) {
            @Suppress("BlockingMethodInNonBlockingContext")
            channel.close()
        }
    }

}

internal class FileInputStream private constructor(
    channel: FileChannel,
    private val path: Path,
    private val range: LongRange?,
    private val endPosition: Long?
) : FileByteStream(channel), InputByteStream {

    override suspend fun read(buffer: ByteBuffer): Boolean {
        checkOpened()
        return withContext(Dispatchers.IO) {
            if (endPosition == null) {
                tryRead(buffer)
            } else {
                val position = channel.position()
                check(endPosition <= position) { "Invariant violated: position $position > $endPosition" }
                val leftToRead = (endPosition - position).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                if (leftToRead == 0) {
                    false
                } else {
                    buffer.withRemainingAtMost(leftToRead) {
                        tryRead(it)
                    }
                }
            }
        }
    }

    private fun tryRead(dst: ByteBuffer): Boolean {
        val count = channel.read(dst)
        return count != -1
    }

    override suspend fun transferTo(output: OutputByteStream) {
        if (output is FileOutputStream) {
            // todo: implement more efficient copy via Channel's transferTo()
        }

        // Direct buffers are preferred for file I/O
        val buffer = ByteBuffer.allocateDirect(BLOCK_SIZE)
        ByteStream.transfer(this, output, buffer)
    }

    override fun toString(): String {
        return "FileInputStream(path=$path,range=$range)"
    }

    companion object {

        suspend fun open(path: Path, range: LongRange?): FileInputStream {
            return withContext(Dispatchers.IO) {
                val channel = FileChannel.open(path, StandardOpenOption.READ)!!
                val endPosition = range?.let {
                    val fileSize = channel.size()
                    require(range.first >= 0 && range.first <= range.last) { "Illegal range: $range" }
                    require(range.last <= fileSize) { "Range $range is out of file bounds (length: $fileSize)" }

                    channel.position(range.first)
                    range.last
                }
                FileInputStream(channel, path, range, endPosition)
            }
        }

    }

}

internal class FileOutputStream private constructor(
    channel: FileChannel,
    private val path: Path,
    private val append: Boolean
) : FileByteStream(channel), OutputByteStream {

    override suspend fun write(buffer: ByteBuffer) {
        checkOpened()
        withContext(Dispatchers.IO) {
            channel.write(buffer)
        }
    }

    override fun toString(): String {
        return "FileOutputStream(path=$path, append=$append)"
    }

    companion object {

        suspend fun open(path: Path, append: Boolean): FileOutputStream {
            val option = if (append) StandardOpenOption.APPEND else StandardOpenOption.TRUNCATE_EXISTING
            val channel = withContext(Dispatchers.IO) {
                FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, option)!!
            }
            return FileOutputStream(channel, path, append)
        }

    }

}
