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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.ReminderCenterState
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfastingapp.R

@Composable
internal fun SetupAndRemindersSection(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    actions: SetupReminderActions,
    notificationPermissionActions: NotificationPermissionActions,
    supportState: AppSupportState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(CatholicFastingThemeValues.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.small),
    ) {
        QuickSetupCard(
            state = state,
            setupProgressSummary = supportState.setupProgressSummary,
            onNoticeAcknowledgedChange = actions.onNoticeAcknowledgedChange,
            onCompleteOnboarding = actions.onCompleteOnboarding,
        )
        ReminderCenterCard(
            reminderCenterState = supportState.reminderCenterState,
            summaryLine = supportState.premiumSnapshot.reminderRecommendation.summaryLine,
            notificationPermissionGranted = notificationPermissionActions.granted,
            onRequestNotificationPermission = notificationPermissionActions.requestPermission,
            onReminderTierChange = actions.onReminderTierChange,
            onDailyQuoteReminderEnabledChange = actions.onDailyQuoteReminderEnabledChange,
            onDailyQuoteReminderTimeChange = actions.onDailyQuoteReminderTimeChange,
        )
        SectionCard(title = stringResource(R.string.more_setup_progress_title)) {
            Text(supportState.setupProgressSummary)
            Text(stringResource(R.string.more_region_value, state.settings.regionProfile.localizedLabel()))
            Text(stringResource(R.string.more_calendar_value, state.settings.calendarMode.localizedLabel()))
            Text(
                stringResource(
                    R.string.more_reminder_strategy_value,
                    state.launchFunnelSnapshot.selectedReminderTier.localizedLabel(),
                ),
            )
            Text(
                if (state.settings.hasFullBirthDate) {
                    stringResource(R.string.more_birth_profile_complete)
                } else {
                    stringResource(R.string.more_birth_profile_partial)
                },
            )
            Text(stringResource(R.string.more_profiles_stored, state.profiles.size))
        }
    }
}

