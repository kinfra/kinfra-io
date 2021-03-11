package ru.kontur.kinfra.io.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ru.kontur.kinfra.io.*
import ru.kontur.kinfra.io.InputByteStream.Companion.transfer
import ru.kontur.kinfra.io.utils.withRemainingAtMost
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption

private const val BLOCK_SIZE = 4096

internal abstract class AbstractFileStream(
    protected val channel: FileChannel
) : AbstractCloseable() {

    final override suspend fun closeImpl() {
        withContext(Dispatchers.IO + NonCancellable) {
            channel.close()
        }
    }

}

internal class FileInputStream private constructor(
    channel: FileChannel,
    private val path: Path,
    private val range: LongRange?,
    private var limit: Long,
) : AbstractFileStream(channel), InputByteStream {

    override suspend fun read(buffer: ByteBuffer): Boolean {
        checkOpened()
        val oldPosition = buffer.position()
        val success = withContext(Dispatchers.IO) {
            if (limit > Int.MAX_VALUE) {
                readInternal(buffer)
            } else {
                val intLimit = limit.toInt()
                buffer.withRemainingAtMost(intLimit) {
                    readInternal(it)
                }
            }
        }
        limit -= buffer.position() - oldPosition
        check(limit >= 0) { "Invariant violated: limit exceeded (requested range: $range)" }
        return success
    }

    private fun readInternal(dst: ByteBuffer): Boolean {
        val count = channel.read(dst)
        return count != -1
    }

    // todo: implement more efficient file-to-file transfer via Channel's transferTo()
    override suspend fun transferTo(output: OutputByteStream): Long {
        checkOpened()
        // Direct buffers are preferred for file I/O
        val buffer = ByteBuffer.allocateDirect(BLOCK_SIZE)
        return transfer(this, output, buffer)
    }

    override fun toString(): String {
        return "FileInputStream(path=$path,range=$range)"
    }

    companion object {

        suspend fun open(path: Path, range: LongRange?): FileInputStream {
            var channelToClose: FileChannel? = null
            return try {
                withContext(Dispatchers.IO) {
                    val channel = FileChannel.open(path, StandardOpenOption.READ)
                    channelToClose = channel
                    val limit = if (range != null) {
                        val fileSize = channel.size()
                        require(range.first >= 0 && range.first <= range.last) { "Illegal range: $range" }
                        require(range.last <= fileSize) { "Range $range is out of file bounds (length: $fileSize)" }
                        channel.position(range.first)
                        range.last - range.first
                    } else {
                        Long.MAX_VALUE
                    }
                    FileInputStream(channel, path, range, limit)
                }
            } catch (e: Throwable) {
                channelToClose?.let { channel ->
                    withContext(Dispatchers.IO + NonCancellable) {
                        channel.close()
                    }
                }
                throw e
            }
        }

    }

}

internal class FileOutputStream private constructor(
    channel: FileChannel,
    private val path: Path,
    private val append: Boolean
) : AbstractFileStream(channel), OutputByteStream {

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
            var channelToClose: FileChannel? = null
            try {
                return withContext(Dispatchers.IO) {
                    val channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, option)
                    channelToClose = channel
                    FileOutputStream(channel, path, append)
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.IO + NonCancellable) {
                    channelToClose?.close()
                }
                throw e
            }
        }

    }

}
