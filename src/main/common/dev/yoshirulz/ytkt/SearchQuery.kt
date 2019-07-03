package dev.yoshirulz.ytkt

import io.ktor.http.parametersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope

@UseExperimental(ExperimentalCoroutinesApi::class)
data class SearchQuery(val query: String) {
	private fun genPaginatedURI(page: Int) = ytCanonicalURI("/search_ajax", parametersOf("style" to listOf("json"), "search_query" to listOf(query), "page" to listOf(page.toString()), "hl" to listOf("en")))

	suspend fun getPartialResults(scraper: YTKtScraper, maxResults: Int): List<Video> = coroutineScope {
		val resultGen = getResults(scraper, this)
		val results = mutableListOf<Video>()
		try {
			while (results.size < maxResults) results.add(resultGen.receive())
		} catch (_: ClosedReceiveChannelException) {
			// ignored
		}
		finally { resultGen.cancel() }
		results
	}

	fun getResults(scraper: YTKtScraper, scope: CoroutineScope): ReceiveChannel<Video> = scope.produce {
		var page = 1
		var tempPageResults: List<YTVideoDetails>
		while (true) {
			tempPageResults = YTSearchResults.parse(scraper.get(genPaginatedURI(page++))).video
			if (tempPageResults.isEmpty()) break
			tempPageResults.forEach { send(Video.fromJSON(it)) }
		}
	}
}
