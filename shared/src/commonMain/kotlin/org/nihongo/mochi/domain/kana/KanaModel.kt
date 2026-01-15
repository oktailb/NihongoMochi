package org.nihongo.mochi.domain.kana

import kotlinx.serialization.Serializable

@Serializable
data class KanaEntry(
    val character: String,
    val romaji: String,
    val type: KanaType,
    val line: Int = 0,
    val column: Int = 0
) {
    val category: String
        get() = when {
            line <= 11 -> "gojuon"
            line <= 16 -> if (romaji.startsWith("p")) "handakuon" else "dakuon"
            else -> "yoon"
        }
}

enum class KanaType {
    HIRAGANA,
    KATAKANA
}

@Serializable
data class KanaData(
    val characters: List<KanaEntry>
)

@Serializable
data class NumberEntry(
    val character: String, 
    val romaji: String, 
    val value: Int
)

@Serializable
data class NumberData(
    val numbers: List<NumberEntry>
)
