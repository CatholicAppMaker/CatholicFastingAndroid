@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfasting.feature.tracker

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.catholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.catholicFastingSectionCard
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TrackerUiState(
    val schedules: List<IntermittentSchedulePlan>,
    val activeScheduleId: String?,
    val sessions: List<IntermittentFastSession>,
    val activeFast: ActiveIntermittentFast?,
    val presetHours: Int,
    val premiumSnapshot: PremiumSnapshot,
    val prepGuidance: List<String>,
    val seasonProgramActions: List<String>,
)

data class TrackerActions(
    val onPresetHoursChange: (Int) -> Unit,
    val onStartFast: () -> Unit,
    val onEndFast: () -> Unit,
    val onCancelFast: () -> Unit,
    val onSaveSchedule: (String?, String, Int, Set<Int>) -> String,
    val onDeleteSchedule: (String) -> String,
    val onApplySchedule: (String) -> String,
)

private data class ScheduleEditorUiState(
    val editingScheduleId: String?,
    val scheduleName: String,
    val scheduleStartHourInput: String,
    val selectedWeekdays: Set<Int>,
    val statusMessage: String,
)

private data class ScheduleEditorActions(
    val onScheduleNameChange: (String) -> Unit,
    val onScheduleStartHourChange: (String) -> Unit,
    val onToggleWeekday: (Int) -> Unit,
    val onApplySchedule: (String) -> Unit,
    val onEditSchedule: (IntermittentSchedulePlan) -> Unit,
    val onDeleteSchedule: (String) -> Unit,
    val onSaveSchedule: () -> Unit,
    val onCancelEdit: () -> Unit,
)

@OptIn(ExperimentalLayoutApi::class)
@Suppress("LongMethod")
@Composable
fun trackerScreen(
    uiState: TrackerUiState,
    actions: TrackerActions,
    modifier: Modifier = Modifier,
) {
    val spacing = CatholicFastingThemeValues.spacing
    val context = LocalContext.current
    var presetInput by remember(uiState.presetHours) { mutableStateOf(uiState.presetHours.toString()) }
    var scheduleStatus by rememberSaveable { mutableStateOf("") }
    var editingScheduleId by rememberSaveable { mutableStateOf<String?>(null) }
    var scheduleName by rememberSaveable { mutableStateOf("") }
    var scheduleStartHourInput by rememberSaveable { mutableStateOf("20") }
    var selectedWeekdays by remember { mutableStateOf(defaultWeekdays()) }

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        item {
            catholicFastingScreenTitle(
                text = stringResource(R.string.tracker_title),
                modifier = Modifier.semantics { heading() },
            )
        }
        item {
            activeFastCard(
                activeFast = uiState.activeFast,
                presetHours = uiState.presetHours,
                presetInput = presetInput,
                onPresetInputChange = { next ->
                    presetInput = next
                    next.toIntOrNull()?.let(actions.onPresetHoursChange)
                },
                onStartFast = actions.onStartFast,
                onEndFast = actions.onEndFast,
                onCancelFast = actions.onCancelFast,
            )
        }
        item {
            schedulesCard(schedules = uiState.schedules)
        }
        item {
            schedulePlannerCard(
                schedules = uiState.schedules,
                activeScheduleId = uiState.activeScheduleId,
                presetHours = uiState.presetHours,
                editorState =
                    ScheduleEditorUiState(
                        editingScheduleId = editingScheduleId,
                        scheduleName = scheduleName,
                        scheduleStartHourInput = scheduleStartHourInput,
                        selectedWeekdays = selectedWeekdays,
                        statusMessage = scheduleStatus,
                    ),
                editorActions =
                    ScheduleEditorActions(
                        onScheduleNameChange = { scheduleName = it },
                        onScheduleStartHourChange = { scheduleStartHourInput = it },
                        onToggleWeekday = { weekday ->
                            selectedWeekdays =
                                if (selectedWeekdays.contains(weekday)) {
                                    selectedWeekdays - weekday
                                } else {
                                    selectedWeekdays + weekday
                                }
                        },
                        onApplySchedule = { scheduleId ->
                            presetInput =
                                uiState.schedules
                                    .firstOrNull { it.id == scheduleId }
                                    ?.targetHours
                                    ?.toString()
                                    ?: presetInput
                            scheduleStatus = actions.onApplySchedule(scheduleId)
                        },
                        onEditSchedule = { plan ->
                            editingScheduleId = plan.id
                            scheduleName = plan.name
                            scheduleStartHourInput = plan.startHour.toString()
                            selectedWeekdays = plan.weekdays.toSet()
                            presetInput = plan.targetHours.toString()
                            actions.onPresetHoursChange(plan.targetHours)
                            scheduleStatus =
                                actionLabelString(
                                    context = context,
                                    resId = R.string.tracker_editing_value,
                                    value = plan.name,
                                )
                        },
                        onDeleteSchedule = { scheduleId ->
                            scheduleStatus = actions.onDeleteSchedule(scheduleId)
                            if (editingScheduleId == scheduleId) {
                                editingScheduleId = null
                                scheduleName = ""
                                scheduleStartHourInput = "20"
                                selectedWeekdays = defaultWeekdays()
                            }
                        },
                        onSaveSchedule = {
                            val startHour = scheduleStartHourInput.toIntOrNull() ?: 20
                            scheduleStatus =
                                actions.onSaveSchedule(
                                    editingScheduleId,
                                    scheduleName,
                                    startHour,
                                    selectedWeekdays,
                                )
                            editingScheduleId = null
                            scheduleName = ""
                            scheduleStartHourInput = "20"
                            selectedWeekdays = defaultWeekdays()
                        },
                        onCancelEdit = {
                            editingScheduleId = null
                            scheduleName = ""
                            scheduleStartHourInput = "20"
                            selectedWeekdays = defaultWeekdays()
                            scheduleStatus =
                                actionLabelString(
                                    context = context,
                                    resId = R.string.tracker_schedule_edit_canceled,
                                )
                        },
                    ),
            )
        }
        item {
            sessionSummaryCard(
                sessions = uiState.sessions,
                activeSchedule =
                    uiState.schedules.firstOrNull { it.id == uiState.activeScheduleId },
            )
        }
        item {
            trackerSupportCard(
                premiumSnapshot = uiState.premiumSnapshot,
                prepGuidance = uiState.prepGuidance,
                seasonProgramActions = uiState.seasonProgramActions,
            )
        }
        item {
            Text(
                stringResource(R.string.tracker_recent_sessions),
                style = CatholicFastingThemeValues.typography.sectionTitle,
                modifier = Modifier.semantics { heading() },
            )
        }
        if (uiState.sessions.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.tracker_no_sessions),
                    style = CatholicFastingThemeValues.typography.supporting,
                )
            }
        } else {
            items(uiState.sessions.take(12), key = IntermittentFastSession::id) { session ->
                sessionRow(session)
            }
        }
    }
}

