package org.nihongo.mochi.ui.game

// Progress for a single Kanji in a game session
data class KanjiProgress(
    var normalSolved: Boolean = false, 
    var reverseSolved: Boolean = false,
    var meaningSolved: Boolean = false, 
    var readingSolved: Boolean = false
)
