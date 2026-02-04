package com.lessapp.less.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.lessapp.less.MainActivity
import com.lessapp.less.R
import com.lessapp.less.data.model.Lang
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationService {

    private const val CHANNEL_ID = "less_daily_reminder"
    private const val NOTIFICATION_ID = 1001
    private const val WORK_TAG = "less_daily_reminder_work"

    // Create notification channel (required for Android 8+)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminder"
            val description = "Daily reminder to practice with Less"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Check if we have notification permission (Android 13+)
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Schedule daily notification at specified time
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int, lang: Lang) {
        // Cancel any existing work first
        cancelDailyReminder(context)

        // Calculate initial delay until next occurrence of the specified time
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        // Create work request
        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "lang" to lang.code
                )
            )
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )

        println("NotificationService: Scheduled daily reminder for $hour:${String.format("%02d", minute)}")
    }

    // Cancel daily reminder
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        println("NotificationService: Cancelled daily reminder")
    }

    // Show notification (called by worker)
    fun showNotification(context: Context, lang: Lang) {
        if (!hasNotificationPermission(context)) {
            println("NotificationService: No permission to show notification")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, body) = getNotificationContent(lang)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, notification)
                println("NotificationService: Notification shown")
            } catch (e: SecurityException) {
                println("NotificationService: SecurityException - no permission")
            }
        }
    }

    private fun getNotificationContent(lang: Lang): Pair<String, String> {
        return when (lang) {
            Lang.FR -> "Less" to "C'est l'heure de ton rituel Daily"
            Lang.EN -> "Less" to "Time for your Daily ritual"
            Lang.ES -> "Less" to "Es hora de tu ritual Daily"
        }
    }
}

// Worker class for daily notifications
class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val langCode = inputData.getString("lang") ?: "en"
        val lang = Lang.fromCode(langCode)

        NotificationService.showNotification(applicationContext, lang)
        return Result.success()
    }
}
