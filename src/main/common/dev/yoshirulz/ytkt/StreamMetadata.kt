package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import io.ktor.http.Parameters
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.parseUrlEncodedParameters
import io.ktor.util.KtorExperimentalAPI

typealias AdaptiveStreamMetadataSet = Pair<List<AudioOnlyStreamMetadata>, List<VideoOnlyStreamMetadata>>

@UseExperimental(ExperimentalUnsignedTypes::class)
val GET_BITRATE_FROM_TEST_DATA = 0UL //TODO

@UseExperimental(ExperimentalUnsignedTypes::class)
val GET_FRAMERATE_FROM_TEST_DATA = 0U //TODO

@UseExperimental(ExperimentalUnsignedTypes::class, KtorExperimentalAPI::class)
interface StreamMetadata {
	val iTag: ITag
	val uri: URI
	val container: Container
	/** In bytes (B). */ val size: ULong
	/** In bits per second (b/s). */ val bitrate: ULong

	fun getStream(scraper: YTKtScraper): MediaStream = scraper.httpClient.stream(this)

	companion object {
		val REGEX_A = Regex("""clen[/=](\d+)""")

		private val REGEX_C = Regex("""/s/([^/]*)""") //TODO doesn't need to be regex

		@RequiresXMLParser
		internal suspend fun adaptiveFromDASHManifest(scraper: YTKtScraper, playerSourceURI: String, dashManifestUrlOrig: String): AdaptiveStreamMetadataSet {
			val audioStreamsMetadata = mutableListOf<AudioOnlyStreamMetadata>()
			val videoStreamsMetadata = mutableListOf<VideoOnlyStreamMetadata>()
			val dashManifestUrl = REGEX_C.find(dashManifestUrlOrig)?.let { match ->
				match.groupValues[1].takeIf { it.isNotBlank() }
			}?.let { // need to decipher signature and set it in the URI
				dashManifestUrlOrig.setURIPathEncParam("signature", scraper.decipherer.getDecipherRoutine(playerSourceURI, scraper.httpClient).decipher(it))
			} ?: dashManifestUrlOrig
			for (streamInfoXML in scraper.getAndParseXML(dashManifestUrl.parseURI())
				.descendants("Representation") // get representation nodes from DASH manifest
				.filter { it.descendants("Initialization").firstOrNull()?.attribute("sourceURL")?.value?.contains("sq/") != true } // skip partial streams
			) {
				val iTag = ITag(streamInfoXML.attribute("id").value.toInt())
				val uri = streamInfoXML.element("BaseURL")!!.toString()
				val (rawContainer, rawContentLength) =
					if (uri.contains('?')) uri.parseUrlEncodedParameters().let { params -> Pair(params["mime"]!!.substringAfter('/'), params["clen"]!!) }
					else Pair(uri.substringAfter("/mime/").substringBefore('/').decodeURLQueryComponent(), uri.substringAfter("/clen/").substringBefore('/').decodeURLQueryComponent())
				val (container, contentLength) = Pair(Container.parse(rawContainer)!!, rawContentLength.toULong())
				val bitrate = streamInfoXML.attribute("bandwidth").value.toULong()
				if (streamInfoXML.element("AudioChannelConfiguration") != null) { // audio-only
					audioStreamsMetadata.add(AudioOnlyStreamMetadata(
						iTag,
						uri.parseURI(),
						container,
						contentLength,
						bitrate,
						AudioEncoding.parse(streamInfoXML.attribute("codecs").value)!!
					))
				} else { // video-only
					videoStreamsMetadata.add(VideoOnlyStreamMetadata(
						iTag,
						uri.parseURI(),
						container,
						contentLength,
						bitrate,
						VideoEncoding.parse(streamInfoXML.attribute("codecs").value)!!,
						iTag.videoQuality!!,
						VideoResolution(streamInfoXML.attribute("width").value.toUShort(), streamInfoXML.attribute("height").value.toUShort()),
						streamInfoXML.attribute("frameRate").value.toUInt()
					))
				}
			}
			return AdaptiveStreamMetadataSet(audioStreamsMetadata, videoStreamsMetadata)
		}

		internal suspend fun adaptiveFromMetadata(scraper: YTKtScraper, playerSourceURI: String, adaptiveMetadataUrlEncoded: String): AdaptiveStreamMetadataSet {
			val audioStreamsMetadata = mutableListOf<AudioOnlyStreamMetadata>()
			val videoStreamsMetadata = mutableListOf<VideoOnlyStreamMetadata>()
			for (qParams in adaptiveMetadataUrlEncoded
				.split(',')
				.filter { it.isNotEmpty() }
				.map{ it.parseUrlEncodedParameters() }
			) {
				val iTag = ITag(qParams["itag"]!!.toInt())
				val bitrate = qParams["bitrate"]!!.toULong()
				val uri = qParams["url"]!!.parseURI().let {
					val signature = qParams["s"] // if set, decipher and add to the new URI
					if (signature.isNullOrBlank()) it
					else it.copyAndSetParam(
						qParams["sp"] ?: "signature",
						scraper.decipherer.getDecipherRoutine(playerSourceURI, scraper.httpClient).decipher(signature)
					)
				}
				val container = Container.parse(qParams["type"]!!.substringAfter('/').substringBefore(';'))!!
				var contentLength = REGEX_A.find(uri.toString())!!.groupValues[1].toULongOrNull() ?: 0UL
				if (contentLength == 0UL) { // couldn't extract content length, get it manually
					contentLength = scraper.httpClient.getContentLength(uri) ?: 0UL
					if (contentLength == 0UL) continue // still not available, stream is gone or faulty
				}
				if (qParams["type"]!!.startsWith("audio/")) { // audio-only
					audioStreamsMetadata.add(AudioOnlyStreamMetadata(
						iTag,
						uri,
						container,
						contentLength,
						bitrate,
						AudioEncoding.parse(qParams["type"]!!.substringAfter('"').substringBefore('.'))!!
					))
				} else { // video-only
					videoStreamsMetadata.add(VideoOnlyStreamMetadata(
						iTag,
						uri,
						container,
						contentLength,
						bitrate,
						VideoEncoding.parse(qParams["type"]!!.substringAfter('"').substringBefore('.'))!!,
						VideoQuality.parse(qParams["quality_label"]!!.substringBefore('p') + 'p'),
						VideoResolution.parse(qParams["size"]!!),
						qParams["fps"]!!.toUInt()
					))
				}
			}
			return AdaptiveStreamMetadataSet(audioStreamsMetadata, videoStreamsMetadata)
		}
	}
}

