package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.utils.io.core.Closeable
import io.ktor.utils.io.core.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Most use cases will only need one instance of this during their execution lifecycle.
 * @see withScraper
 * @see withConfiguredScraper
 */
class YTKtScraper(val httpClient: HttpClient): Closeable {
	val decipherer = Decipherer() // holds cached decipher routines

	internal suspend inline fun get(uri: String): String = httpClient.get(uri)

	internal suspend inline fun get(uri: URI): String = httpClient.get(uri)

	@RequiresXMLParser
	internal suspend inline fun getAndParseHTML(uri: URI) = HTMLParser.parse(httpClient.get(uri))

	@RequiresXMLParser
	internal suspend inline fun getAndParseXML(uri: URI) = XElement.parse(httpClient.get(uri)).stripNamespaces()

	@EntryPoint
	@RequiresXMLParser
	suspend fun CCTrackMetadata.downloadCCsFromMetadata(output: Stream, progress: IProgress<Double>? = null): Unit =
		StreamWriter(output, StreamWriter.Companion.Encoding.UTF8, 1024, true).use { writer ->
			val captions = this.getCaptions(this@YTKtScraper).captions
			var lineNo = 0
			captions.forEach {
				lineNo++ // lines start at 1, pre-increment
				writer.writeLineAsync("$lineNo\n${it.timespan.toCaptionMarker()}\n${it.text}\n") // serialise and write
				progress?.report(lineNo.toDouble() / captions.size) // report progress
			}
		}

	@RequiresXMLParser
	suspend fun CCTrackMetadata.getCaptions(): ClosedCaptionsTrack = this.getCaptions(this@YTKtScraper)

	@RequiresXMLParser
	suspend fun ChannelID.getData(): Channel = this.getData(this@YTKtScraper)

	suspend fun PlaylistID.getData(): Playlist = this.getData(this@YTKtScraper)

	suspend fun SearchQuery.getPartialResults(maxResults: Int): List<Video> = this.getPartialResults(this@YTKtScraper, maxResults)

	@EntryPoint
	fun SearchQuery.getResults(scope: CoroutineScope): ReceiveChannel<Video> = this.getResults(this@YTKtScraper, scope)

	@EntryPoint
	suspend fun StreamMetadata.downloadStreamFromMetadata(output: Stream): Unit = this.getStream(this@YTKtScraper).writeTo(output)

	fun StreamMetadata.getStream(): MediaStream = this.getStream(this@YTKtScraper)

	@RequiresXMLParser
	suspend fun Username.getChannelID(): ChannelID = this.getChannelID(this@YTKtScraper)

	@EntryPoint
	suspend fun Username.getData(): User = this.getData(this@YTKtScraper)

	suspend fun VideoID.getCCMetadata(): List<CCTrackMetadata> = this.getCCMetadata(this@YTKtScraper)

	@RequiresXMLParser
	suspend fun VideoID.getData(): Video = this.getData(this@YTKtScraper)

	@RequiresXMLParser
	suspend fun VideoID.getStreamMetadataSet(): StreamMetadataSet = this.getStreamMetadataSet(this@YTKtScraper)

	override fun close() = httpClient.close()
}
