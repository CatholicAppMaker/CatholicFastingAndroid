package com.kevpierce.catholicfastingapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.feature.guidance.GuidanceScreen
import com.kevpierce.catholicfasting.feature.settings.SettingsScreen
import com.kevpierce.catholicfastingapp.R

@Composable
internal fun MoreDestination(
    initialSection: MoreSection,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    onSettingsChange: (com.kevpierce.catholicfasting.core.model.RuleSettings) -> Unit,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    notificationPermissionActions: NotificationPermissionActions,
    supportState: AppSupportState,
    modifier: Modifier = Modifier,
) {
    var section by rememberSaveable(initialSection) { mutableStateOf(initialSection) }
    LaunchedEffect(initialSection) {
        section = initialSection
    }

    Column(modifier = modifier) {
        MoreSectionTabs(
            selected = section,
            onSelected = { section = it },
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            MoreSectionContent(
                section = section,
                state = state,
                repository = repository,
                onSettingsChange = onSettingsChange,
                billingState = billingState,
                billingActions = billingActions,
                notificationPermissionActions = notificationPermissionActions,
                supportState = supportState,
            )
        }
    }
}

@Composable
private fun MoreSectionTabs(
    selected: MoreSection,
    onSelected: (MoreSection) -> Unit,
) {
    val spacing = CatholicFastingThemeValues.spacing
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.medium, vertical = spacing.small),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        Text(
            stringResource(R.string.more_title),
            style = CatholicFastingThemeValues.typography.screenTitle,
            modifier = Modifier.padding(horizontal = spacing.xxSmall),
        )
        RowWithScroll {
            MoreSection.entries.forEach { section ->
                val sectionLabel = stringResource(section.labelRes())
                val sectionStateDescription = selectedStateDescription(selected == section)
                FilterChip(
                    selected = selected == section,
                    onClick = { onSelected(section) },
                    modifier =
                        Modifier.semantics {
                            contentDescription = sectionLabel
                            stateDescription = sectionStateDescription
                        },
                    label = { Text(sectionLabel) },
                )
            }
        }
    }
}

internal fun MoreSection.labelRes(): Int =
    when (this) {
        MoreSection.SUPPORT_PREMIUM -> R.string.more_support_premium
        MoreSection.SETUP_REMINDERS -> R.string.more_setup_reminders
        MoreSection.PROFILE_NORMS -> R.string.more_profile_norms
        MoreSection.GUIDANCE_RULES -> R.string.more_guidance_rules
        MoreSection.HISTORY_OF_FASTING -> R.string.more_history_fasting
        MoreSection.PRIVACY_DATA -> R.string.more_privacy_data
    }

@Composable
private fun MoreSectionContent(
    section: MoreSection,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    onSettingsChange: (com.kevpierce.catholicfasting.core.model.RuleSettings) -> Unit,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    notificationPermissionActions: NotificationPermissionActions,
    supportState: AppSupportState,
) {
    when (section) {
        MoreSection.SUPPORT_PREMIUM ->
            SupportAndPremiumSection(
                state = state,
                repository = repository,
                billingState = billingState,
                billingActions = billingActions,
                supportState = supportState,
            )
        MoreSection.SETUP_REMINDERS ->
            SetupAndRemindersSection(
                state = state,
                actions =
                    SetupReminderActions(
                        onReminderTierChange = repository::setReminderTier,
                        onDailyQuoteReminderEnabledChange = repository::setDailyQuoteReminderEnabled,
                        onDailyQuoteReminderTimeChange = repository::setDailyQuoteReminderTime,
                        onNoticeAcknowledgedChange = repository::setIndependentAppNoticeAcknowledged,
                        onCompleteOnboarding = repository::completeOnboarding,
                    ),
                notificationPermissionActions = notificationPermissionActions,
                supportState = supportState,
                modifier = Modifier.fillMaxSize(),
            )
        MoreSection.PROFILE_NORMS ->
            SettingsScreen(
                settings = state.settings,
                onSettingsChange = onSettingsChange,
                modifier = Modifier.fillMaxSize(),
            )
        MoreSection.GUIDANCE_RULES ->
            GuidanceScreen(
                settings = state.settings,
                ruleBundleAudit = supportState.ruleBundleAudit,
                devotionalGallery = supportState.devotionalGallery,
                modifier = Modifier.fillMaxSize(),
            )
        MoreSection.HISTORY_OF_FASTING ->
            HistoryOfFastingSection(
                supportState = supportState,
                modifier = Modifier.fillMaxSize(),
            )
        MoreSection.PRIVACY_DATA ->
            PrivacyAndDataSection(
                state = state,
                supportState = supportState,
                modifier = Modifier.fillMaxSize(),
            )
    }
}
