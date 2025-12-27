package org.nihongo.mochi.domain.meaning

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class MeaningRoot(
    val meanings: Meanings
)

@Serializable
data class Meanings(
    @SerialName("@locale") val locale: String,
    val kanji: List<KanjiMeaning>
)

@Serializable
data class KanjiMeaning(
    @SerialName("@id") val id: String,
    @Serializable(with = MeaningEntryListSerializer::class)
    val meaning: List<String> = emptyList()
)

object MeaningEntryListSerializer : KSerializer<List<String>> {
    private val listSerializer = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<String>) {
        listSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val input = decoder as? JsonDecoder ?: throw IllegalStateException("This serializer can be used only with Json format")
        val element = input.decodeJsonElement()

        return if (element is JsonArray) {
            element.mapNotNull { (it as? JsonPrimitive)?.content }
        } else if (element is JsonPrimitive) { // It's a single string
            listOf(element.content)
        } else {
            emptyList()
        }
    }
}