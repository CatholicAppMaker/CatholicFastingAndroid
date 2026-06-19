package com.kevpierce.catholicfastingapp.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import com.kevpierce.catholicfasting.feature.tracker.TrackerActions
import com.kevpierce.catholicfasting.feature.tracker.TrackerScreen
import com.kevpierce.catholicfasting.feature.tracker.TrackerUiState
import com.kevpierce.catholicfastingapp.R

internal fun completionSummary(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
): String {
    val completedCount = state.statusesById.count { it.value.countsTowardProgress }
    return context.resources.getQuantityString(
        R.plurals.summary_completion_value,
        completedCount,
        completedCount,
    )
}

@Composable
internal fun TrackFastDestination(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    supportState: AppSupportState,
    modifier: Modifier = Modifier,
) {
    TrackerScreen(
        uiState = trackerUiState(state, supportState),
        actions = trackerActions(repository, LocalResources.current),
        modifier = modifier,
    )
}

private fun trackerUiState(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    supportState: AppSupportState,
): TrackerUiState =
    TrackerUiState(
        schedules = state.schedules,
        activeScheduleId = state.activeIntermittentScheduleId,
        sessions = state.intermittentSessions,
        activeFast = state.activeIntermittentFast,
        presetHours = state.intermittentPresetHours,
        selectedIntentionId = state.launchFunnelSnapshot.selectedIntermittentIntentionId,
        latestRecap = supportState.companionSnapshot.liveFast.latestSessionRecap,
        premiumSnapshot = supportState.premiumSnapshot,
        prepGuidance = supportState.fastPrepGuidance,
        seasonProgramActions = supportState.seasonProgramActions,
    )

private fun trackerActions(
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    resources: android.content.res.Resources,
): TrackerActions =
    TrackerActions(
        onPresetHoursChange = repository::setIntermittentPresetHours,
        onIntentionChange = repository::setIntermittentIntention,
        onStartFast = repository::startIntermittentFastWithIntention,
        onEndFast = repository::endIntermittentFastWithReview,
        onCancelFast = repository::cancelIntermittentFast,
        onSaveSchedule = { scheduleId, name, startHour, weekdays ->
            repository
                .saveIntermittentSchedule(
                    scheduleId = scheduleId,
                    name = name,
                    startHour = startHour,
                    weekdays = weekdays,
                ).fold(
                    onSuccess = { plan -> resources.getString(R.string.status_schedule_saved, plan.name) },
                    onFailure = { it.message ?: resources.getString(R.string.status_schedule_save_failed) },
                )
        },
        onDeleteSchedule = { scheduleId ->
            repository
                .deleteIntermittentSchedule(scheduleId)
                .fold(
                    onSuccess = { resources.getString(R.string.status_schedule_deleted) },
                    onFailure = { it.message ?: resources.getString(R.string.status_schedule_delete_failed) },
                )
        },
        onApplySchedule = { scheduleId ->
            repository
                .applyIntermittentSchedule(scheduleId)
                .fold(
                    onSuccess = { plan -> resources.getString(R.string.status_schedule_applied, plan.name) },
                    onFailure = { it.message ?: resources.getString(R.string.status_schedule_apply_failed) },
                )
        },
    )
