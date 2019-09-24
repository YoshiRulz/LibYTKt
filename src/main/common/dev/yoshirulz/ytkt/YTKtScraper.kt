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
@Suppress("NOTHING_TO_INLINE")
data class YTKtScraper(val httpClient: HttpClient, val decipherer: Decipherer = Decipherer()): Closeable by httpClient {
	internal suspend inline fun get(uri: String): String = httpClient.get(uri)

	internal suspend inline fun get(uri: URI): String = httpClient.get(uri)

	@EntryPoint
	suspend inline fun CCTrackMetadata.downloadCCsFromMetadata(output: Stream, progress: IProgress<Double>? = null): Unit =
		StreamWriter(output, StreamWriter.Companion.Encoding.UTF8, 1024, true).use { writer ->
			val captions = this.getCaptions(this@YTKtScraper.httpClient).captions
			captions.forEachIndexed { lineNo, it ->
				writer.writeLineAsync("$lineNo\n${it.timespan.toCaptionMarker()}\n${it.text}\n") // serialise and write
				progress?.report(lineNo.toDouble() / captions.size) // report progress
			}
		}

	suspend inline fun CCTrackMetadata.getCaptions(): ClosedCaptionsTrack = this.getCaptions(this@YTKtScraper.httpClient)

	suspend inline fun ChannelID.getData(): Channel = this.getData(this@YTKtScraper.httpClient)

	suspend inline fun PlaylistID.getData(): Playlist = this.getData(this@YTKtScraper.httpClient)

	suspend inline fun SearchQuery.getPartialResults(maxResults: Int): List<Video> = this.getPartialResults(this@YTKtScraper.httpClient, maxResults)

	@EntryPoint
	inline fun SearchQuery.getResults(scope: CoroutineScope): ReceiveChannel<Video> = this.getResults(this@YTKtScraper.httpClient, scope)

	@EntryPoint
	suspend inline fun StreamMetadata.downloadStreamFromMetadata(output: Stream): Unit = this.getStream(this@YTKtScraper.httpClient).writeTo(output)

	inline fun StreamMetadata.getStream(): MediaStream = this.getStream(this@YTKtScraper.httpClient)

	suspend inline fun Username.getChannelID(): ChannelID = this.getChannelID(this@YTKtScraper.httpClient)

	@EntryPoint
	suspend inline fun Username.getData(): User = this.getData(this@YTKtScraper.httpClient)

	suspend inline fun VideoID.getCCMetadata(): List<CCTrackMetadata> = this.getCCMetadata(this@YTKtScraper.httpClient)

	suspend inline fun VideoID.getData(): Video = this.getData(this@YTKtScraper.httpClient)

	suspend inline fun VideoID.getStreamMetadataSet(): StreamMetadataSet = this.getStreamMetadataSet(this@YTKtScraper.httpClient, this@YTKtScraper.decipherer)
}
