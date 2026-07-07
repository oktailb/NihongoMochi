package org.nihongo.mochi.domain.services

class NoOpCloudSaveService : CloudSaveService {
    override suspend fun signIn(): Boolean = false
    override suspend fun isAuthenticated(): Boolean = false
    override suspend fun saveGame(name: String, data: String, description: String): Boolean = false
    override suspend fun loadGame(name: String): String? = null
    override fun submitScore(leaderboardId: String, score: Long) {}
    override fun unlockAchievement(achievementId: String) {}
    override fun showLeaderboards() {}
    override fun showAchievements() {}
    override fun showSavedGamesUI(title: String, allowAdd: Boolean, allowDelete: Boolean, maxSnapshots: Int) {}
    override suspend fun getPlayerInfo(): PlayerInfo? = null
    override var onSnapshotSelected: ((String?, Boolean) -> Unit)? = null
}
