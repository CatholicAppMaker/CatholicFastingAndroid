package com.kevpierce.catholicfastingapp.ui

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.SetupProgressState
import org.junit.Test

class SetupCompletionSupportTest {
    @Test
    fun buildSetupCompletionStateAllowsCompletionWhenCoreSetupIsDone() {
        val state =
            SetupCompletionState.from(
                setupProgressState =
                    SetupProgressState(
                        completedSteps = 4,
                        totalSteps = 4,
                        birthProfileComplete = false,
                        independentNoticeAcknowledged = true,
                        regionSelected = true,
                        reminderTierSelected = true,
                        onboardingCompleted = false,
                    ),
                notificationPermissionGranted = false,
                notificationPermissionSupported = true,
            )

        assertThat(state.canCompleteOnboarding).isTrue()
        assertThat(state.shouldPromptForNotificationPermission).isTrue()
    }

    @Test
    fun buildSetupCompletionStateBlocksCompletionWhenReminderTierIsStillMinimal() {
        val state =
            SetupCompletionState.from(
                setupProgressState =
                    SetupProgressState(
                        completedSteps = 2,
                        totalSteps = 4,
                        birthProfileComplete = false,
                        independentNoticeAcknowledged = true,
                        regionSelected = true,
                        reminderTierSelected = false,
                        onboardingCompleted = false,
                    ),
                notificationPermissionGranted = false,
                notificationPermissionSupported = true,
            )

        assertThat(state.canCompleteOnboarding).isFalse()
        assertThat(state.shouldPromptForNotificationPermission).isFalse()
    }

    @Test
    fun buildSetupCompletionStateSkipsPermissionPromptWhenPlatformDoesNotNeedIt() {
        val state =
            SetupCompletionState.from(
                setupProgressState =
                    SetupProgressState(
                        completedSteps = 4,
                        totalSteps = 4,
                        birthProfileComplete = true,
                        independentNoticeAcknowledged = true,
                        regionSelected = true,
                        reminderTierSelected = true,
                        onboardingCompleted = false,
                    ),
                notificationPermissionGranted = false,
                notificationPermissionSupported = false,
            )

        assertThat(state.canCompleteOnboarding).isTrue()
        assertThat(state.shouldPromptForNotificationPermission).isFalse()
    }
}
