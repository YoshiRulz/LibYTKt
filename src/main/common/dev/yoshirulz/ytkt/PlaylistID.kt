package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.parametersOf
import kotlin.collections.set

@UseExperimental(ExperimentalUnsignedTypes::class)
data class PlaylistID(val raw: String, val type: PlaylistType) {
	/** The playlist page. */
	val canonicalURI: URI get() = ytCanonicalURI("/playlist", parametersOf("list", raw))

	/**
	 * TODO inherited docs claim 1 "page" holds <= 200 videos, but the index is incremented by 100...
	 * TODO test playlist visibility options
	 */
	suspend fun getData(httpClient: HttpClient): Playlist {
		suspend inline fun getRawResults(index: UInt) = YTPlaylistDetails.parse(httpClient.get(ytCanonicalURI(
			"/list_ajax",
			parametersOf(
				"style" to listOf("json"),
				"action_get_list" to listOf("1"),
				"list" to listOf(raw),
				"index" to listOf(index.toString()),
				"hl" to listOf("en")
			)
		)))
		inline fun appendFromRaw(received: MutableMap<VideoID, Video>, rawResults: YTPlaylistDetails): UInt {
			val raw = rawResults.video?.takeIf { it.isNotEmpty() } ?: return 0U
			var added = 0U
			var cachedID: VideoID
			raw.forEach { videoJSON ->
				cachedID = VideoID.parse(videoJSON.encrypted_id)!!
				if (!received.containsKey(cachedID)) {
					received[cachedID] = Video.fromJSON(videoJSON, cachedID)
					added++
				}
			}
			return added
		}
		var index = if (type == PlaylistType.Playlist) 101U else 1U
		val firstRaw = getRawResults(index)
		val first = firstRaw.video.orEmpty()
		val received = mutableMapOf<VideoID, Video>()
		if (first.isNotEmpty()) {
			first.forEach { received[VideoID.parse(it.encrypted_id)!!] = Video.fromJSON(it) } // first "page" will never contain duplicates
			do index += 100U while (appendFromRaw(received, getRawResults(index)) != 0U)
		}
		return Playlist(
			this,
			firstRaw.author.orEmpty(),
			firstRaw.title,
			firstRaw.description.orEmpty(),
			firstRaw.views?.toULong() ?: 0UL,
			received.values.toList()
		)
	}

	override fun toString() = raw

	companion object {
		enum class PlaylistType(val prefix: String) {
			/** A channel's "Favorites" playlist (probably abbreviates "favorites list"). */
			ChannelFavorites("FL"),
			/** A channel's "Liked videos" playlist (probably abbreviates "likes list"). */
			ChannelLiked("LL"),
			/**
			 * An autogenerated playlist populated by an artist's songs from one album, in order (a backronym is "opus list").
			 *
			 * These commonly use the wrong/live version of a song, or load from an unaffiliated channel instead of an "Official Artist Channel" or an autogenerated placeholder channel.
			 * The prefix seems to actually be `OLAK5uy_`.
			 */
			@Suppress("unused")
			Album("OL"),
			/**
			 * A regular, user-generated playlist.
			 *
			 * Apart from the common 32-char alphanumeric IDs, there seem to be shorter (perhaps older?) 16-char hexadecimal IDs.
			 */
			Playlist("PL"),
			/** Like [ChannelUploads], but sorted by popularity instead of upload time. */
			PopularUploads("PU"),
			/**
			 * An autogenerated "YouTube Mix" playlist, populated with similar videos (probably abbreviates "random").
			 *
			 * IDs seem to be the "seed" video's ID prefixed by either `RD` or `RDMM`.
			 */
			YouTubeMix("RD"),
			/** Like [YouTubeMix], but limited to the same channel as this video (probably abbreviates "user list"). */
			SameChannelMix("UL"),
			/** An autogenerated playlist of all a channel's uploads (probably abbreviates "user uploads"). */
			ChannelUploads("UU"),
			/** A user's "Watch later" playlist. */
			WatchLater("WL");

			companion object {
				fun parse(prefix: String): PlaylistType = values().first { it.prefix == prefix }
			}
		}

		/** matches `PLOU2XLYxmsIJGErt5rrCqaSGTMyyqNt2H` and `WL` */
		private val PLAYLIST_ID_PATTERN = Regex("""^(?:PL(?:[\-0-9A-Z_a-z]{32}|[0-9A-F]{16})|OLAK5uy_[\-0-9A-Z_a-z]{33}|WL|(?:RD(?:MM)?|UL)${VideoID.VIDEO_ID_PATTERN_FRAGMENT}|(?:FL|LL|PU|UU)${ChannelID.CHANNEL_ID_PATTERN_FRAGMENT})$""")

		@EntryPoint
		val CURRENT_USER_WATCH_LATER = PlaylistID(PlaylistType.WatchLater.prefix, PlaylistType.WatchLater)

		fun parse(raw: String): PlaylistID? = if (PLAYLIST_ID_PATTERN matches raw) PlaylistID(raw, PlaylistType.parse(raw.substring(0..1))) else null

		@EntryPoint
		fun parseFromURI(uri: String): PlaylistID? = parseFromURI(uri.parseURI())

		fun parseFromURI(uri: URI): PlaylistID? = when (YouTubeDomain.ofURI(uri)) {
			YouTubeDomain.MainOrMirror -> uri.parameters["list"]?.takeIf {
				uri.encodedPath.let { it.startsWith("/embed/") || it == "/playlist" || it == "/watch" }
			}
			YouTubeDomain.Short -> uri.parameters["list"]
			else -> null
		}?.let { parse(it) }
	}
}
