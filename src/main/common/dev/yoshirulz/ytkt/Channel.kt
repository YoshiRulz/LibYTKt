package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import kotlinx.coroutines.coroutineScope

data class Channel(val id: ChannelID, val title: String, val logoURI: String) {
	/** The channel home page. */
	@EntryPoint
	val canonicalURI: URI get() = id.canonicalURI

	/** This channel's "Favorites" playlist. */
	@EntryPoint
	val linkedFavoritesPlaylist: PlaylistID get() = id.linkedFavoritesPlaylist

	/** This channel's "Liked videos" playlist. */
	@EntryPoint
	val linkedLikesPlaylist: PlaylistID get() = id.linkedLikesPlaylist

	/** Like [linkedUploadsPlaylist], but sorted by popularity instead of upload time. */
	@EntryPoint
	val linkedPopularPlaylist: PlaylistID get() = id.linkedPopularPlaylist

	/** The autogenerated playlist of all the channel's uploads. */
	@EntryPoint
	val linkedUploadsPlaylist: PlaylistID get() = id.linkedUploadsPlaylist

	override fun toString() = "[channel] $title"
}

data class ChannelID internal constructor(val raw: String) {
	/** The channel home page. */
	val canonicalURI: URI get() = ytCanonicalURI("/channel/$raw")

	/** This channel's "Favorites" playlist. */
	val linkedFavoritesPlaylist: PlaylistID get() = PlaylistID(raw.replaceRange(0..1, "FL"), PlaylistID.Companion.PlaylistType.ChannelFavorites)

	/** This channel's "Liked videos" playlist. */
	val linkedLikesPlaylist: PlaylistID get() = PlaylistID(raw.replaceRange(0..1, "LL"), PlaylistID.Companion.PlaylistType.ChannelLiked)

	/** Like [linkedUploadsPlaylist], but sorted by popularity instead of upload time. */
	val linkedPopularPlaylist: PlaylistID get() = PlaylistID(raw.replaceRange(0..1, "PU"), PlaylistID.Companion.PlaylistType.PopularUploads)

	/** The autogenerated playlist of all the channel's uploads. */
	val linkedUploadsPlaylist: PlaylistID get() = PlaylistID(raw.replaceRange(0..1, "UU"), PlaylistID.Companion.PlaylistType.ChannelUploads)

	suspend fun getData(httpClient: HttpClient): Channel = coroutineScope {
		canonicalURI.requestPageProxied(httpClient, this)
			.receiveSingleOrElse { throw PageRequestFailureException() }
			.let { channelPageBody ->
				Channel(
					this@ChannelID,
					channelPageBody.querySelector("""meta[property="og:title"]""")!!.getAttribute("content"),
					channelPageBody.querySelector("""meta[property="og:image"]""")!!.getAttribute("content")
				)
			}
	}

	override fun toString() = raw

	companion object {
		const val CHANNEL_ID_PATTERN_FRAGMENT = """[\-0-9A-Z_a-z]{22}"""

		/** matches `UC3xnGqlcL3y-GXz5N3wiTJQ` */
		private val CHANNEL_ID_PATTERN = Regex("^UC$CHANNEL_ID_PATTERN_FRAGMENT\$")

		fun parse(raw: String): ChannelID? = if (CHANNEL_ID_PATTERN matches raw) ChannelID(raw) else null

		@EntryPoint
		fun parseFromURI(uri: String): ChannelID? = parseFromURI(uri.parseURI())

		fun parseFromURI(uri: URI): ChannelID? = uri.encodedPath.let {
			if (YouTubeDomain.ofURI(uri) == YouTubeDomain.MainOrMirror && it.startsWith("/channel/"))
				parse(it.substring(9).substringBefore('/'))
			else null
		}
	}
}
