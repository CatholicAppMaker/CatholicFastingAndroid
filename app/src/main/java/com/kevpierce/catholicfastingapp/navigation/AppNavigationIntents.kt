package com.kevpierce.catholicfastingapp.navigation

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kevpierce.catholicfastingapp.MainActivity
import com.kevpierce.catholicfastingapp.notifications.NotificationActionReceiver

object AppNavigationIntents {
    fun activityIntent(
        context: Context,
        deepLink: String,
    ): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(deepLink), context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

    fun activityPendingIntent(
        context: Context,
        deepLink: String,
        requestCode: Int,
    ): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            activityIntent(context, deepLink),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    fun endFastPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            4101,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_END_FAST
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
