package org.nihongo.mochi.ui.writinggame

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress
enum class QuestionType { MEANING, READING }

class WritingGameViewModel : ViewModel() {
    var isGameInitialized = false
    
    val allKanjiDetails = mutableListOf<KanjiDetail>()
    val allKanjiDetailsXml = mutableListOf<KanjiDetail>()
    
    var kanjiListPosition = 0
    val currentKanjiSet = mutableListOf<KanjiDetail>()
    val revisionList = mutableListOf<KanjiDetail>()
    val kanjiStatus = mutableMapOf<KanjiDetail, GameStatus>()
    val kanjiProgress = mutableMapOf<KanjiDetail, KanjiProgress>()
    
    lateinit var currentKanji: KanjiDetail
    var currentQuestionType: QuestionType = QuestionType.MEANING
    var correctAnswer: String = ""
    
    // UI State for Feedback
    var isAnswerProcessing = false
    var lastAnswerStatus: Boolean? = null
    var showCorrectionFeedback = false
    var correctionDelayPending = false
}
