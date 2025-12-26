package org.nihongo.mochi.domain.kanji

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder

@Serializable
data class KanjiDetailsRoot(
    @SerialName("kanji_details")
    val kanjiDetails: KanjiDetailsWrapper = KanjiDetailsWrapper(emptyList())
)

@Serializable
data class KanjiDetailsWrapper(
    val kanji: List<KanjiEntry> = emptyList()
)

@Serializable
data class KanjiEntry(
    val id: String = "",
    val character: String = "",
    @SerialName("school_grade")
    val schoolGrade: String? = null,
    @SerialName("jlpt_level")
    val jlptLevel: String? = null,
    val frequency: String? = null,
    val strokes: String? = null,
    val readings: ReadingsWrapper? = null
)

@Serializable
data class ReadingsWrapper(
    @Serializable(with = ReadingEntryListSerializer::class)
    val reading: List<ReadingEntry> = emptyList()
)

@Serializable
data class ReadingEntry(
    val type: String = "",
    val frequency: String? = null,
    @SerialName("#text")
    val value: String = ""
)

object ReadingEntryListSerializer : KSerializer<List<ReadingEntry>> {
    private val listSerializer = ListSerializer(ReadingEntry.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<ReadingEntry>) {
        listSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<ReadingEntry> {
        val input = decoder as? JsonDecoder ?: throw IllegalStateException("This serializer can be used only with Json format")
        val element = input.decodeJsonElement()
        
        return if (element is JsonArray) {
            input.json.decodeFromJsonElement(listSerializer, element)
        } else if (element is JsonObject) {
            listOf(input.json.decodeFromJsonElement(ReadingEntry.serializer(), element))
        } else {
            // Case where it might be null or something else, return empty list or throw
            emptyList()
        }
    }
}
