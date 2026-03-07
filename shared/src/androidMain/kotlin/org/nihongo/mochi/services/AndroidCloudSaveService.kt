package org.nihongo.mochi.services

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.tasks.Task
import org.nihongo.mochi.domain.services.CloudSaveService
import org.nihongo.mochi.domain.services.PlayerInfo
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AndroidCloudSaveService(private val activity: Activity) : CloudSaveService {

    private val gamesSignInClient: GamesSignInClient by lazy { PlayGames.getGamesSignInClient(activity) }
    private val snapshotsClient: SnapshotsClient by lazy { PlayGames.getSnapshotsClient(activity) }
    private val achievementsClient by lazy { PlayGames.getAchievementsClient(activity) }
    private val leaderboardsClient by lazy { PlayGames.getLeaderboardsClient(activity) }
    private val playersClient by lazy { PlayGames.getPlayersClient(activity) }
    private val imageManager by lazy { ImageManager.create(activity) }

    override var onSnapshotSelected: ((snapshotName: String?, isNew: Boolean) -> Unit)? = null

    companion object {
        const val RC_LEADERBOARDS = 9002
        const val RC_SAVED_GAMES = 9003
        const val RC_ACHIEVEMENTS = 9004
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

    override fun showAchievements() {
        achievementsClient.achievementsIntent.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, RC_ACHIEVEMENTS)
        }
    }

    override fun showSavedGamesUI(title: String, allowAdd: Boolean, allowDelete: Boolean, maxSnapshots: Int) {
        snapshotsClient.getSelectSnapshotIntent(title, allowAdd, allowDelete, maxSnapshots)
            .addOnSuccessListener { intent ->
                activity.startActivityForResult(intent, RC_SAVED_GAMES)
            }
    }

    override suspend fun getPlayerInfo(): PlayerInfo? {
        return try {
            val player = playersClient.currentPlayer.await()
            val imageUri = player.hiResImageUri ?: player.iconImageUri
            
            var avatarBytes: ByteArray? = null
            if (imageUri != null) {
                avatarBytes = suspendCoroutine { continuation ->
                    imageManager.loadImage({ uri, drawable, isRequested ->
                        try {
                            if (drawable is BitmapDrawable) {
                                val bitmap = drawable.bitmap
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                continuation.resume(stream.toByteArray())
                            } else {
                                continuation.resume(null)
                            }
                        } catch (e: Exception) {
                            Log.e("AndroidCloudSave", "Error converting avatar", e)
                            continuation.resume(null)
                        }
                    }, imageUri)
                }
            }

            PlayerInfo(
                displayName = player.displayName,
                iconUri = imageUri?.toString(),
                avatarBytes = avatarBytes
            )
        } catch (e: Exception) {
            Log.e("AndroidCloudSave", "Failed to get player info", e)
            null
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
