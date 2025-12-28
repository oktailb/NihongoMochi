package org.nihongo.mochi.ui.wordquiz

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.domain.game.WordQuizEngine
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.Word

class WordQuizViewModel : ViewModel() {
    
    private val engine = WordQuizEngine()
    
    // Delegate properties to Engine
    var isGameInitialized: Boolean
        get() = engine.isGameInitialized
        set(value) { engine.isGameInitialized = value }

    var allWords: MutableList<Word>
        get() = engine.allWords
        set(value) { engine.allWords = value }

    var currentWordSet: MutableList<Word>
        get() = engine.currentWordSet
        set(value) { engine.currentWordSet = value }

    var revisionList: MutableList<Word>
        get() = engine.revisionList
        set(value) { engine.revisionList = value }

    val wordStatus: MutableMap<Word, GameStatus>
        get() = engine.wordStatus

    var wordListPosition: Int
        get() = engine.wordListPosition
        set(value) { engine.wordListPosition = value }

    var currentWord: Word
        get() = engine.currentWord
        set(value) { engine.currentWord = value }

    var currentAnswers: List<String>
        get() = engine.currentAnswers
        set(value) { engine.currentAnswers = value }

    // UI Specific State
    var areButtonsEnabled = true
    var buttonColors = mutableListOf<Int>() // Store color resource IDs
}
