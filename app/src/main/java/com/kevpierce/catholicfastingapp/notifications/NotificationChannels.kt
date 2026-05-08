package com.kevpierce.catholicfastingapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.kevpierce.catholicfastingapp.R

object NotificationChannels {
    const val REMINDER_CHANNEL_ID = "fasting_reminders"

    fun ensureReminderChannel(context: Context) {
        val channel =
            NotificationChannel(
                REMINDER_CHANNEL_ID,
                context.getString(R.string.notification_channel_reminder_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_reminder_description)
            }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
