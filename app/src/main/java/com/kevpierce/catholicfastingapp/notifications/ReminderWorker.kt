package com.kevpierce.catholicfastingapp.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfastingapp.R
import com.kevpierce.catholicfastingapp.navigation.AppNavigationIntents

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (!canPostNotifications(applicationContext)) {
            return Result.success()
        }

        val title = inputData.getString(KEY_TITLE) ?: "Catholic fasting reminder"
        val body = inputData.getString(KEY_BODY) ?: "Open the app for today’s observance plan."

        notifyReminder(
            notificationId = title.hashCode(),
            notification =
                NotificationCompat.Builder(applicationContext, NotificationChannels.REMINDER_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setContentIntent(
                        AppNavigationIntents.activityPendingIntent(
                            context = applicationContext,
                            deepLink = AppDeepLinks.TODAY,
                            requestCode = title.hashCode(),
                        ),
                    )
                    .addAction(
                        0,
                        applicationContext.getString(R.string.notification_reminder_action_open),
                        AppNavigationIntents.activityPendingIntent(
                            context = applicationContext,
                            deepLink = AppDeepLinks.TODAY,
                            requestCode = title.hashCode() + 1,
                        ),
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build(),
        )
        return Result.success()
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }

    @SuppressLint("MissingPermission")
    private fun notifyReminder(
        notificationId: Int,
        notification: android.app.Notification,
    ) {
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }
}
