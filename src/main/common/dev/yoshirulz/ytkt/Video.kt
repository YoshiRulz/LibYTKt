package dev.yoshirulz.ytkt

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@UseExperimental(ExperimentalTime::class, ExperimentalUnsignedTypes::class)
data class Video constructor(
	val id: VideoID,
	val author: String,
	val uploadDate: Timestamp,
	val title: String,
	val description: String,
	val duration: Duration,
	val keywords: List<String>,
	val viewCount: ULong,
	val likeCount: ULong,
	val dislikeCount: ULong
) {
	@EntryPoint
	val canonicalURI: URI get() = id.canonicalURI

	@EntryPoint
	val highResThumbnailURI = "https://img.youtube.com/vi/$id/hqdefault.jpg"

	@EntryPoint
	val lowResThumbnailURI = "https://img.youtube.com/vi/$id/default.jpg"

	/** Not always available */
	@EntryPoint
	val maxResThumbnailURI = "https://img.youtube.com/vi/$id/maxresdefault.jpg"

	@EntryPoint
	val mediumResThumbnailURI = "https://img.youtube.com/vi/$id/mqdefault.jpg"

	/** Not always available */
	@EntryPoint
	val standardResThumbnailURI = "https://img.youtube.com/vi/$id/sddefault.jpg"

	//TODO dupe id values
	override fun toString() = "[$duration, $author] $title"

	companion object {
		private val KEYWORDS_REGEX = Regex("\"[^\"]+\"|[^ ]+") // double quotes are silently stripped from keywords so it's safe to pair them (tested w/ pre-Polymer UI)

		fun fromJSON(json: YTVideoDetails, cachedID: VideoID = VideoID.parse(json.encrypted_id)!!): Video = Video(
			cachedID,
			json.author,
			Timestamp(1000L * json.time_created),
			json.title,
			json.description,
			json.length_seconds.seconds,
			KEYWORDS_REGEX.findAll(json.keywords).mapTo(mutableListOf()) { if (it.value[0] == '"') it.value.substring(1, it.value.lastIndex) else it.value },
			json.views.stripNonDigit().toULong(),
			json.likes.toULong(),
			json.dislikes.toULong()
		)
	}
}
