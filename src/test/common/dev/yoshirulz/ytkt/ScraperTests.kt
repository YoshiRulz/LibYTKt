package dev.yoshirulz.ytkt

import dev.yoshirulz.ytkt.StructuredTestData.ChannelIDs
import dev.yoshirulz.ytkt.StructuredTestData.MockProvider
import dev.yoshirulz.ytkt.StructuredTestData.PlaylistData
import dev.yoshirulz.ytkt.StructuredTestData.PlaylistIDs
import dev.yoshirulz.ytkt.StructuredTestData.SearchResults
import dev.yoshirulz.ytkt.StructuredTestData.Usernames
import dev.yoshirulz.ytkt.StructuredTestData.VideoIDs
import dev.yoshirulz.ytkt.StructuredTestData.VideoInfoMaps
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.headersOf
import io.ktor.utils.io.core.use
import kotlinx.coroutines.delay
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.fail

/** for native, waiting on [Kotlin/kotlinx.serialization#188](https://github.com/Kotlin/kotlinx.serialization/issues/188), one of the contributors has made a separate library but it doesn't target Kotlin/Native */
@Experimental(level = Experimental.Level.WARNING)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
internal annotation class RequiresXMLParser

@EntryPoint
object ScraperTests {
	private const val LIVE_TEST = false

	private val MOCK_HEADER_FORM_ENC = headersOf("Content-Type" to listOf(ContentType.Application.FormUrlEncoded.toString()))

	private val MOCK_HEADER_JSON = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

	private fun genTestHTTPClient() = HttpClient(MockEngine) {
		engine {
			addHandler { request ->
				val url = request.url
				if (!url.host.endsWith("youtube.com")) error("no mock data for domain ${url.host}")
				try {
					inline fun genResponse(mockProvider: MockProvider, headers: Headers) = mockProvider.mockFromParams(url.parameters).let { (body, status) ->
						if (body == null) respondError(status) else respond(body, status, headers = headers)
					}
					when (url.encodedPath) {
						"/get_video_info" -> genResponse(VideoInfoMaps, MOCK_HEADER_FORM_ENC)
						"/list_ajax" -> genResponse(PlaylistData, MOCK_HEADER_JSON)
						"/search_ajax" -> genResponse(SearchResults, MOCK_HEADER_JSON)
						else -> null
					}?.also { delay(1000L) } ?: error("no mock data for $url")
				} catch (e: Exception) {
					error("generating response for $url threw an exception of type ${e::class.simpleName}: ${e.message ?: "no message"}")
				}
			}
		}
		install(JsonFeature)
	}

	@Suppress("ConstantConditionIf")
	private suspend fun usingScraper(block: suspend YTKtScraper.() -> Unit) = withConfiguredScraper(
		if (LIVE_TEST) genDefaultHTTPClient() else genTestHTTPClient(),
		block
	)

	@Test
	fun testCCMetadataFromID() = nativeRunBlocking {
		usingScraper {
//			VideoIDs.valid_existent_playable.forEach { // CBB
			VideoIDs.valid_existent_playable_withCCs.forEach {
				assertSucceeds { VideoID(it).getCCMetadata() }
			}
			VideoIDs.valid_nonexistent.forEach {
				assertFailsWith(VideoUnavailableException::class) { VideoID(it).getCCMetadata() }
			}
		}
	}

	@Ignore
	@RequiresXMLParser
	@Test
	fun testCCs() = nativeRunBlocking {
		usingScraper {
			VideoIDs.valid_existent_playable_withCCs.forEach {
				assertSucceeds {
					val ccMetadata = VideoID(it).getCCMetadata()
					(ccMetadata.firstOrNull() ?: fail()).getCaptions()
					testCCsToNativeFile(ccMetadata.random())
				}
			}
		}
	}

	@Ignore
	@RequiresXMLParser
	@Test
	fun testChannelFromID() = nativeRunBlocking {
		usingScraper {
			ChannelIDs.valid.map { ChannelID(it) }.forEach {
				assertSucceeds { it.getData() }
			}
		}
	}

	@Ignore
	@RequiresXMLParser
	@Test
	fun testChannelIDFromUsername() = nativeRunBlocking {
		usingScraper {
			Usernames.valid.map { Username(it) }.forEach {
				assertSucceeds { it.getChannelID() }
			}
		}
	}

	@Test
	fun testPlaylistFromID() = nativeRunBlocking {
		usingScraper {
			PlaylistData.rawResults.forEach { (rawID, data) ->
				assertTrue { PlaylistID.parse(rawID)!!.getData().videos.size == data.first }
			}
			PlaylistIDs.valid_unusable.map { PlaylistID.parse(it)!! }.forEach {
				assertFailsWith(ClientRequestException::class) { it.getData() }
			}
		}
	}

	@Test
	fun testSearchResults() = nativeRunBlocking {
		usingScraper {
			SearchResults.queries.forEach {
				assertTrue { it.getPartialResults(30).size <= 30 }
			}
		}
	}

	@Ignore
	@RequiresXMLParser
	@Test
	fun testStreaming() = nativeRunBlocking {
		usingScraper {
			VideoIDs.valid_nonexistent.map { VideoID(it) }.forEach {
				assertFailsWith(VideoUnavailableException::class) { it.getStreamMetadataSet() }
			}
			VideoIDs.valid_existent_unplayable.map { VideoID(it) }.forEach {
				assertFailsWith(VideoUnplayableException::class) { it.getStreamMetadataSet() } // the only datum is a premium video, so we should catch a VideoRequiresPurchaseException
			}
			assertSucceeds {
				// try all of first ID
				VideoID(VideoIDs.valid_existent_playable[0]).let { videoID ->
					val metadataSet = videoID.getStreamMetadataSet()
					metadataSet.audioVisual[0].getStream().use { it.writeTo(ByteArray(4096).toStream()) }
					arrayOf(metadataSet.audioOnly, metadataSet.videoOnly, metadataSet.audioVisual)
						.mapNotNull { l -> l.minBy { it.size } }
						.forEach { testStreamingToNativeFile(it) }
				}
				// try AV of remaining IDs
				VideoIDs.valid_existent_playable.drop(1).map { VideoID(it) }.forEach { videoID ->
					testStreamingToNativeFile(videoID.getStreamMetadataSet().audioVisual.minBy { it.size }!!)
				}
			}
		}
	}

	@Ignore
	@RequiresXMLParser
	@Test
	fun testVideoFromID() = nativeRunBlocking {
		usingScraper {
			VideoIDs.valid_existent.map { VideoID(it) }.forEach {
				assertSucceeds { it.getData() }
			}
			VideoIDs.valid_nonexistent.map { VideoID(it) }.forEach {
				assertFailsWith(VideoUnavailableException::class) { it.getData() }
			}
		}
	}
}
