package org.nihongo.mochi.domain.services

data class PlayerInfo(
    val displayName: String, 
    val iconUri: String?,
    val avatarBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PlayerInfo
        if (displayName != other.displayName) return false
        if (iconUri != other.iconUri) return false
        if (avatarBytes != null) {
            if (other.avatarBytes == null) return false
            if (!avatarBytes.contentEquals(other.avatarBytes)) return false
        } else if (other.avatarBytes != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = displayName.hashCode()
        result = 31 * result + (iconUri?.hashCode() ?: 0)
        result = 31 * result + (avatarBytes?.contentHashCode() ?: 0)
        return result
    }
}

interface CloudSaveService {
    suspend fun signIn(): Boolean
    suspend fun isAuthenticated(): Boolean
    suspend fun saveGame(name: String, data: String, description: String): Boolean
    suspend fun loadGame(name: String): String?
    fun submitScore(leaderboardId: String, score: Long)
    fun unlockAchievement(achievementId: String)
    fun showLeaderboards()
    fun showAchievements()
    fun showSavedGamesUI(title: String, allowAdd: Boolean, allowDelete: Boolean, maxSnapshots: Int)
    suspend fun getPlayerInfo(): PlayerInfo?
    
    var onSnapshotSelected: ((snapshotName: String?, isNew: Boolean) -> Unit)?
}
