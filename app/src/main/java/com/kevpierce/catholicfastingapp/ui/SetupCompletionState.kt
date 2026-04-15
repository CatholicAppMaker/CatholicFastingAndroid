package com.kevpierce.catholicfastingapp.ui

import com.kevpierce.catholicfasting.core.model.SetupProgressState

internal data class SetupCompletionState(
    val canCompleteOnboarding: Boolean,
    val shouldPromptForNotificationPermission: Boolean,
) {
    companion object {
        fun from(
            setupProgressState: SetupProgressState,
            notificationPermissionGranted: Boolean,
            notificationPermissionSupported: Boolean,
        ): SetupCompletionState =
            SetupCompletionState(
                canCompleteOnboarding =
                    setupProgressState.independentNoticeAcknowledged &&
                        setupProgressState.regionSelected &&
                        setupProgressState.reminderTierSelected,
                shouldPromptForNotificationPermission =
                    notificationPermissionSupported &&
                        !notificationPermissionGranted &&
                        setupProgressState.reminderTierSelected,
            )
    }
}
