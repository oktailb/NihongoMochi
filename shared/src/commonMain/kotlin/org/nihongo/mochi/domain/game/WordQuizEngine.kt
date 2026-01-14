package org.nihongo.mochi.domain.game

import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.Word

class WordQuizEngine {
    var isGameInitialized = false
    var allWords = mutableListOf<Word>()
    var currentWordSet = mutableListOf<Word>()
    var revisionList = mutableListOf<Word>()
    val wordStatus = mutableMapOf<Word, GameStatus>()
    var wordListPosition = 0
    var currentWord: Word? = null
    var currentAnswers = listOf<String>()
}
