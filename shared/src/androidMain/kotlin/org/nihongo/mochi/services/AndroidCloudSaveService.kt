package org.nihongo.mochi.services

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.tasks.Task
import org.nihongo.mochi.domain.services.CloudSaveService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AndroidCloudSaveService(private val activity: Activity) : CloudSaveService {

    private val gamesSignInClient: GamesSignInClient by lazy { PlayGames.getGamesSignInClient(activity) }
    private val snapshotsClient: SnapshotsClient by lazy { PlayGames.getSnapshotsClient(activity) }
    private val achievementsClient by lazy { PlayGames.getAchievementsClient(activity) }
    private val leaderboardsClient by lazy { PlayGames.getLeaderboardsClient(activity) }

    companion object {
        private const val RC_LEADERBOARDS = 9002
    }

    override suspend fun signIn(): Boolean {
        return try {
            val result = gamesSignInClient.signIn().await()
            result.isAuthenticated
        } catch (e: Exception) {
            Log.e("AndroidCloudSave", "Sign in failed", e)
            false
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return try {
            val result = gamesSignInClient.isAuthenticated.await()
            result.isAuthenticated
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun saveGame(name: String, data: String, description: String): Boolean {
        return try {
            val openResult = snapshotsClient.open(name, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED).await()
            val snapshot = openResult.data ?: return false
            
            snapshot.snapshotContents.writeBytes(data.toByteArray())
            
            val metadataChange = SnapshotMetadataChange.Builder()
                .setDescription(description)
                .build()
            
            snapshotsClient.commitAndClose(snapshot, metadataChange).await()
            true
        } catch (e: Exception) {
            Log.e("AndroidCloudSave", "Save game failed", e)
            false
        }
    }

    override suspend fun loadGame(name: String): String? {
        return try {
            val openResult = snapshotsClient.open(name, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED).await()
            val snapshot = openResult.data ?: return null
            
            val data = snapshot.snapshotContents.readFully()
            String(data)
        } catch (e: Exception) {
            Log.e("AndroidCloudSave", "Load game failed", e)
            null
        }
    }

    override fun submitScore(leaderboardId: String, score: Long) {
        leaderboardsClient.submitScore(leaderboardId, score)
    }

    override fun unlockAchievement(achievementId: String) {
        achievementsClient.unlock(achievementId)
    }

    override fun showLeaderboards() {
        leaderboardsClient.allLeaderboardsIntent.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, RC_LEADERBOARDS)
        }
    }
    
    suspend fun getAchievementsIntent(): Intent {
        return achievementsClient.achievementsIntent.await()
    }

    suspend fun getLeaderboardsIntent(): Intent {
        return leaderboardsClient.allLeaderboardsIntent.await()
    }
    
    suspend fun getSavedGamesIntent(title: String, allowAdd: Boolean, allowDelete: Boolean, maxSnapshots: Int): Intent {
        return snapshotsClient.getSelectSnapshotIntent(title, allowAdd, allowDelete, maxSnapshots).await()
    }

    private suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }
}
