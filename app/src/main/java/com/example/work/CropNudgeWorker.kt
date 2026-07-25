package com.example.work

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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.CropCalendar

/**
 * Proactive crop nudge (P2.6). Runs about once a day; when the farmer's crop enters a new growth
 * stage that day, it posts a notification with the action due — the retention mechanism that turns
 * a passive chatbot into an advisor the farmer opens each week. Fires only on stage transitions
 * (~4-5 times a season), so it never spams.
 */
class CropNudgeWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val profile = runCatching {
            AppDatabase.getDatabase(ctx).kisaanDao().getActiveProfileOnce()
        }.getOrNull() ?: return Result.success()

        if (profile.sowingDateMillis <= 0L) return Result.success()
        val stages = CropCalendar.stagesByCrop[profile.primaryCrop] ?: return Result.success()
        val days = CropCalendar.daysSinceSowing(profile.sowingDateMillis)

        // Only nudge on the day the crop enters a new stage.
        val startingStage = stages.firstOrNull { it.startDay == days && it.startDay > 0 }
            ?: return Result.success()

        val prefs = ctx.getSharedPreferences("kisaan_prefs", Context.MODE_PRIVATE)
        if (prefs.getInt("last_nudge_day", -1) == days) return Result.success()

        val cropUr = CROP_UR[profile.primaryCrop] ?: profile.primaryCrop
        showNudge(ctx, "کِسان دوست یاد دہانی", "$cropUr اب ${startingStage.nameUr} میں ہے — ${startingStage.actionUr}")
        prefs.edit().putInt("last_nudge_day", days).apply()
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "crop_nudges"
        const val UNIQUE_WORK = "crop_nudge_daily"
        private val CROP_UR = mapOf(
            "Wheat" to "گندم", "Rice" to "چاول", "Cotton" to "کپاس",
            "Sugarcane" to "گنا", "Maize" to "مکئی"
        )

        fun ensureChannel(ctx: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = ctx.getSystemService(NotificationManager::class.java)
                if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                    mgr.createNotificationChannel(
                        NotificationChannel(CHANNEL_ID, "فصل کی یاد دہانیاں", NotificationManager.IMPORTANCE_DEFAULT).apply {
                            description = "کھیت کے کاموں کی بروقت یاد دہانی"
                        }
                    )
                }
            }
        }

        private fun showNudge(ctx: Context, title: String, body: String) {
            ensureChannel(ctx)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return // No notification permission granted — skip silently.
            }

            val intent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(
                ctx, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()

            runCatching { NotificationManagerCompat.from(ctx).notify(1001, notif) }
        }
    }
}
