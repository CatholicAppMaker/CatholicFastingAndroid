package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.LaunchFunnelSnapshot
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test

class OnboardingAndSetupProgressTest {
    @Test
    fun onboardingStateStartsAtNoticeStepWhenNoticeIsPending() {
        val onboardingState =
            buildOnboardingState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            independentAppNoticeAcknowledged = false,
                            selectedRegion = RegionProfile.US,
                            selectedReminderTier = ReminderTier.BALANCED,
                        ),
                ),
            )

        assertThat(onboardingState.currentStep).isEqualTo(1)
        assertThat(onboardingState.noticeAcknowledged).isFalse()
        assertThat(onboardingState.isCompleted).isFalse()
    }

    @Test
    fun onboardingStateMovesToRegionStepWhenStoredSelectionDriftsFromSettings() {
        val onboardingState =
            buildOnboardingState(
                sampleState(
                    settings = RuleSettings(regionProfile = RegionProfile.CANADA),
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.US,
                            regionSelected = true,
                            selectedReminderTier = ReminderTier.BALANCED,
                        ),
                ),
            )

        assertThat(onboardingState.currentStep).isEqualTo(2)
        assertThat(onboardingState.selectedRegion).isEqualTo(RegionProfile.US)
    }

    @Test
    fun onboardingStateMovesToRegionStepWhenDefaultRegionHasNotBeenConfirmed() {
        val onboardingState =
            buildOnboardingState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.US,
                            regionSelected = false,
                            selectedReminderTier = ReminderTier.BALANCED,
                        ),
                ),
            )

        assertThat(onboardingState.currentStep).isEqualTo(2)
        assertThat(onboardingState.selectedRegion).isEqualTo(RegionProfile.US)
        assertThat(onboardingState.regionSelected).isFalse()
    }

    @Test
    fun onboardingStateMovesToReminderStepWhenTierIsStillMinimal() {
        val onboardingState =
            buildOnboardingState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.US,
                            regionSelected = true,
                            selectedReminderTier = ReminderTier.MINIMAL,
                        ),
                ),
            )

        assertThat(onboardingState.currentStep).isEqualTo(3)
        assertThat(onboardingState.selectedReminderTier).isEqualTo(ReminderTier.MINIMAL)
        assertThat(onboardingState.reminderTierSelected).isFalse()
    }

    @Test
    fun onboardingStateMovesToReminderStepWhenDefaultTierHasNotBeenSelected() {
        val onboardingState =
            buildOnboardingState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.US,
                            regionSelected = true,
                            selectedReminderTier = ReminderTier.BALANCED,
                            reminderTierSelected = false,
                        ),
                ),
            )

        assertThat(onboardingState.currentStep).isEqualTo(3)
        assertThat(onboardingState.selectedReminderTier).isEqualTo(ReminderTier.BALANCED)
        assertThat(onboardingState.reminderTierSelected).isFalse()
    }

    @Test
    fun setupProgressCountsCompletedSetupSteps() {
        val progress =
            buildSetupProgressState(
                sampleState(
                    settings =
                        RuleSettings(
                            birthYear = 1990,
                            birthMonth = 4,
                            birthDay = 7,
                            regionProfile = RegionProfile.CANADA,
                        ),
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            completedOnboardingAtIso = "2026-03-13T00:10:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.CANADA,
                            regionSelected = true,
                            selectedReminderTier = ReminderTier.GUIDED,
                            reminderTierSelected = true,
                            selectedIntermittentIntentionId = IntermittentFastIntention.PRAYER.name,
                            intermittentIntentionSelected = true,
                        ),
                ),
            )

        assertThat(progress.completedSteps).isEqualTo(5)
        assertThat(progress.birthProfileComplete).isTrue()
        assertThat(progress.independentNoticeAcknowledged).isTrue()
        assertThat(progress.regionSelected).isTrue()
        assertThat(progress.reminderTierSelected).isTrue()
        assertThat(progress.intermittentIntentionSelected).isTrue()
        assertThat(progress.onboardingCompleted).isTrue()
    }

    @Test
    fun setupProgressLeavesReminderStepIncompleteWhenTierHasNotBeenSelected() {
        val progress =
            buildSetupProgressState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.US,
                            regionSelected = true,
                            selectedReminderTier = ReminderTier.BALANCED,
                            reminderTierSelected = false,
                        ),
                ),
            )

        assertThat(progress.completedSteps).isEqualTo(2)
        assertThat(progress.reminderTierSelected).isFalse()
        assertThat(progress.onboardingCompleted).isFalse()
    }

    @Test
    fun onboardingStateMovesToIntentionStepAfterReminderRhythm() {
        val onboardingState =
            buildOnboardingState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.US,
                            regionSelected = true,
                            selectedReminderTier = ReminderTier.GUIDED,
                            reminderTierSelected = true,
                        ),
                ),
            )

        assertThat(onboardingState.currentStep).isEqualTo(4)
        assertThat(onboardingState.intermittentIntentionSelected).isFalse()
        assertThat(onboardingState.isCompleted).isFalse()
    }

    @Test
    fun onboardingStateMovesToCompletionStepAfterIntentionIsSelected() {
        val onboardingState =
            buildOnboardingState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.US,
                            regionSelected = true,
                            selectedReminderTier = ReminderTier.GUIDED,
                            reminderTierSelected = true,
                            selectedIntermittentIntentionId = IntermittentFastIntention.MERCY.name,
                            intermittentIntentionSelected = true,
                        ),
                ),
            )

        assertThat(onboardingState.currentStep).isEqualTo(5)
        assertThat(onboardingState.selectedIntermittentIntentionId)
            .isEqualTo(IntermittentFastIntention.MERCY.name)
        assertThat(onboardingState.isCompleted).isFalse()
    }

    @Test
    fun onboardingStateRemainsCompletedWhenStoredCompletionDateExists() {
        val onboardingState =
            buildOnboardingState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            completedOnboardingAtIso = "2026-03-13T00:10:00Z",
                            independentAppNoticeAcknowledged = true,
                            selectedRegion = RegionProfile.US,
                            selectedReminderTier = ReminderTier.GUIDED,
                        ),
                ),
            )

        assertThat(onboardingState.currentStep).isEqualTo(5)
        assertThat(onboardingState.isCompleted).isTrue()
        assertThat(onboardingState.regionSelected).isTrue()
    }

    private fun sampleState(
        settings: RuleSettings = RuleSettings(),
        launchFunnelSnapshot: LaunchFunnelSnapshot =
            LaunchFunnelSnapshot(
                startedAtIso = "2026-03-13T00:00:00Z",
                independentAppNoticeAcknowledged = true,
                selectedRegion = RegionProfile.US,
                regionSelected = true,
                selectedReminderTier = ReminderTier.BALANCED,
                reminderTierSelected = true,
            ),
    ): DashboardState =
        DashboardState(
            settings = settings,
            year = 2026,
            observances = observancesFor(2026, settings),
            launchFunnelSnapshot = launchFunnelSnapshot,
        )
}
