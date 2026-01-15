package org.nihongo.mochi.data

import org.nihongo.mochi.ui.games.memorize.MemorizeGameResult
import org.nihongo.mochi.ui.games.simon.SimonGameResult
import org.nihongo.mochi.ui.games.taquin.TaquinGameResult
import org.nihongo.mochi.ui.games.kanadrop.KanaLinkResult
import org.nihongo.mochi.ui.games.crossword.CrosswordGameResult

interface ScoreRepository {
    fun saveScore(key: String, wasCorrect: Boolean, type: ScoreManager.ScoreType)
    fun getScore(key: String, type: ScoreManager.ScoreType): LearningScore
    fun getAllScores(type: ScoreManager.ScoreType): Map<String, LearningScore>
    
    // List management
    fun getListItems(listName: String): List<String>
    
    /**
     * Applies decay to scores that haven't been reviewed for a while.
     * @return true if any score was decayed/updated, false otherwise.
     */
    fun decayScores(): Boolean
    
    // Backup/Restore
    fun getAllDataJson(): String
    fun restoreDataFromJson(json: String)

    // History methods
    fun saveMemorizeResult(result: MemorizeGameResult)
    fun getMemorizeHistory(): String
    
    fun saveSimonResult(result: SimonGameResult)
    fun getSimonHistory(): String
    
    fun saveTaquinResult(result: TaquinGameResult)
    fun getTaquinHistory(): String
    
    fun saveKanaLinkResult(result: KanaLinkResult)
    fun getKanaLinkHistory(): String

    fun saveCrosswordResult(result: CrosswordGameResult)
    fun getCrosswordHistory(): String

    // Legacy (to be removed once migrated)
    @Deprecated("Use saveMemorizeResult")
    fun saveMemorizeHistory(historyJson: String) {}
    @Deprecated("Use saveSimonResult")
    fun saveSimonHistory(historyJson: String) {}
    @Deprecated("Use saveTaquinResult")
    fun saveTaquinHistory(historyJson: String) {}
    @Deprecated("Use saveKanaLinkResult")
    fun saveKanaLinkHistory(historyJson: String) {}
}
