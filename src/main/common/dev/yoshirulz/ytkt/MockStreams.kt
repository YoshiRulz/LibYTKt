@file:Suppress("UNUSED", "UNUSED_PARAMETER")

package dev.yoshirulz.ytkt

import io.ktor.utils.io.core.Closeable

@Suppress("NOTHING_TO_INLINE")
private inline fun noTODO(): Nothing = throw NotImplementedError()

interface Stream: Closeable

fun ByteArray.toStream(): Stream = noTODO()

class StreamWriter(private val stream: Stream, val encoding: Encoding, val i: Int, val b: Boolean): Closeable {
	fun writeLineAsync(string: String): Unit = noTODO()

	override fun close(): Unit = stream.close()

	companion object {
		enum class Encoding {
			UTF8
		}
	}
}

interface File: Stream {
	companion object {
		fun create(path: String): File = noTODO()
	}
}

interface IProgress<T> {
	fun report(progress: T)
}

@UseExperimental(ExperimentalUnsignedTypes::class)
open class MediaStream(val url: String, val range: ULongRange, val segmented: Boolean = false): Stream {
	@Suppress("RedundantSuspendModifier")
	suspend fun writeTo(output: Stream): Unit = noTODO()

	override fun close(): Unit = noTODO()

	companion object {
		/** This number was "carefully devised" through "research". */
		private const val RATE_LIMIT_SIZE = 9898989L
	}
}