interface AudioStreamMetadata: StreamMetadata {
	val audioEncoding: AudioEncoding
}

@UseExperimental(ExperimentalUnsignedTypes::class)
interface VideoStreamMetadata: StreamMetadata {
	val videoEncoding: VideoEncoding
	val videoQuality: VideoQuality
	val resolution: VideoResolution
	val framerate: UInt
}

@UseExperimental(ExperimentalUnsignedTypes::class)
class AudioOnlyStreamMetadata(
	override val iTag: ITag,
	override val uri: URI,
	override val container: Container,
	override val size: ULong,
	override val bitrate: ULong,
	override val audioEncoding: AudioEncoding
): AudioStreamMetadata {
	override fun toString() = "$iTag ($container) [audio]"
}

@UseExperimental(ExperimentalUnsignedTypes::class)
class VideoOnlyStreamMetadata(
	override val iTag: ITag,
	override val uri: URI,
	override val container: Container,
	override val size: ULong,
	override val bitrate: ULong,
	override val videoEncoding: VideoEncoding,
	override val videoQuality: VideoQuality,
	override val resolution: VideoResolution,
	override val framerate: UInt
): VideoStreamMetadata {
	override fun toString() = "$iTag ($container) [video]"
}

@UseExperimental(ExperimentalUnsignedTypes::class)
class AudioVisualStreamMetadata(
	override val iTag: ITag,
	override val uri: URI,
	override val container: Container,
	override val size: ULong,
	override val bitrate: ULong,
	override val audioEncoding: AudioEncoding,
	override val videoEncoding: VideoEncoding,
	override val videoQuality: VideoQuality,
	override val resolution: VideoResolution,
	override val framerate: UInt
): AudioStreamMetadata, VideoStreamMetadata {
	override fun toString() = "$iTag ($container) [av]"

	companion object {
		internal suspend fun fromQueryParams(qParams: Parameters, playerSourceURI: String, httpClient: HttpClient, decipherer: Decipherer): AudioVisualStreamMetadata? {
			val uri = qParams["url"]!!.parseURI().let {
				val signature = qParams["s"] // if set, decipher and add to the new URI
				if (signature.isNullOrBlank()) it
				else it.copyAndSetParam(
					qParams["sp"] ?: "signature",
					decipherer.getDecipherRoutine(playerSourceURI, httpClient).decipher(signature)
				)
			}
			var contentLength = StreamMetadata.REGEX_A.find(uri.toString())!!.groupValues[1].toULongOrNull() ?: 0UL
			if (contentLength == 0UL) { // couldn't extract content length, get it manually
				contentLength = httpClient.getContentLength(uri) ?: 0UL
				if (contentLength == 0UL) return null // still not available, stream is gone or faulty
			}
			val type = qParams["type"]!!
			val avCodecs = type.substringAfter('"').substringBefore('"').split(' ', limit = 2).map { it.substringBefore('.') }
			val iTag = ITag(qParams["itag"]!!.toInt())
			val videoQuality = iTag.videoQuality!!
			return AudioVisualStreamMetadata(
				iTag,
				uri,
				Container.parse(type.substringAfter('/').substringBefore(';'))!!,
				contentLength,
				GET_BITRATE_FROM_TEST_DATA,
				AudioEncoding.parse(avCodecs[1])!!,
				VideoEncoding.parse(avCodecs[0])!!,
				videoQuality,
				videoQuality.canonicalRes,
				GET_FRAMERATE_FROM_TEST_DATA
			)
		}
	}
}

data class StreamMetadataSet(
	val audioVisual: List<AudioVisualStreamMetadata>,
	val audioOnly: List<AudioOnlyStreamMetadata>,
	val videoOnly: List<VideoOnlyStreamMetadata>,
	/** Expiry date for this information */ val validUntil: Timestamp,
	/** Raw HTTP Live Streaming (HLS) URL to the m3u8 playlist. Null if not a live stream. */ val hlsLiveStreamUrl: String?
) {
	val all by lazy { audioVisual + audioOnly + videoOnly }

	val allWithAudio by lazy { audioVisual + audioOnly }

	val allWithVideo by lazy { audioVisual + videoOnly }
}
