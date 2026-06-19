package com.kevpierce.catholicfastingapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.OnboardingState
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderCenterState
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.model.SeasonalHeroState
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.rememberSeasonTone
import com.kevpierce.catholicfastingapp.R

@Composable
internal fun OnboardingRoute(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    notificationPermissionActions: NotificationPermissionActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val supportState = buildAppSupportState(context, state, billingState.premiumUnlocked)
    val onboardingState = supportState.onboardingState
    val completionState =
        SetupCompletionState.from(
            setupProgressState = supportState.setupProgressState,
            notificationPermissionGranted = notificationPermissionActions.granted,
            notificationPermissionSupported = notificationPermissionSupported(),
        )

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(CatholicFastingThemeValues.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.small),
    ) {
        OnboardingHeaderCard(
            onboardingState = onboardingState,
            setupProgressSummary = supportState.setupProgressSummary,
        )
        when (onboardingState.currentStep) {
            1 ->
                OnboardingNoticeCard(
                    noticeAcknowledged = onboardingState.noticeAcknowledged,
                    onNoticeAcknowledgedChange = repository::setIndependentAppNoticeAcknowledged,
                )

            2 ->
                OnboardingProfileSection(
                    state = state,
                    repository = repository,
                )

            3 ->
                OnboardingReminderCard(
                    onboardingState = onboardingState,
                    reminderCenterState = supportState.reminderCenterState,
                    notificationPermissionGranted = notificationPermissionActions.granted,
                    onRequestNotificationPermission = notificationPermissionActions.requestPermission,
                    onReminderTierChange = repository::setReminderTier,
                    onDailyQuoteReminderEnabledChange = repository::setDailyQuoteReminderEnabled,
                    onDailyQuoteReminderTimeChange = repository::setDailyQuoteReminderTime,
                )

            4 ->
                OnboardingIntentionCard(
                    onboardingState = onboardingState,
                    onIntentionSelected = repository::setIntermittentIntention,
                )

            else ->
                OnboardingPremiumCard(
                    seasonalHeroState = supportState.seasonalHeroState,
                    premiumSnapshot = supportState.premiumSnapshot,
                )
        }
        OnboardingFinishSection(
            completionState = completionState,
            onCompleteOnboarding = repository::completeOnboarding,
        )
    }
}

@Composable
private fun OnboardingProfileSection(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
) {
    OnboardingProfileCard(
        state = state,
        onRegionSelected = repository::setSelectedRegion,
        onFridayModeSelected = { mode ->
            repository.updateSettings(
                state.settings.copy(
                    fridayOutsideLentMode = mode,
                ),
            )
        },
    )
}

@Composable
private fun OnboardingFinishSection(
    completionState: SetupCompletionState,
    onCompleteOnboarding: () -> Unit,
) {
    OutlinedActionButton(
        label = stringResource(R.string.onboarding_finish),
        onClick = onCompleteOnboarding,
        enabled = completionState.canCompleteOnboarding,
    )
    if (!completionState.canCompleteOnboarding) {
        Text(stringResource(R.string.onboarding_finish_blocked))
    }
}

