package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import org.w3c.dom.XMLDocument
import org.w3c.xhr.DOCUMENT
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

fun String.addProxyPrefix() = "https://cors-anywhere.herokuapp.com/$this"

actual class HTMLPage(val document: Document)

actual class XMLPage(val xmlDocument: XMLDocument)

@UseExperimental(ExperimentalCoroutinesApi::class)
actual fun requestPage(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage> = scope.produce {
	XMLHttpRequest().run {
		this.addEventListener(
			"load",
			{
				scope.launch {
					this@produce.send(HTMLPage(this@run.responseXML!!))
					this@produce.cancel()
				}
			}
		)
		this.open("GET", uri)
		this.responseType = XMLHttpRequestResponseType.DOCUMENT
		this.send()
	}
	@Suppress("ControlFlowWithEmptyBody")
	while (true); //TODO necessary?
}

actual fun requestPageProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage> = requestPage(uri.addProxyPrefix(), httpClient, scope)

@UseExperimental(ExperimentalCoroutinesApi::class)
actual fun requestPlaintext(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String> = scope.produce {
	XMLHttpRequest().run {
		this.addEventListener(
			"load",
			{
				scope.launch {
					this@produce.send(this@run.responseText)
					this@produce.cancel()
				}
			}
		)
		this.open("GET", uri)
		this.send()
	}
	@Suppress("ControlFlowWithEmptyBody")
	while (true); //TODO necessary?
}

actual fun requestPlaintextProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String> = requestPlaintext(uri.addProxyPrefix(), httpClient, scope)

actual fun requestXML(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage> = TODO()

actual fun requestXMLProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage> = requestXML(uri.addProxyPrefix(), httpClient, scope)

actual fun URI.requestPage(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage> = requestPage(this.toString(), httpClient, scope) // not using Ktor so there's no benefit

actual fun URI.requestPageProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage> = requestPageProxied(this.toString(), httpClient, scope) // not using Ktor so there's no benefit

actual fun URI.requestPlaintext(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String> = requestPlaintext(this.toString(), httpClient, scope) // not using Ktor so there's no benefit

actual fun URI.requestPlaintextProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String> = requestPlaintextProxied(this.toString(), httpClient, scope) // not using Ktor so there's no benefit

actual fun URI.requestXML(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage> = requestXML(this.toString(), httpClient, scope) // not using Ktor so there's no benefit

actual fun URI.requestXMLProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage> = requestXMLProxied(this.toString(), httpClient, scope) // not using Ktor so there's no benefit
