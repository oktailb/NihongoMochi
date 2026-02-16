package org.nihongo.mochi.data

import org.nihongo.mochi.domain.services.CloudSaveService
import org.nihongo.mochi.ui.games.memorize.MemorizeGameResult
import org.nihongo.mochi.ui.games.simon.SimonGameResult
import org.nihongo.mochi.ui.games.taquin.TaquinGameResult
import org.nihongo.mochi.ui.games.kanadrop.KanaLinkResult
import org.nihongo.mochi.ui.games.crossword.CrosswordGameResult
import org.nihongo.mochi.ui.games.snake.SnakeGameResult
import org.nihongo.mochi.ui.games.shiritori.ShiritoriGameResult

interface ScoreRepository {
    fun setCloudSaveService(service: CloudSaveService?)

    fun saveScore(key: String, wasCorrect: Boolean, type: ScoreManager.ScoreType)
    fun getScore(key: String, type: ScoreManager.ScoreType): LearningScore
    fun getAllScores(type: ScoreManager.ScoreType): Map<String, LearningScore>
    
    // List management
    fun getListItems(listName: String): List<String>
    fun addItemToList(listName: String, itemKey: String)
    fun removeItemFromList(listName: String, itemKey: String)
    fun isInList(listName: String, itemKey: String): Boolean
    
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
    fun getMemorizeHistory(): List<MemorizeGameResult>
    
    fun saveSimonResult(result: SimonGameResult)
    fun getSimonHistory(): List<SimonGameResult>
    
    fun saveTaquinResult(result: TaquinGameResult)
    fun getTaquinHistory(): List<TaquinGameResult>
    
    fun saveKanaLinkResult(result: KanaLinkResult)
    fun getKanaLinkHistory(): List<KanaLinkResult>

    fun saveCrosswordResult(result: CrosswordGameResult)
    fun getCrosswordHistory(): List<CrosswordGameResult>

    fun saveSnakeResult(result: SnakeGameResult)
    fun getSnakeHistory(): List<SnakeGameResult>

    fun saveShiritoriResult(result: ShiritoriGameResult)
    fun getShiritoriHistory(): List<ShiritoriGameResult>
}
