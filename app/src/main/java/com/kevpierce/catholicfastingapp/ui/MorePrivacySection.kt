package com.kevpierce.catholicfastingapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfastingapp.R

@Composable
internal fun PrivacyAndDataSection(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
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
        PrivacySummaryCard(state = state, supportState = supportState)
        DataStoredCard()
        CurrentLocalStateCard(state = state, supportState = supportState)
    }
}

@Composable
private fun PrivacySummaryCard(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    supportState: AppSupportState,
) {
    SectionCard(title = stringResource(R.string.more_privacy_title)) {
        Text(
            stringResource(R.string.more_privacy_local_first),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.more_privacy_backup_tools),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_privacy_last_sync,
                supportState.storageDiagnosticsState.lastLocalWriteIso ?: stringResource(R.string.more_not_yet_saved),
            ),
            style = CatholicFastingThemeValues.typography.utility,
        )
        Text(
            if (state.launchFunnelSnapshot.independentAppNoticeAcknowledged) {
                stringResource(R.string.more_notice_acknowledged)
            } else {
                stringResource(R.string.more_notice_not_acknowledged)
            },
            style = CatholicFastingThemeValues.typography.utility,
        )
        Text(
            stringResource(R.string.more_stored_reflections, state.reflections.size),
            style = CatholicFastingThemeValues.typography.utility,
        )
        Text(
            stringResource(R.string.more_household_profiles, state.profiles.size),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}

@Composable
private fun DataStoredCard() {
    SectionCard(title = stringResource(R.string.more_data_stored_title)) {
        Text(
            stringResource(R.string.more_data_profile_settings),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.more_data_observances),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.more_data_funnel),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.more_data_no_tracking),
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@Composable
private fun CurrentLocalStateCard(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    supportState: AppSupportState,
) {
    SectionCard(title = stringResource(R.string.more_current_local_state_title)) {
        Text(
            stringResource(R.string.more_observances_this_year, state.observances.size),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(
                R.string.more_completed_count,
                supportState.storageDiagnosticsState.completedObservancesCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_friday_notes_saved,
                supportState.storageDiagnosticsState.fridayNotesCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_intermittent_sessions_saved,
                supportState.storageDiagnosticsState.intermittentSessionsCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_stored_reflections,
                supportState.storageDiagnosticsState.reflectionsCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_selected_reminder_tier,
                supportState.reminderCenterState.selectedTier.localizedLabel(),
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        if (supportState.storageDiagnosticsState.warnings.isNotEmpty()) {
            supportState.storageDiagnosticsState.warnings.forEach { warning ->
                Text(
                    stringResource(R.string.more_warning_value, warning),
                    style = CatholicFastingThemeValues.typography.utility,
                )
            }
        }
    }
}
