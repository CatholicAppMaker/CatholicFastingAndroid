package com.kevpierce.catholicfasting.feature.tracker

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues

internal data class ScheduleEditorUiState(
    val editingScheduleId: String?,
    val scheduleName: String,
    val scheduleStartHourInput: String,
    val selectedWeekdays: Set<Int>,
    val statusMessage: String,
)

internal data class ScheduleEditorActions(
    val onScheduleNameChange: (String) -> Unit,
    val onScheduleStartHourChange: (String) -> Unit,
    val onToggleWeekday: (Int) -> Unit,
    val onApplySchedule: (String) -> Unit,
    val onEditSchedule: (IntermittentSchedulePlan) -> Unit,
    val onDeleteSchedule: (String) -> Unit,
    val onSaveSchedule: () -> Unit,
    val onCancelEdit: () -> Unit,
)

internal class ScheduleEditorDraftState(
    editingScheduleId: String? = null,
    scheduleName: String = "",
    scheduleStartHourInput: String = DEFAULT_SCHEDULE_START_HOUR,
    selectedWeekdays: Set<Int> = defaultWeekdays(),
    statusMessage: String = "",
) {
    var editingScheduleId by mutableStateOf(editingScheduleId)
    var scheduleName by mutableStateOf(scheduleName)
    var scheduleStartHourInput by mutableStateOf(scheduleStartHourInput)
    var selectedWeekdays by mutableStateOf(selectedWeekdays)
    var statusMessage by mutableStateOf(statusMessage)

    val uiState: ScheduleEditorUiState
        get() =
            ScheduleEditorUiState(
                editingScheduleId = editingScheduleId,
                scheduleName = scheduleName,
                scheduleStartHourInput = scheduleStartHourInput,
                selectedWeekdays = selectedWeekdays,
                statusMessage = statusMessage,
            )

    fun clearEditor() {
        editingScheduleId = null
        scheduleName = ""
        scheduleStartHourInput = DEFAULT_SCHEDULE_START_HOUR
        selectedWeekdays = defaultWeekdays()
    }

    fun toggleWeekday(weekday: Int) {
        selectedWeekdays =
            if (selectedWeekdays.contains(weekday)) {
                selectedWeekdays - weekday
            } else {
                selectedWeekdays + weekday
            }
    }

    fun edit(plan: IntermittentSchedulePlan) {
        editingScheduleId = plan.id
        scheduleName = plan.name
        scheduleStartHourInput = plan.startHour.toString()
        selectedWeekdays = plan.weekdays.toSet()
    }

    companion object {
        val Saver: Saver<ScheduleEditorDraftState, Any> =
            listSaver(
                save = { draft ->
                    listOf(
                        draft.editingScheduleId.orEmpty(),
                        draft.scheduleName,
                        draft.scheduleStartHourInput,
                        draft.selectedWeekdays.joinToString(separator = ","),
                        draft.statusMessage,
                    )
                },
                restore = { values ->
                    ScheduleEditorDraftState(
                        editingScheduleId = values[0].ifBlank { null },
                        scheduleName = values[1],
                        scheduleStartHourInput = values[2],
                        selectedWeekdays = parseWeekdayCsv(values[3]),
                        statusMessage = values[4],
                    )
                },
            )
    }
}

private const val DEFAULT_SCHEDULE_START_HOUR = "20"

internal fun scheduleEditorActions(
    context: Context,
    uiState: TrackerUiState,
    actions: TrackerActions,
    draft: ScheduleEditorDraftState,
    currentPresetInput: () -> String,
    onPresetInputChange: (String) -> Unit,
): ScheduleEditorActions =
    ScheduleEditorActions(
        onScheduleNameChange = { draft.scheduleName = it },
        onScheduleStartHourChange = { draft.scheduleStartHourInput = it },
        onToggleWeekday = draft::toggleWeekday,
        onApplySchedule = { scheduleId ->
            draft.statusMessage = applySchedule(uiState, actions, scheduleId, currentPresetInput, onPresetInputChange)
        },
        onEditSchedule = { plan ->
            startEditingSchedule(context, actions, draft, onPresetInputChange, plan)
        },
        onDeleteSchedule = { scheduleId ->
            deleteSchedule(actions, draft, scheduleId)
        },
        onSaveSchedule = {
            saveSchedule(actions, draft)
        },
        onCancelEdit = {
            draft.clearEditor()
            draft.statusMessage = actionLabelString(context, R.string.tracker_schedule_edit_canceled)
        },
    )

