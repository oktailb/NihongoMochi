package org.nihongo.mochi.domain.game

interface TextNormalizer {
    fun normalize(text: String): String
}
