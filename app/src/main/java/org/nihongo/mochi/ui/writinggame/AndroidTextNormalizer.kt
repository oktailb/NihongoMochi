package org.nihongo.mochi.ui.writinggame

import org.nihongo.mochi.domain.game.TextNormalizer
import java.text.Normalizer
import java.util.Locale

class AndroidTextNormalizer : TextNormalizer {
    override fun normalize(text: String): String {
        // Normalize Unicode characters (accents, etc.) to base form
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        // Remove diacritical marks (accents)
        val withoutAccents = normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        // Return lowercase
        return withoutAccents.lowercase(Locale.getDefault())
    }
}
