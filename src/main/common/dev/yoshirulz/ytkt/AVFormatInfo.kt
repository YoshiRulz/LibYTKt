@file:Suppress("SpellCheckingInspection")

package dev.yoshirulz.ytkt

import kotlin.math.ceil

enum class AudioEncoding(val code: String) {
	@Suppress("unused") MP4A("mp4a"),
	@Suppress("unused") Vorbis("vorbis"),
	@Suppress("unused") OPUS("opus");

	companion object {
		fun parse(code: String): AudioEncoding? = values().firstOrNull { it.code == code }
	}
}

/** Media stream container type */
enum class Container(val fileExtension: String) {
	@Suppress("unused") MP4("mp4"),
	@Suppress("unused") WebM("webm"),
	@Suppress("unused") TGPP("3gpp");

	companion object {
		fun parse(fileExtension: String): Container? = values().firstOrNull { it.fileExtension == fileExtension }
	}
}

/** Unique tag that identifies the properties of the associated stream */
data class ITag(val intVal: Int) {
	/** @return encoded quality info, null for audio-only streams */
	val videoQuality: VideoQuality? get() = when (intVal) {
		5, 13, 17, 91, 151, 160, 278, 330, 394 -> VideoQuality.Low144p
		6, 36, 92, 132, 133, 242, 331, 395 -> VideoQuality.Low240p
		18, 34, 43, 82, 93, 100, 134, 167, 243, 332, 396 -> VideoQuality.Medium360p
		35, 44, 59, 78, 83, 94, 101, 135, 168, 212, 213, 218, 219, 244, 245, 246, 333, 397 -> VideoQuality.Medium480p
		22, 45, 84, 95, 102, 136, 169, 214, 215, 247, 298, 302, 334, 398 -> VideoQuality.High720p
		37, 46, 85, 96, 137, 170, 216, 217, 248, 299, 399, 303, 335 -> VideoQuality.High1080p
		264, 271, 308, 336 -> VideoQuality.High1440p
		266, 272, 313, 315, 337 -> VideoQuality.High2160p
		38 -> VideoQuality.High3072p
		138 -> VideoQuality.High4320p
		else -> null
	}
}

/** TODO lowercase? */
enum class VideoEncoding(val code: String) {
	/** MPEG-4 Part 2 */ @Suppress("unused") MP4V("mp4v"),
	/** MPEG-4 Part 10, H264, Advanced Video Coding (AVC) */ @Suppress("unused") H264("avc1"),
	@Suppress("unused") VP8("vp8"),
	@Suppress("unused") VP9("vp9"),
	@Suppress("unused") AV1("av01");

	companion object {
		fun parse(code: String): VideoEncoding? = if (code == "unknown") AV1 else values().firstOrNull { it.code == code }
	}
}

@UseExperimental(ExperimentalUnsignedTypes::class)
enum class VideoQuality(
	val canonicalRes: VideoResolution,
	/** `###p` where `###` is how many rows of pixel data make up one frame (the p stands for progressive/non-interlaced scan) */
	val resLabel: String = "${canonicalRes.height}p"
) {
	/** Low quality (144p) */ Low144p(VideoResolution(256u, 144u)),
	/** Low quality (240p) */ Low240p(VideoResolution(426u, 240u)),
	/** Medium quality (360p) */ Medium360p(VideoResolution(640u, 360u)),
	/** Medium quality (480p) */ Medium480p(VideoResolution(854u, 480u)),
	/** High quality (720p) */ High720p(VideoResolution(1280u, 720u)),
	/** High quality (1080p) */ High1080p(VideoResolution(1920u, 1080u)),
	/** High quality (1440p) */ High1440p(VideoResolution(2560u, 1440u)),
	/** High quality (2160p) */ High2160p(VideoResolution(3840u, 2160u)),
	/** High quality (3072p) */ High3072p(VideoResolution(4096u, 3072u)),
	/** High quality (4320p) */ High4320p(VideoResolution(7680u, 4320u));

	/** framerate only shown if it's above 30, and it's rounded up to the nearest ten */
	@EntryPoint
	fun resLabelWithFramerate(framerate: UInt): String = if (framerate > 30U) "$resLabel${ceil(framerate.toDouble() / 10)}0" else resLabel

	companion object {
		fun parse(resLabel: String): VideoQuality = values().first { it.resLabel == resLabel }
	}
}

@UseExperimental(ExperimentalUnsignedTypes::class)
data class VideoResolution(val width: UShort, val height: UShort) {
	companion object {
		fun parse(s: String): VideoResolution = s.indexOf('x').let {
			VideoResolution(s.slice(0 until it).toUShort(), s.slice(it + 1 until s.length).toUShort())
		}
	}
}
