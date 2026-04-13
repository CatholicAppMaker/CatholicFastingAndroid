package com.kevpierce.catholicfastingapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kevpierce.catholicfasting.core.data.AppContainer

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != ACTION_END_FAST) {
            return
        }

        val repository = AppContainer.repository
        repository.endIntermittentFast()
        IntermittentFastNotificationManager.syncActiveFast(
            context = context,
            startIso = null,
            targetHours = repository.dashboardState.value.intermittentPresetHours,
        )
    }

    companion object {
        const val ACTION_END_FAST = "com.kevpierce.catholicfastingapp.action.END_FAST"
    }
}
