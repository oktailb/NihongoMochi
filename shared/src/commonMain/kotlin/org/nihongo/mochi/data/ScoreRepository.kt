package org.nihongo.mochi.data

interface ScoreRepository {
    fun saveScore(key: String, wasCorrect: Boolean, type: ScoreManager.ScoreType)
    fun getScore(key: String, type: ScoreManager.ScoreType): LearningScore
    fun getAllScores(type: ScoreManager.ScoreType): Map<String, LearningScore>
    
    /**
     * Applies decay to scores that haven't been reviewed for a while.
     * @return true if any score was decayed/updated, false otherwise.
     */
    fun decayScores(): Boolean
    
    // Backup/Restore
    fun getAllDataJson(): String
    fun restoreDataFromJson(json: String)

    // History methods
    fun saveMemorizeHistory(historyJson: String)
    fun getMemorizeHistory(): String
    fun saveSimonHistory(historyJson: String)
    fun getSimonHistory(): String
    fun saveTaquinHistory(historyJson: String)
    fun getTaquinHistory(): String
    fun saveKanaLinkHistory(historyJson: String)
    fun getKanaLinkHistory(): String
}
