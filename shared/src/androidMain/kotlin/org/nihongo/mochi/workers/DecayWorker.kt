package org.nihongo.mochi.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.shared.generated.resources.*
import kotlinx.coroutines.runBlocking

class DecayWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    private val scoreRepository: ScoreRepository by inject()

    override fun doWork(): Result {
        val decayed = scoreRepository.decayScores()
        
        if (decayed) {
            runBlocking {
                sendNotification()
            }
        }
        
        return Result.success()
    }

    private suspend fun sendNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mochi_decay_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(Res.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(Res.string.notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Use launch intent to avoid hard dependency on :app:MainActivity
        val launchIntent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent: PendingIntent = if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            PendingIntent.getActivity(applicationContext, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            // Fallback (might not work well if MainActivity is not found, but it's a safety)
            PendingIntent.getActivity(applicationContext, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
        }

        val messages = listOf(
            Res.string.notification_msg_1,
            Res.string.notification_msg_2,
            Res.string.notification_msg_3,
            Res.string.notification_msg_4
        )
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(getString(Res.string.notification_title))
            .setContentText(getString(messages.random()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        notificationManager.notify(1, notification)
    }
}