private fun applySchedule(
    uiState: TrackerUiState,
    actions: TrackerActions,
    scheduleId: String,
    currentPresetInput: () -> String,
    onPresetInputChange: (String) -> Unit,
): String {
    val targetHours =
        uiState.schedules
            .firstOrNull { it.id == scheduleId }
            ?.targetHours
            ?.toString()
            ?: currentPresetInput()
    onPresetInputChange(targetHours)
    return actions.onApplySchedule(scheduleId)
}

private fun startEditingSchedule(
    context: Context,
    actions: TrackerActions,
    draft: ScheduleEditorDraftState,
    onPresetInputChange: (String) -> Unit,
    plan: IntermittentSchedulePlan,
) {
    draft.edit(plan)
    onPresetInputChange(plan.targetHours.toString())
    actions.onPresetHoursChange(plan.targetHours)
    draft.statusMessage =
        actionLabelString(
            context = context,
            resId = R.string.tracker_editing_value,
            value = plan.name,
        )
}

private fun deleteSchedule(
    actions: TrackerActions,
    draft: ScheduleEditorDraftState,
    scheduleId: String,
) {
    draft.statusMessage = actions.onDeleteSchedule(scheduleId)
    if (draft.editingScheduleId == scheduleId) {
        draft.clearEditor()
    }
}

private fun saveSchedule(
    actions: TrackerActions,
    draft: ScheduleEditorDraftState,
) {
    val startHour = draft.scheduleStartHourInput.toIntOrNull() ?: DEFAULT_SCHEDULE_START_HOUR.toInt()
    draft.statusMessage =
        actions.onSaveSchedule(
            draft.editingScheduleId,
            draft.scheduleName,
            startHour,
            draft.selectedWeekdays,
        )
    draft.clearEditor()
}

private fun parseWeekdayCsv(value: String): Set<Int> =
    value
        .split(',')
        .mapNotNull { it.toIntOrNull() }
        .toSet()
        .ifEmpty { defaultWeekdays() }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SchedulePlannerCard(
    schedules: List<IntermittentSchedulePlan>,
    activeScheduleId: String?,
    presetHours: Int,
    editorState: ScheduleEditorUiState,
    editorActions: ScheduleEditorActions,
) {
    CatholicFastingSectionCard(title = stringResource(R.string.tracker_custom_schedules)) {
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
        WeekdaySelector(
            selectedWeekdays = editorState.selectedWeekdays,
            onToggleWeekday = editorActions.onToggleWeekday,
        )
        ScheduleEditorActions(
            editingScheduleId = editorState.editingScheduleId,
            canSave = editorState.selectedWeekdays.isNotEmpty(),
            onSaveSchedule = editorActions.onSaveSchedule,
            onCancelEdit = editorActions.onCancelEdit,
        )
        if (schedules.isEmpty()) {
            Text(stringResource(R.string.tracker_no_saved_schedules))
        } else {
            schedules.forEachIndexed { index, schedule ->
                if (index > 0) {
                    HorizontalDivider()
                }
                ScheduleRow(
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
private fun WeekdaySelector(
    selectedWeekdays: Set<Int>,
    onToggleWeekday: (Int) -> Unit,
) {
    val context = LocalContext.current
    val spacing = CatholicFastingThemeValues.spacing
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        for (weekday in 1..7) {
            val label = weekdayShortLabel(context, weekday)
            val selectedState = trackerSelectedStateDescription(selectedWeekdays.contains(weekday))
            FilterChip(
                selected = selectedWeekdays.contains(weekday),
                onClick = { onToggleWeekday(weekday) },
                modifier =
                    Modifier.semantics {
                        stateDescription = selectedState
                    },
                label = { Text(label) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleEditorActions(
    editingScheduleId: String?,
    canSave: Boolean,
    onSaveSchedule: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    val spacing = CatholicFastingThemeValues.spacing
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleRow(
    schedule: IntermittentSchedulePlan,
    isActive: Boolean,
    onApply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val spacing = CatholicFastingThemeValues.spacing
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        Text(
            text =
                if (isActive) {
                    stringResource(R.string.tracker_applied_schedule_value, schedule.name)
                } else {
                    schedule.name
                },
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        Text(
            stringResource(
                R.string.tracker_schedule_row_value,
                schedule.targetHours,
                "%02d:00".format(schedule.startHour),
                weekdayListText(context, schedule.weekdays),
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
            verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
        ) {
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

private fun defaultWeekdays(): Set<Int> = setOf(2, 4, 6)
