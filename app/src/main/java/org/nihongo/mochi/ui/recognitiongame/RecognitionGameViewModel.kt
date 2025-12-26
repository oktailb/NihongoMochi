package org.nihongo.mochi.ui.recognitiongame

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.ui.game.GameStatus
import org.nihongo.mochi.ui.game.KanjiDetail
import org.nihongo.mochi.ui.game.KanjiProgress
import org.nihongo.mochi.ui.game.Reading

enum class QuestionDirection { NORMAL, REVERSE }

class RecognitionGameViewModel : ViewModel() {
    var isGameInitialized = false
    
    val allKanjiDetailsXml = mutableListOf<KanjiDetail>()
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    var currentKanjiSet = mutableListOf<KanjiDetail>()
    var revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    var kanjiListPosition = 0
    lateinit var currentKanji: KanjiDetail
    lateinit var correctAnswer: String
    lateinit var gameMode: String
    lateinit var readingMode: String
    var currentDirection: QuestionDirection = QuestionDirection.NORMAL
    
    // UI State
    var currentAnswers = listOf<String>()
    var areButtonsEnabled = true
    var buttonColors = mutableListOf<Int>() // Will store color resource IDs or similar state
    
    fun resetState() {
        isGameInitialized = false
        allKanjiDetailsXml.clear()
        allKanjiDetails.clear()
        currentKanjiSet.clear()
        revisionList.clear()
        kanjiStatus.clear()
        kanjiProgress.clear()
        kanjiListPosition = 0
        currentAnswers = emptyList()
        areButtonsEnabled = true
        buttonColors.clear()
    }
}