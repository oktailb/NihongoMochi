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
import org.nihongo.mochi.data.ScoreManager

class DecayWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val decayed = ScoreManager.decayScores()
        
        if (decayed) {
            sendNotification()
        }
        
        return Result.success()
    }

    private fun sendNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mochi_decay_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mochi Learning Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to keep your Mochi fresh!"
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
            "Don't let your Mochi dry out! 🍡 Time for a quick review?",
            "Your Kanji missed you! Come back to refresh your memory! ✨",
            "A fresh Mochi is a happy Mochi! Let's practice! 🍵",
            "Mochi-Mochi! It's time to stretch your brain! 🧠"
        )
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle("Nihongo Mochi")
            .setContentText(messages.random())
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