@Composable
private fun QuickSetupCard(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    setupProgressSummary: String,
    onNoticeAcknowledgedChange: (Boolean) -> Unit,
    onCompleteOnboarding: () -> Unit,
) {
    SectionCard(title = stringResource(R.string.more_quick_setup_title)) {
        Text(stringResource(R.string.more_quick_setup_body), style = CatholicFastingThemeValues.typography.body)
        Text(
            if (state.launchFunnelSnapshot.independentAppNoticeAcknowledged) {
                stringResource(R.string.more_notice_acknowledged)
            } else {
                stringResource(R.string.more_notice_pending)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        RowWithScroll {
            listOf(true, false).forEach { acknowledged ->
                FilterChip(
                    selected =
                        state.launchFunnelSnapshot.independentAppNoticeAcknowledged == acknowledged,
                    onClick = { onNoticeAcknowledgedChange(acknowledged) },
                    label = {
                        Text(
                            if (acknowledged) {
                                stringResource(R.string.more_notice_acknowledged_chip)
                            } else {
                                stringResource(R.string.more_notice_pending_chip)
                            },
                        )
                    },
                )
            }
        }
        Text(setupProgressSummary, style = CatholicFastingThemeValues.typography.supporting)
        OutlinedActionButton(
            label =
                if (state.launchFunnelSnapshot.completedOnboardingAtIso == null) {
                    stringResource(R.string.more_mark_setup_complete)
                } else {
                    stringResource(R.string.more_refresh_setup_complete)
                },
            onClick = onCompleteOnboarding,
        )
    }
}

@Composable
private fun ReminderCenterCard(
    reminderCenterState: ReminderCenterState,
    summaryLine: String,
    notificationPermissionGranted: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onReminderTierChange: (ReminderTier) -> Unit,
    onDailyQuoteReminderEnabledChange: (Boolean) -> Unit,
    onDailyQuoteReminderTimeChange: (Int, Int) -> Unit,
) {
    SectionCard(title = stringResource(R.string.more_reminder_center_title)) {
        Text(summaryLine, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(
                R.string.more_reminder_tier_value,
                reminderCenterState.selectedTier.localizedLabel(),
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            reminderCenterState.selectedTier.localizedSummary(),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        ReminderTierChipRow(
            selectedTier = reminderCenterState.selectedTier,
            reminderTierSelected = true,
            onReminderTierChange = onReminderTierChange,
        )
        QuoteReminderControls(
            reminderCenterState = reminderCenterState,
            labelRes = R.string.more_quote_reminder_time_value,
            selectedEnabled = reminderCenterState.dailyQuoteReminderEnabled,
            onDailyQuoteReminderEnabledChange = onDailyQuoteReminderEnabledChange,
            onDailyQuoteReminderTimeChange = onDailyQuoteReminderTimeChange,
        )
        Text(
            stringResource(R.string.more_reminder_strategy_body),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            if (notificationPermissionGranted) {
                stringResource(R.string.more_notification_permission_granted)
            } else {
                stringResource(R.string.more_notification_permission_needed)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        if (notificationPermissionSupported() && !notificationPermissionGranted) {
            OutlinedActionButton(
                label = stringResource(R.string.more_request_notification_permission),
                onClick = onRequestNotificationPermission,
            )
        }
        Text(stringResource(R.string.more_required_day_local), style = CatholicFastingThemeValues.typography.utility)
        Text(stringResource(R.string.more_active_fast_sync), style = CatholicFastingThemeValues.typography.utility)
    }
}

@Composable
internal fun ReminderTierChipRow(
    selectedTier: ReminderTier,
    reminderTierSelected: Boolean,
    onReminderTierChange: (ReminderTier) -> Unit,
) {
    RowWithScroll {
        ReminderTier.entries.forEach { tier ->
            val tierLabel = tier.localizedLabel()
            val selected = reminderTierSelected && selectedTier == tier
            val tierStateDescription = selectedStateDescription(selected)
            FilterChip(
                selected = selected,
                onClick = { onReminderTierChange(tier) },
                label = { Text(tierLabel) },
                modifier =
                    Modifier
                        .testTag(REMINDER_TIER_CHIP_TEST_TAG_PREFIX + tier.name)
                        .semantics {
                            contentDescription = tierLabel
                            stateDescription = tierStateDescription
                            onClick {
                                onReminderTierChange(tier)
                                true
                            }
                        },
            )
        }
    }
}

@Composable
internal fun IntentionChipRow(
    selectedIntentionId: String,
    onIntentionSelected: (String) -> Unit,
) {
    RowWithScroll {
        IntermittentFastIntention.entries.forEach { intention ->
            val selected = selectedIntentionId == intention.name
            val intentionStateDescription = selectedStateDescription(selected)
            FilterChip(
                selected = selected,
                onClick = { onIntentionSelected(intention.name) },
                label = { Text(intention.label) },
                modifier =
                    Modifier
                        .testTag(INTERMITTENT_INTENTION_CHIP_TEST_TAG_PREFIX + intention.name)
                        .semantics {
                            contentDescription = intention.label
                            stateDescription = intentionStateDescription
                            onClick {
                                onIntentionSelected(intention.name)
                                true
                            }
                        },
            )
        }
    }
}

@Composable
internal fun QuoteReminderControls(
    reminderCenterState: ReminderCenterState,
    labelRes: Int,
    selectedEnabled: Boolean,
    onDailyQuoteReminderEnabledChange: (Boolean) -> Unit,
    onDailyQuoteReminderTimeChange: (Int, Int) -> Unit,
) {
    Text(
        stringResource(
            labelRes,
            reminderCenterState.dailyQuoteTimeLabel,
        ),
        style = CatholicFastingThemeValues.typography.supporting,
    )
    BooleanChoiceRow(
        selected = selectedEnabled,
        onSelectionChange = onDailyQuoteReminderEnabledChange,
        trueLabel = stringResource(R.string.onboarding_quote_reminder_on),
        falseLabel = stringResource(R.string.onboarding_quote_reminder_off),
    )
    if (reminderCenterState.dailyQuoteReminderEnabled) {
        RowWithScroll {
            listOf(6, 7, 8, 9).forEach { hour ->
                val hourLabel = stringResource(R.string.onboarding_hour_value, hour)
                val hourStateDescription =
                    selectedStateDescription(reminderCenterState.dailyQuoteReminderHour == hour)
                FilterChip(
                    selected = reminderCenterState.dailyQuoteReminderHour == hour,
                    onClick = {
                        onDailyQuoteReminderTimeChange(
                            hour,
                            reminderCenterState.dailyQuoteReminderMinute,
                        )
                    },
                    label = { Text(hourLabel) },
                    modifier =
                        Modifier.semantics {
                            contentDescription = hourLabel
                            stateDescription = hourStateDescription
                        },
                )
            }
        }
    }
}

@Composable
internal fun BooleanChoiceRow(
    selected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    trueLabel: String,
    falseLabel: String,
) {
    RowWithScroll {
        listOf(true to trueLabel, false to falseLabel).forEach { (value, label) ->
            val optionStateDescription = selectedStateDescription(selected == value)
            FilterChip(
                selected = selected == value,
                onClick = { onSelectionChange(value) },
                label = { Text(label) },
                modifier =
                    Modifier.semantics {
                        contentDescription = label
                        stateDescription = optionStateDescription
                    },
            )
        }
    }
}
