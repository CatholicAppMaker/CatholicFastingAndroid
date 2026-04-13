package com.kevpierce.catholicfastingapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDER_CHANNEL_ID = "fasting_reminders"

    fun ensureReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel =
            NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Fasting reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Required-day and support reminders"
            }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
