package org.nihongo.mochi.domain.kanji

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

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
    val readings: ReadingsWrapper? = null,
    val components: ComponentsWrapper? = null
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

@Serializable
data class ComponentsWrapper(
    val structure: String? = null,
    @Serializable(with = ComponentEntryListSerializer::class)
    val component: List<ComponentEntry> = emptyList()
)

@Serializable
data class ComponentEntry(
    @SerialName("kanji_ref")
    val kanjiRef: String? = null,
    @SerialName("#text")
    val text: String? = null
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

object ComponentEntryListSerializer : KSerializer<List<ComponentEntry>> {
    private val listSerializer = ListSerializer(ComponentEntry.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<ComponentEntry>) {
        listSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<ComponentEntry> {
        val input = decoder as? JsonDecoder ?: throw IllegalStateException("This serializer can be used only with Json format")
        val element = input.decodeJsonElement()

        return if (element is JsonArray) {
            input.json.decodeFromJsonElement(listSerializer, element)
        } else if (element is JsonObject) {
            listOf(input.json.decodeFromJsonElement(ComponentEntry.serializer(), element))
        } else {
            emptyList()
        }
    }
}
