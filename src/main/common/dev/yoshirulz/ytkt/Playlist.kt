package dev.yoshirulz.ytkt

import io.ktor.http.parametersOf

@UseExperimental(ExperimentalUnsignedTypes::class)
data class Playlist(val id: PlaylistID, val author: String, val title: String, val description: String, val viewCount: ULong, val videos: List<Video>) {
	@EntryPoint
	val canonicalURI: URI get() = id.canonicalURI

	/** The embedded player page of the first video, with this playlist loaded. */
	val firstVidEmbedURI: URI get() = ytCanonicalURI("/embed/${videos[0].id.raw}", parametersOf("list", id.raw))

	/** A shortened URI that redirects to the first video's player page, with this playlist loaded. */
	val firstVidShortURI: URI get() = ytCanonicalURI("/${videos[0].id.raw}", parametersOf("list", id.raw), isShortened = true)

	/** The first video's player page, with this playlist loaded. */
	val firstVidWatchURI: URI get() = ytCanonicalURI("/watch", parametersOf("v" to listOf(videos[0].id.raw), "list" to listOf(id.raw)))

	override fun toString() = "[${id.type}, ${videos.size} videos] $title"
}
