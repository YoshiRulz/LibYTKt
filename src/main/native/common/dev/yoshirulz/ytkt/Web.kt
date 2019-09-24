@file:Suppress("NOTHING_TO_INLINE")

package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch

private inline fun noTODO(): Nothing = throw NotImplementedError()

private inline fun HttpClient.getAndParseHTML(uri: String): HTMLPage = noTODO()

private inline fun HttpClient.getAndParseHTML(uri: URI): HTMLPage = noTODO()

private inline fun HttpClient.getAndParseXML(uri: String): XMLPage = noTODO()

private inline fun HttpClient.getAndParseXML(uri: URI): XMLPage = noTODO()

actual class HTMLPage

actual class XMLPage

@UseExperimental(ExperimentalCoroutinesApi::class)
actual fun requestPage(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage> = scope.produce {
	scope.launch {
		this@produce.send(httpClient.getAndParseHTML(uri))
		this@produce.close()
	}
	@Suppress("ControlFlowWithEmptyBody")
	while (true); //TODO necessary?
}

actual fun requestPageProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage> = requestPage(uri, httpClient, scope) // proxy not necessary w/ curl

@UseExperimental(ExperimentalCoroutinesApi::class)
actual fun requestPlaintext(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String> = scope.produce {
	scope.launch {
		this@produce.send(httpClient.get<String>(uri))
		this@produce.close()
	}
	@Suppress("ControlFlowWithEmptyBody")
	while (true); //TODO necessary?
}

actual fun requestPlaintextProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String> = requestPlaintext(uri, httpClient, scope) // proxy not necessary w/ curl

@UseExperimental(ExperimentalCoroutinesApi::class)
actual fun requestXML(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage> = scope.produce {
	scope.launch {
		this@produce.send(httpClient.getAndParseXML(uri))
		this@produce.close()
	}
	@Suppress("ControlFlowWithEmptyBody")
	while (true); //TODO necessary?
}

actual fun requestXMLProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage> = requestXML(uri, httpClient, scope) // proxy not necessary w/ curl

@UseExperimental(ExperimentalCoroutinesApi::class)
actual fun URI.requestPage(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage> = scope.produce {
	scope.launch {
		this@produce.send(httpClient.getAndParseHTML(this@requestPage))
		this@produce.close()
	}
	@Suppress("ControlFlowWithEmptyBody")
	while (true); //TODO necessary?
}

actual fun URI.requestPageProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage> = this.requestPage(httpClient, scope) // proxy not necessary w/ curl

@UseExperimental(ExperimentalCoroutinesApi::class)
actual fun URI.requestPlaintext(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String> = scope.produce {
	scope.launch {
		this@produce.send(httpClient.get<String>(this@requestPlaintext))
		this@produce.close()
	}
	@Suppress("ControlFlowWithEmptyBody")
	while (true); //TODO necessary?
}

actual fun URI.requestPlaintextProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String> = this.requestPlaintext(httpClient, scope) // proxy not necessary w/ curl

@UseExperimental(ExperimentalCoroutinesApi::class)
actual fun URI.requestXML(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage> = scope.produce {
	scope.launch {
		this@produce.send(httpClient.getAndParseXML(this@requestXML))
		this@produce.close()
	}
	@Suppress("ControlFlowWithEmptyBody")
	while (true); //TODO necessary?
}

actual fun URI.requestXMLProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage> = this.requestXML(httpClient, scope) // proxy not necessary w/ curl