@Composable
private fun OnboardingHeaderCard(
    onboardingState: OnboardingState,
    setupProgressSummary: String,
) {
    SectionCard(
        title = stringResource(R.string.onboarding_title),
        heroTitle = true,
    ) {
        Text(stringResource(R.string.onboarding_subtitle), style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(
                R.string.onboarding_step_value,
                onboardingState.currentStep,
                onboardingState.totalSteps,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(setupProgressSummary, style = CatholicFastingThemeValues.typography.supporting)
    }
}

@Composable
private fun OnboardingNoticeCard(
    noticeAcknowledged: Boolean,
    onNoticeAcknowledgedChange: (Boolean) -> Unit,
) {
    SectionCard(title = stringResource(R.string.onboarding_notice_title)) {
        Text(
            stringResource(R.string.notice_independent_app_summary),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            if (noticeAcknowledged) {
                stringResource(R.string.more_notice_acknowledged)
            } else {
                stringResource(R.string.more_notice_pending)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        BooleanChoiceRow(
            selected = noticeAcknowledged,
            onSelectionChange = onNoticeAcknowledgedChange,
            trueLabel = stringResource(R.string.onboarding_notice_accept),
            falseLabel = stringResource(R.string.onboarding_notice_review),
        )
    }
}

@Composable
private fun OnboardingProfileCard(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    onRegionSelected: (RegionProfile) -> Unit,
    onFridayModeSelected: (FridayOutsideLentMode) -> Unit,
) {
    SectionCard(title = stringResource(R.string.onboarding_profile_title)) {
        Text(
            stringResource(R.string.onboarding_profile_body),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            if (state.settings.hasFullBirthDate) {
                stringResource(R.string.more_birth_profile_complete)
            } else {
                stringResource(R.string.onboarding_profile_follow_up)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(R.string.onboarding_region_title),
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        RegionProfileChipRow(
            selectedRegion = state.settings.regionProfile,
            onRegionSelected = onRegionSelected,
        )
        Text(
            stringResource(R.string.onboarding_friday_title),
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        FridayOutsideLentModeChipRow(
            selectedMode = state.settings.fridayOutsideLentMode,
            onFridayModeSelected = onFridayModeSelected,
        )
    }
}

@Composable
private fun RegionProfileChipRow(
    selectedRegion: RegionProfile,
    onRegionSelected: (RegionProfile) -> Unit,
) {
    RowWithScroll {
        RegionProfile.entries.forEach { region ->
            val regionLabel = region.localizedLabel()
            val selected = selectedRegion == region
            val regionStateDescription = selectedStateDescription(selected)
            FilterChip(
                selected = selected,
                onClick = { onRegionSelected(region) },
                label = { Text(regionLabel) },
                modifier =
                    Modifier
                        .testTag(REGION_CHIP_TEST_TAG_PREFIX + region.name)
                        .semantics {
                            contentDescription = regionLabel
                            stateDescription = regionStateDescription
                            onClick {
                                onRegionSelected(region)
                                true
                            }
                        },
            )
        }
    }
}

@Composable
private fun FridayOutsideLentModeChipRow(
    selectedMode: FridayOutsideLentMode,
    onFridayModeSelected: (FridayOutsideLentMode) -> Unit,
) {
    RowWithScroll {
        FridayOutsideLentMode.entries.forEach { mode ->
            val modeLabel = mode.localizedLabel()
            val selected = selectedMode == mode
            val modeStateDescription = selectedStateDescription(selected)
            FilterChip(
                selected = selected,
                onClick = { onFridayModeSelected(mode) },
                label = { Text(modeLabel) },
                modifier =
                    Modifier.semantics {
                        contentDescription = modeLabel
                        stateDescription = modeStateDescription
                    },
            )
        }
    }
}

@Composable
private fun OnboardingReminderCard(
    onboardingState: OnboardingState,
    reminderCenterState: ReminderCenterState,
    notificationPermissionGranted: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onReminderTierChange: (ReminderTier) -> Unit,
    onDailyQuoteReminderEnabledChange: (Boolean) -> Unit,
    onDailyQuoteReminderTimeChange: (Int, Int) -> Unit,
) {
    SectionCard(title = stringResource(R.string.onboarding_reminders_title)) {
        Text(stringResource(R.string.onboarding_reminders_body), style = CatholicFastingThemeValues.typography.body)
        ReminderTierChipRow(
            selectedTier = onboardingState.selectedReminderTier,
            reminderTierSelected = onboardingState.reminderTierSelected,
            onReminderTierChange = onReminderTierChange,
        )
        QuoteReminderControls(
            reminderCenterState = reminderCenterState,
            labelRes = R.string.onboarding_quote_reminder_value,
            selectedEnabled = onboardingState.dailyQuoteReminderEnabled,
            onDailyQuoteReminderEnabledChange = onDailyQuoteReminderEnabledChange,
            onDailyQuoteReminderTimeChange = onDailyQuoteReminderTimeChange,
        )
        Text(
            if (notificationPermissionGranted || !notificationPermissionSupported()) {
                stringResource(R.string.more_notification_permission_granted)
            } else {
                stringResource(R.string.onboarding_notification_permission_needed)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        if (notificationPermissionSupported() && !notificationPermissionGranted) {
            OutlinedActionButton(
                label = stringResource(R.string.onboarding_request_notification_permission),
                onClick = onRequestNotificationPermission,
            )
        }
    }
}

@Composable
private fun OnboardingIntentionCard(
    onboardingState: OnboardingState,
    onIntentionSelected: (String) -> Unit,
) {
    SectionCard(title = stringResource(R.string.onboarding_intention_title)) {
        Text(stringResource(R.string.onboarding_intention_body), style = CatholicFastingThemeValues.typography.body)
        IntentionChipRow(
            selectedIntentionId = onboardingState.selectedIntermittentIntentionId,
            onIntentionSelected = onIntentionSelected,
        )
        val selectedIntention =
            IntermittentFastIntention.entries.firstOrNull {
                it.name == onboardingState.selectedIntermittentIntentionId
            } ?: IntermittentFastIntention.PERSONAL_DISCIPLINE
        Text(selectedIntention.detail, style = CatholicFastingThemeValues.typography.supporting)
        if (!onboardingState.intermittentIntentionSelected) {
            Text(
                stringResource(R.string.onboarding_intention_pending),
                style = CatholicFastingThemeValues.typography.utility,
            )
        }
    }
}

@Composable
private fun OnboardingPremiumCard(
    seasonalHeroState: SeasonalHeroState,
    premiumSnapshot: PremiumSnapshot,
) {
    val tone = rememberSeasonTone(premiumSnapshot.season)

    SectionCard(
        title = stringResource(R.string.onboarding_premium_title),
        tone = tone,
    ) {
        Text(stringResource(R.string.onboarding_premium_body), style = CatholicFastingThemeValues.typography.body)
        Text(seasonalHeroState.campaignTitle, style = CatholicFastingThemeValues.typography.heroTitle)
        Text(seasonalHeroState.campaignSubtitle, style = CatholicFastingThemeValues.typography.sectionTitle)
        Text(seasonalHeroState.formationLine, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(
                R.string.onboarding_quote_card_value,
                seasonalHeroState.quote.text,
                seasonalHeroState.quote.author,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(premiumSnapshot.recoveryCoachPlan.summary, style = CatholicFastingThemeValues.typography.supporting)
    }
}
