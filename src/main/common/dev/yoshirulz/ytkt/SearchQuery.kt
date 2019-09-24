package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.parametersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope

@UseExperimental(ExperimentalCoroutinesApi::class)
data class SearchQuery(val query: String) {
	suspend fun getPartialResults(httpClient: HttpClient, maxResults: Int): List<Video> = coroutineScope {
		val resultGen = getResults(httpClient, this)
		val results = mutableListOf<Video>()
		try {
			while (results.size < maxResults) results.add(resultGen.receive())
		} catch (_: ClosedReceiveChannelException) {
			// ignored
		}
		finally { resultGen.cancel() }
		results
	}

	fun getResults(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<Video> = scope.produce {
		var page = 1
		var tempPageResults: List<YTVideoDetails>
		while (true) {
			tempPageResults = YTSearchResults.parse(httpClient.get(ytCanonicalURI(
				"/search_ajax",
				parametersOf(
					"style" to listOf("json"),
					"search_query" to listOf(query),
					"page" to listOf(page++.toString()),
					"hl" to listOf("en")
				)
			))).video
			if (tempPageResults.isEmpty()) break
			tempPageResults.forEach { this.send(Video.fromJSON(it)) }
		}
	}
}
