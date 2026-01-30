package org.nihongo.mochi.domain.services

data class PlayerInfo(val displayName: String, val iconUri: String?)

interface CloudSaveService {
    suspend fun signIn(): Boolean
    suspend fun isAuthenticated(): Boolean
    suspend fun saveGame(name: String, data: String, description: String): Boolean
    suspend fun loadGame(name: String): String?
    fun submitScore(leaderboardId: String, score: Long)
    fun unlockAchievement(achievementId: String)
    fun showLeaderboards()
    suspend fun getPlayerInfo(): PlayerInfo?
}
