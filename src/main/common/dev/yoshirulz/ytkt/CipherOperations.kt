package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import io.ktor.client.request.get

typealias DecipherRoutine = List<DecipherOp>

fun DecipherRoutine.decipher(signature: String): String = fold(signature) { acc, cipherOp -> cipherOp.decipher(acc) }

class Decipherer {
	private val cache = mutableMapOf<String, DecipherRoutine>()

	suspend fun getDecipherRoutine(playerSourceUrl: String, httpClient: HttpClient): DecipherRoutine {
		cache[playerSourceUrl]?.let { return it } // check cache

		val fullScript = httpClient.get<String>(playerSourceUrl)
		val deciphererFuncBody = getDeciphererFuncBody(fullScript)
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
					genSliceDecipherOpPattern(escapedFuncName) matches deciphererDefinitionBody -> SliceDecipherOp(getDeciphererFuncIndex(statement))
					genSwapDecipherOpPattern(escapedFuncName) matches deciphererDefinitionBody -> SwapDecipherOp(getDeciphererFuncIndex(statement))
					genReverseDecipherOpPattern(escapedFuncName) matches deciphererDefinitionBody -> ReverseDecipherOp
					else -> null
				}
			}
			.filterNotNull() //TODO only take first three non-null?
			.toList()
		return operations.also { cache[playerSourceUrl] = it }
	}

	@Suppress("SpellCheckingInspection")
	companion object {
		private val DECIPHERER_FUNC_INDEX_PATTERN = Regex("""\(\w+,(\d+)\)""")

		private val DECIPHERER_FUNC_NAME_PATTERN_A = Regex("""(\w+)=function\(\w+\)\{(\w+)=2\.split\(\x22{2}\);.*?return\s+2\.join\(\x22{2}\)}""")

		private val DECIPHERER_FUNC_BODY_PATTERN_B = Regex("""\w+(?:.|\[)("\w+"|\w+)]?\(""")

		private fun genDeciphererFuncBodyPattern(funcName: String) = Regex("""(?!h\.)${Regex.escape(funcName)}=function\(\w+\)\{(.*?)}""")

		private fun genReverseDecipherOpPattern(escapedFuncName: String) = Regex("""$escapedFuncName:\bfunction\b\(\w+\)""")

		private fun genSliceDecipherOpPattern(escapedFuncName: String) = Regex("""$escapedFuncName:\bfunction\b\([a],b\).(\breturn\b)?.?\w+\.""")

		private fun genSwapDecipherOpPattern(escapedFuncName: String) = Regex("""$escapedFuncName:\bfunction\b\(\w+,\w\).\bvar\b.\bc=a\b""")

		private fun getDeciphererFuncBody(raw: String) = DECIPHERER_FUNC_NAME_PATTERN_A.find(raw)?.let { match ->
			match.groupValues[1].ifBlank { null }?.let { funcName ->
				genDeciphererFuncBodyPattern(Regex.escape(funcName)).find(raw)?.let {
					it.groupValues[1].ifBlank { null }
				} ?: throw UnrecognisedStructureException("body")
			}
		} ?: throw UnrecognisedStructureException("name")

		private fun getDeciphererFuncIndex(statement: String) = DECIPHERER_FUNC_INDEX_PATTERN.find(statement)!!.groupValues[1].toInt()
	}
}

interface DecipherOp {
	fun decipher(enciphered: String): String
}

object ReverseDecipherOp: DecipherOp {
	override fun decipher(enciphered: String) = enciphered.reversed()
	override fun toString() = "Reverse"
}

data class SliceDecipherOp(private val index: Int): DecipherOp {
	override fun decipher(enciphered: String) = enciphered.substring(index)
	override fun toString() = "Slice [$index]"
}

data class SwapDecipherOp(private val index: Int): DecipherOp {
	override fun decipher(enciphered: String) = enciphered.swapCharsAt(0, index)
	override fun toString() = "Swap [$index]"
}
