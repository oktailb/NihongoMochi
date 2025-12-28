package org.nihongo.mochi.domain.util

object TextUtils {
    fun isKanji(c: Char): Boolean {
        return c in '\u4e00'..'\u9faf' || c in '\u3400'..'\u4dbf'
    }

    fun isKana(c: Char): Boolean {
        return c in '\u3040'..'\u309F' || c in '\u30A0'..'\u30FF'
    }
    
    fun containsKanji(text: String): Boolean = text.any { isKanji(it) }
    
    fun containsKana(text: String): Boolean = text.any { isKana(it) }
    
    fun kanjiCount(text: String): Int = text.count { isKanji(it) }
}
