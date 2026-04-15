package com.kevpierce.catholicfastingapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kevpierce.catholicfasting.core.data.AppContainer

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != ACTION_END_FAST) {
            return
        }

        AppContainer.initialize(context.applicationContext)
        val repository = AppContainer.repository
        val ended =
            repository.endIntermittentFastFromAction(
                startIso = intent.getStringExtra(EXTRA_START_ISO),
                targetHours =
                    intent.getIntExtra(
                        EXTRA_TARGET_HOURS,
                        repository.dashboardState.value.intermittentPresetHours,
                    ),
            )
        IntermittentFastNotificationManager.cancelActiveFast(context)
        Log.i(TAG, "Processed end-fast notification action. ended=$ended")
    }

    companion object {
        private const val TAG = "CFA.Notification"
        const val ACTION_END_FAST = "com.kevpierce.catholicfastingapp.action.END_FAST"
        const val EXTRA_START_ISO = "extra_start_iso"
        const val EXTRA_TARGET_HOURS = "extra_target_hours"
    }
}
