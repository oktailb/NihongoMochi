package org.nihongo.mochi.domain.meaning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordMeaningEntry(
    @SerialName("@id") val id: String,
    val meaning: String
)

@Serializable
data class WordMeaningContent(
    @SerialName("@locale") val locale: String,
    val entries: List<WordMeaningEntry> = emptyList()
)

@Serializable
data class WordMeaningRoot(
    val word_meanings: WordMeaningContent
)
