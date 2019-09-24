package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import io.ktor.client.request.get

typealias DecipherOp = (String) -> String

typealias DecipherRoutine = List<DecipherOp>

@Suppress("SpellCheckingInspection")
class Decipherer {
	private val cache = mutableMapOf<String, DecipherRoutine>()

	suspend fun decipher(signature: String, playerSourceUrl: String, httpClient: HttpClient) =
		(cache[playerSourceUrl] ?: getDecipherRoutine(playerSourceUrl, httpClient)).fold(signature) { acc, cipherOp -> cipherOp(acc) }

	private suspend fun getDecipherRoutine(playerSourceUrl: String, httpClient: HttpClient): DecipherRoutine {
		inline fun genDeciphererFuncBodyPattern(funcName: String) = Regex("""(?!h\.)${Regex.escape(funcName)}=function\(\w+\)\{(.*?)}""")
		inline fun genReverseDecipherOpPattern(escapedFuncName: String) = Regex("""$escapedFuncName:\bfunction\b\(\w+\)""")
		inline fun genSliceDecipherOpPattern(escapedFuncName: String) = Regex("""$escapedFuncName:\bfunction\b\([a],b\).(\breturn\b)?.?\w+\.""")
		inline fun genSwapDecipherOpPattern(escapedFuncName: String) = Regex("""$escapedFuncName:\bfunction\b\(\w+,\w\).\bvar\b.\bc=a\b""")

		inline fun getDeciphererFuncArg(statement: String) = DECIPHERER_FUNC_INDEX_PATTERN.find(statement)!!.groupValues[1].toInt()

		val fullScript = httpClient.get<String>(playerSourceUrl)
		val deciphererFuncBody = DECIPHERER_FUNC_NAME_PATTERN_A.find(fullScript)?.let { match ->
			match.groupValues[1].ifBlank { null }?.let { funcName ->
				genDeciphererFuncBodyPattern(Regex.escape(funcName)).find(fullScript)?.let {
					it.groupValues[1].ifBlank { null }
				} ?: throw UnrecognisedStructureException("body")
			}
		} ?: throw UnrecognisedStructureException("name")
		val deciphererDefinitionName = Regex("""(\w+).\w+\(\w+,\d+\);""").find(deciphererFuncBody)!!.groupValues[1] //TODO WTF
		val deciphererDefinitionBody = Regex("""var\s+${Regex.escape(deciphererDefinitionName)}=\{(\w+:function\(\w+(,\w+)?\)\{(.*?)}),?};""").find(fullScript)!!.groupValues[0]
		val operations = deciphererFuncBody.splitToSequence(';')
			.map { statement ->
				DECIPHERER_FUNC_BODY_PATTERN_B.find(statement)?.groupValues?.get(1)?.takeUnless { it.isEmpty() }?.let { Pair(statement, Regex.escape(it)) }
			}
			.filterNotNull()
			.map { (statement, escapedFuncName) ->
				when {
					//TODO is execution order important here?
					genSliceDecipherOpPattern(escapedFuncName) matches deciphererDefinitionBody -> { e: String -> e.substring(getDeciphererFuncArg(statement)) }
					genSwapDecipherOpPattern(escapedFuncName) matches deciphererDefinitionBody -> { e: String -> e.swapCharsAt(0, getDeciphererFuncArg(statement)) }
					genReverseDecipherOpPattern(escapedFuncName) matches deciphererDefinitionBody -> REVERSE_OP
					else -> null
				}
			}
			.filterNotNull() //TODO only take first three non-null?
			.toList()
		return operations.also { cache[playerSourceUrl] = it }
	}

	companion object {
		private val DECIPHERER_FUNC_INDEX_PATTERN = Regex("""\(\w+,(\d+)\)""")

		private val DECIPHERER_FUNC_NAME_PATTERN_A = Regex("""(\w+)=function\(\w+\)\{(\w+)=2\.split\(\x22{2}\);.*?return\s+2\.join\(\x22{2}\)}""")

		private val DECIPHERER_FUNC_BODY_PATTERN_B = Regex("""\w+(?:.|\[)("\w+"|\w+)]?\(""")

		private val REVERSE_OP: DecipherOp = String::reversed
	}
}
