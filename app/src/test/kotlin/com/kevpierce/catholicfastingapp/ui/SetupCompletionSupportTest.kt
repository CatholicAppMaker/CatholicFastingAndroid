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
                        completedSteps = 5,
                        totalSteps = 5,
                        birthProfileComplete = false,
                        independentNoticeAcknowledged = true,
                        regionSelected = true,
                        reminderTierSelected = true,
                        intermittentIntentionSelected = true,
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
                        totalSteps = 5,
                        birthProfileComplete = false,
                        independentNoticeAcknowledged = true,
                        regionSelected = true,
                        reminderTierSelected = false,
                        intermittentIntentionSelected = true,
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
                        completedSteps = 5,
                        totalSteps = 5,
                        birthProfileComplete = true,
                        independentNoticeAcknowledged = true,
                        regionSelected = true,
                        reminderTierSelected = true,
                        intermittentIntentionSelected = true,
                        onboardingCompleted = false,
                    ),
                notificationPermissionGranted = false,
                notificationPermissionSupported = false,
            )

        assertThat(state.canCompleteOnboarding).isTrue()
        assertThat(state.shouldPromptForNotificationPermission).isFalse()
    }

    @Test
    fun buildSetupCompletionStateSkipsPermissionPromptWhenAlreadyGranted() {
        val state =
            SetupCompletionState.from(
                setupProgressState =
                    SetupProgressState(
                        completedSteps = 5,
                        totalSteps = 5,
                        birthProfileComplete = true,
                        independentNoticeAcknowledged = true,
                        regionSelected = true,
                        reminderTierSelected = true,
                        intermittentIntentionSelected = true,
                        onboardingCompleted = false,
                    ),
                notificationPermissionGranted = true,
                notificationPermissionSupported = true,
            )

        assertThat(state.canCompleteOnboarding).isTrue()
        assertThat(state.shouldPromptForNotificationPermission).isFalse()
    }

    @Test
    fun buildSetupCompletionStateBlocksCompletionWhenRegionSetupIsIncomplete() {
        val state =
            SetupCompletionState.from(
                setupProgressState =
                    SetupProgressState(
                        completedSteps = 1,
                        totalSteps = 5,
                        birthProfileComplete = false,
                        independentNoticeAcknowledged = true,
                        regionSelected = false,
                        reminderTierSelected = true,
                        intermittentIntentionSelected = true,
                        onboardingCompleted = false,
                    ),
                notificationPermissionGranted = false,
                notificationPermissionSupported = true,
            )

        assertThat(state.canCompleteOnboarding).isFalse()
        assertThat(state.shouldPromptForNotificationPermission).isTrue()
    }
}
