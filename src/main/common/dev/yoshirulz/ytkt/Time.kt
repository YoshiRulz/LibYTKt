package dev.yoshirulz.ytkt

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

typealias RelativeTimespan = LongRange

fun RelativeTimespan.toCaptionMarker(): String = "${RelativeTimestamp(first)} --> ${RelativeTimestamp(endInclusive)}"

inline class RelativeTimestamp(/** Milliseconds since an arbitrary epoch i.e. the start of a video */ val ms: Long) {
	operator fun rangeTo(other: RelativeTimestamp): RelativeTimespan = ms..other.ms

	override fun toString(): String {
		fun Long.pad(i: Int = 2) = this.toString().padStart(i, '0')
		val s = ms / 1000L
		val m = s / 60L
		val h = m / 60L
		return "${h.pad()}:${(m - h * 60L).pad()}:${(s - m * 60L).pad()}.${(ms - s * 1000L).pad(3)}"
	}
}

@UseExperimental(ExperimentalTime::class)
inline class Timestamp(/** Milliseconds since Unix epoch */ private val ms: Long) {
	operator fun plus(other: Duration): Timestamp = Timestamp(ms + other.toLongMilliseconds())

	companion object {
		fun fromYMDString(str: String): Timestamp = str.split('-', limit = 3).let { platformTimeFromYMD(it[0].toInt(), it[1].toInt(), it[2].toInt()) }

		fun now(): Timestamp = platformTimeNow()
	}
}

expect fun platformTimeFromYMD(year: Int, month: Int, day: Int): Timestamp

expect fun platformTimeNow(): Timestamp
