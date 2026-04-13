package com.kevpierce.catholicfastingapp.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfastingapp.R
import com.kevpierce.catholicfastingapp.navigation.AppNavigationIntents
import java.time.Duration
import java.time.Instant

object IntermittentFastNotificationManager {
    private const val CHANNEL_ID = "intermittent_fast"
    private const val NOTIFICATION_ID = 2001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_fast_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.notification_fast_channel_description)
            }

        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun syncActiveFast(
        context: Context,
        startIso: String?,
        targetHours: Int,
    ) {
        if (startIso == null) {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            return
        }
        if (!canPostNotifications(context)) {
            return
        }

        val start = runCatching { Instant.parse(startIso) }.getOrNull()
        val elapsed =
            start?.let {
                val duration = Duration.between(it, Instant.now())
                context.getString(
                    R.string.notification_fast_elapsed_value,
                    duration.toHours(),
                    duration.minusHours(duration.toHours()).toMinutes(),
                )
            } ?: context.getString(R.string.notification_fast_in_progress)

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(context.getString(R.string.notification_fast_title))
                .setContentText(
                    context.getString(
                        R.string.notification_fast_target_value,
                        elapsed,
                        targetHours,
                    ),
                )
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.notification_fast_body),
                    ),
                ).setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(
                    AppNavigationIntents.activityPendingIntent(
                        context = context,
                        deepLink = AppDeepLinks.TRACKER,
                        requestCode = 2001,
                    ),
                ).addAction(
                    0,
                    context.getString(R.string.notification_fast_action_end),
                    AppNavigationIntents.endFastPendingIntent(context),
                ).addAction(
                    0,
                    context.getString(R.string.notification_fast_action_open),
                    AppNavigationIntents.activityPendingIntent(
                        context = context,
                        deepLink = AppDeepLinks.TRACKER,
                        requestCode = 2002,
                    ),
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .build()

        notifySafely(context, notification)
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

    @SuppressLint("MissingPermission")
    private fun notifySafely(
        context: Context,
        notification: android.app.Notification,
    ) {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