@Composable
private fun trackerSupportCard(
    premiumSnapshot: PremiumSnapshot,
    prepGuidance: List<String>,
    seasonProgramActions: List<String>,
) {
    catholicFastingSectionCard(title = stringResource(R.string.tracker_preparation_recovery)) {
        Text(premiumSnapshot.recoveryCoachPlan.summary)
        prepGuidance.forEach { line ->
            Text(stringResource(R.string.tracker_bullet_value, line))
        }
        Text(
            stringResource(R.string.tracker_current_program),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        seasonProgramActions.forEach { action ->
            Text(stringResource(R.string.tracker_bullet_value, action))
        }
    }
}

@Composable
private fun activeFastCard(
    activeFast: ActiveIntermittentFast?,
    presetHours: Int,
    presetInput: String,
    onPresetInputChange: (String) -> Unit,
    onStartFast: () -> Unit,
    onEndFast: () -> Unit,
    onCancelFast: () -> Unit,
) {
    catholicFastingSectionCard(title = stringResource(R.string.tracker_control_center)) {
        if (activeFast == null) {
            Text(stringResource(R.string.tracker_no_active_fast))
        } else {
            val start = parseInstant(activeFast.startIso)
            val elapsed = start?.let { Duration.between(it, Instant.now()) }
            Text(
                stringResource(R.string.tracker_fast_in_progress),
                style = CatholicFastingThemeValues.typography.supporting,
            )
            Text(
                stringResource(
                    R.string.tracker_started_value,
                    start?.let(::formatDateTime) ?: stringResource(R.string.tracker_unknown),
                ),
            )
            Text(
                stringResource(
                    R.string.tracker_elapsed_value,
                    elapsed?.let(::formatDuration) ?: stringResource(R.string.tracker_unavailable),
                ),
            )
            Text(stringResource(R.string.tracker_target_hours_value, activeFast.targetHours))
        }

        OutlinedTextField(
            value = presetInput,
            onValueChange = onPresetInputChange,
            label = { Text(stringResource(R.string.tracker_target_hours_label)) },
            supportingText = { Text(stringResource(R.string.tracker_target_hours_supporting)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = activeFast == null,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.tracker_current_preset_value, presetHours),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (activeFast == null) {
                Button(onClick = onStartFast) {
                    Text(stringResource(R.string.tracker_start_fast))
                }
            } else {
                Button(onClick = onEndFast) {
                    Text(stringResource(R.string.tracker_end_fast))
                }
                OutlinedButton(onClick = onCancelFast) {
                    Text(stringResource(R.string.tracker_cancel))
                }
            }
        }
    }
}

@Composable
private fun schedulesCard(schedules: List<IntermittentSchedulePlan>) {
    catholicFastingSectionCard(title = stringResource(R.string.tracker_saved_schedules)) {
        if (schedules.isEmpty()) {
            Text(stringResource(R.string.tracker_no_saved_schedules))
        } else {
            schedules.forEach { schedule ->
                Text(
                    text =
                        stringResource(
                            R.string.tracker_saved_schedule_value,
                            schedule.name,
                            schedule.targetHours,
                            "%02d:00".format(schedule.startHour),
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun schedulePlannerCard(
    schedules: List<IntermittentSchedulePlan>,
    activeScheduleId: String?,
    presetHours: Int,
    editorState: ScheduleEditorUiState,
    editorActions: ScheduleEditorActions,
) {
    catholicFastingSectionCard(title = stringResource(R.string.tracker_custom_schedules)) {
        Text(stringResource(R.string.tracker_custom_schedules_body))
        OutlinedTextField(
            value = editorState.scheduleName,
            onValueChange = editorActions.onScheduleNameChange,
            label = { Text(stringResource(R.string.tracker_schedule_name_label)) },
            supportingText = { Text(stringResource(R.string.tracker_schedule_name_supporting)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = editorState.scheduleStartHourInput,
            onValueChange = editorActions.onScheduleStartHourChange,
            label = { Text(stringResource(R.string.tracker_start_hour_label)) },
            supportingText = {
                Text(stringResource(R.string.tracker_start_hour_supporting, presetHours))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        weekdaySelector(
            selectedWeekdays = editorState.selectedWeekdays,
            onToggleWeekday = editorActions.onToggleWeekday,
        )
        scheduleEditorActions(
            editingScheduleId = editorState.editingScheduleId,
            canSave = editorState.selectedWeekdays.isNotEmpty(),
            onSaveSchedule = editorActions.onSaveSchedule,
            onCancelEdit = editorActions.onCancelEdit,
        )
        if (schedules.isEmpty()) {
            Text(stringResource(R.string.tracker_no_saved_schedules))
        } else {
            schedules.forEach { schedule ->
                scheduleRow(
                    schedule = schedule,
                    isActive = schedule.id == activeScheduleId,
                    onApply = { editorActions.onApplySchedule(schedule.id) },
                    onEdit = { editorActions.onEditSchedule(schedule) },
                    onDelete = { editorActions.onDeleteSchedule(schedule.id) },
                )
            }
        }
        if (editorState.statusMessage.isNotBlank()) {
            Text(
                text = editorState.statusMessage,
                style = CatholicFastingThemeValues.typography.utility,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun weekdaySelector(
    selectedWeekdays: Set<Int>,
    onToggleWeekday: (Int) -> Unit,
) {
    val context = LocalContext.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (weekday in 1..7) {
            FilterChip(
                selected = selectedWeekdays.contains(weekday),
                onClick = { onToggleWeekday(weekday) },
                label = { Text(weekdayShortLabel(context, weekday)) },
            )
        }
    }
}

@Composable
private fun scheduleEditorActions(
    editingScheduleId: String?,
    canSave: Boolean,
    onSaveSchedule: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onSaveSchedule,
            enabled = canSave,
        ) {
            Text(
                if (editingScheduleId == null) {
                    stringResource(R.string.tracker_save_schedule)
                } else {
                    stringResource(R.string.tracker_update_schedule)
                },
            )
        }
        if (editingScheduleId != null) {
            OutlinedButton(onClick = onCancelEdit) {
                Text(stringResource(R.string.tracker_cancel_edit))
            }
        }
    }
}

@Composable
private fun scheduleRow(
    schedule: IntermittentSchedulePlan,
    isActive: Boolean,
    onApply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    catholicFastingSectionCard(
        title =
            if (isActive) {
                stringResource(R.string.tracker_applied_schedule_value, schedule.name)
            } else {
                schedule.name
            },
    ) {
        Text(
            stringResource(
                R.string.tracker_schedule_row_value,
                schedule.targetHours,
                "%02d:00".format(schedule.startHour),
                weekdayListText(context, schedule.weekdays),
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onApply) {
                Text(stringResource(R.string.tracker_use))
            }
            OutlinedButton(onClick = onEdit) {
                Text(stringResource(R.string.tracker_edit))
            }
            OutlinedButton(onClick = onDelete) {
                Text(stringResource(R.string.tracker_delete))
            }
        }
    }
}

@Composable
private fun sessionSummaryCard(
    sessions: List<IntermittentFastSession>,
    activeSchedule: IntermittentSchedulePlan?,
) {
    val context = LocalContext.current
    val completedTargets = sessions.count(IntermittentFastSession::completedTarget)
    val longestSession = sessions.maxOfOrNull { sessionDurationHours(it) } ?: 0.0
    val hitRate =
        if (sessions.isEmpty()) {
            0
        } else {
            (completedTargets.toDouble() / sessions.size.toDouble() * 100).toInt()
        }

    catholicFastingSectionCard(title = stringResource(R.string.tracker_recent_summary)) {
        Text(stringResource(R.string.tracker_sessions_tracked_value, sessions.size))
        Text(stringResource(R.string.tracker_target_hit_count_value, completedTargets))
        Text(stringResource(R.string.tracker_longest_session_value, "%.1f".format(longestSession)))
        Text(stringResource(R.string.tracker_recent_hit_rate_value, hitRate))
        activeSchedule?.let { schedule ->
            Text(
                stringResource(
                    R.string.tracker_applied_schedule_days_value,
                    schedule.name,
                    weekdayListText(context, schedule.weekdays),
                ),
            )
        }
    }
}

@Composable
private fun sessionRow(session: IntermittentFastSession) {
    val start = parseInstant(session.startIso)
    val end = parseInstant(session.endIso)
    catholicFastingSectionCard(
        title =
            "${start?.let(::formatDateTime) ?: session.startIso} -> " +
                "${end?.let(::formatDateTime) ?: session.endIso}",
    ) {
        Text(stringResource(R.string.tracker_duration_value, "%.1f".format(sessionDurationHours(session))))
        Text(stringResource(R.string.tracker_plan_value, session.targetHours))
        Text(
            if (session.completedTarget) {
                stringResource(R.string.tracker_target_met)
            } else {
                stringResource(R.string.tracker_below_target)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

private fun parseInstant(value: String): Instant? = runCatching { Instant.parse(value) }.getOrNull()

private fun formatDateTime(instant: Instant): String =
    DateTimeFormatter.ofPattern("MMM d, h:mm a")
        .withZone(ZoneId.systemDefault())
        .format(instant)

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    return "${hours}h ${minutes}m"
}

private fun sessionDurationHours(session: IntermittentFastSession): Double {
    val duration =
        parseInstant(session.startIso)?.let { start ->
            parseInstant(session.endIso)?.let { end ->
                Duration.between(start, end)
            }
        }
    return duration?.seconds?.div(3600.0) ?: 0.0
}

private fun defaultWeekdays(): Set<Int> = setOf(2, 4, 6)

private fun weekdayShortLabel(
    context: Context,
    weekday: Int,
): String =
    when (weekday) {
        1 -> context.getString(R.string.tracker_weekday_sun)
        2 -> context.getString(R.string.tracker_weekday_mon)
        3 -> context.getString(R.string.tracker_weekday_tue)
        4 -> context.getString(R.string.tracker_weekday_wed)
        5 -> context.getString(R.string.tracker_weekday_thu)
        6 -> context.getString(R.string.tracker_weekday_fri)
        7 -> context.getString(R.string.tracker_weekday_sat)
        else -> context.getString(R.string.tracker_weekday_unknown)
    }

private fun weekdayListText(
    context: Context,
    weekdays: List<Int>,
): String = weekdays.joinToString { weekdayShortLabel(context, it) }

private fun actionLabelString(
    context: Context,
    resId: Int,
    value: String? = null,
): String = if (value == null) context.getString(resId) else context.getString(resId, value)
