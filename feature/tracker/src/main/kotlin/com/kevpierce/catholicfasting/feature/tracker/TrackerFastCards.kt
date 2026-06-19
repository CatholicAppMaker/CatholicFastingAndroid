package com.kevpierce.catholicfasting.feature.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.IntermittentFastSessionRecap
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import java.time.Duration
import java.time.Instant

@Composable
internal fun TrackerSupportCard(
    premiumSnapshot: PremiumSnapshot,
    prepGuidance: List<String>,
    seasonProgramActions: List<String>,
) {
    CatholicFastingSectionCard(title = stringResource(R.string.tracker_preparation_recovery)) {
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
internal fun ActiveFastCard(
    activeFast: ActiveIntermittentFast?,
    presetHours: Int,
    presetInput: String,
    selectedIntentionId: String,
    reviewNote: String,
    onPresetInputChange: (String) -> Unit,
    onIntentionChange: (String) -> Unit,
    onReviewNoteChange: (String) -> Unit,
    onStartFast: () -> Unit,
    onEndFast: () -> Unit,
    onCancelFast: () -> Unit,
) {
    CatholicFastingSectionCard(title = stringResource(R.string.tracker_control_center)) {
        if (activeFast == null) {
            Text(stringResource(R.string.tracker_no_active_fast))
        } else {
            ActiveFastSummary(activeFast)
        }
        IntentionPicker(
            selectedIntentionId = activeFast?.intentionId ?: selectedIntentionId,
            enabled = activeFast == null,
            onIntentionChange = onIntentionChange,
        )
        TargetHoursField(
            presetHours = presetHours,
            presetInput = presetInput,
            enabled = activeFast == null,
            onPresetInputChange = onPresetInputChange,
        )
        if (activeFast != null) {
            ReviewNoteField(
                reviewNote = reviewNote,
                onReviewNoteChange = onReviewNoteChange,
            )
        }
        ActiveFastActions(
            activeFast = activeFast,
            onStartFast = onStartFast,
            onEndFast = onEndFast,
            onCancelFast = onCancelFast,
        )
    }
}

@Composable
private fun TargetHoursField(
    presetHours: Int,
    presetInput: String,
    enabled: Boolean,
    onPresetInputChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = presetInput,
        onValueChange = onPresetInputChange,
        label = { Text(stringResource(R.string.tracker_target_hours_label)) },
        supportingText = { Text(stringResource(R.string.tracker_target_hours_supporting)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.tracker_current_preset_value, presetHours),
        style = CatholicFastingThemeValues.typography.supporting,
    )
}

@Composable
private fun ReviewNoteField(
    reviewNote: String,
    onReviewNoteChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = reviewNote,
        onValueChange = onReviewNoteChange,
        label = { Text(stringResource(R.string.tracker_review_note_label)) },
        supportingText = { Text(stringResource(R.string.tracker_review_note_supporting)) },
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(TRACKER_REVIEW_NOTE_TEST_TAG),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveFastActions(
    activeFast: ActiveIntermittentFast?,
    onStartFast: () -> Unit,
    onEndFast: () -> Unit,
    onCancelFast: () -> Unit,
) {
    val spacing = CatholicFastingThemeValues.spacing
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        if (activeFast == null) {
            Button(
                onClick = onStartFast,
                modifier = Modifier.testTag(TRACKER_START_FAST_TEST_TAG),
            ) {
                Text(stringResource(R.string.tracker_start_fast))
            }
        } else {
            Button(
                onClick = onEndFast,
                modifier = Modifier.testTag(TRACKER_END_FAST_TEST_TAG),
            ) {
                Text(stringResource(R.string.tracker_end_fast))
            }
            OutlinedButton(onClick = onCancelFast) {
                Text(stringResource(R.string.tracker_cancel))
            }
        }
    }
}

@Composable
private fun ActiveFastSummary(activeFast: ActiveIntermittentFast) {
    val start = parseInstant(activeFast.startIso)
    val elapsed = start?.let { Duration.between(it, Instant.now()) }
    val intention = intentionFor(activeFast.intentionId)
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
    Text(
        pluralStringResource(
            R.plurals.tracker_target_hours_value,
            activeFast.targetHours,
            activeFast.targetHours,
        ),
    )
    intention?.let {
        Text(
            stringResource(R.string.tracker_intention_value, it.label),
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntentionPicker(
    selectedIntentionId: String,
    enabled: Boolean,
    onIntentionChange: (String) -> Unit,
) {
    val spacing = CatholicFastingThemeValues.spacing
    val selectedIntention = intentionFor(selectedIntentionId) ?: IntermittentFastIntention.PERSONAL_DISCIPLINE
    Text(stringResource(R.string.tracker_intention_title), style = CatholicFastingThemeValues.typography.sectionTitle)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        IntermittentFastIntention.entries.forEach { intention ->
            val selected = intention.name == selectedIntention.name
            val selectedState = trackerSelectedStateDescription(selected)
            FilterChip(
                selected = selected,
                enabled = enabled,
                onClick = { onIntentionChange(intention.name) },
                modifier =
                    Modifier
                        .testTag(TRACKER_INTENTION_TEST_TAG_PREFIX + intention.name)
                        .semantics {
                            contentDescription = intention.label
                            stateDescription = selectedState
                        },
                label = { Text(intention.label) },
            )
        }
    }
    Text(selectedIntention.detail, style = CatholicFastingThemeValues.typography.supporting)
}

@Composable
internal fun LatestRecapCard(recap: IntermittentFastSessionRecap) {
    CatholicFastingSectionCard(title = stringResource(R.string.tracker_latest_recap_title)) {
        Text(recap.title, style = CatholicFastingThemeValues.typography.sectionTitle)
        Text(
            stringResource(
                R.string.tracker_recap_duration_value,
                "%.1f".format(recap.durationHours),
                recap.targetHours,
            ),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(recap.encouragement, style = CatholicFastingThemeValues.typography.supporting)
        Text(recap.suggestedNextAction, style = CatholicFastingThemeValues.typography.supporting)
        intentionFor(recap.intentionId)?.let { intention ->
            Text(
                stringResource(R.string.tracker_intention_value, intention.label),
                style = CatholicFastingThemeValues.typography.utility,
            )
        }
        recap.reviewNote?.let { note ->
            Text(
                stringResource(R.string.tracker_review_note_value, note),
                style = CatholicFastingThemeValues.typography.utility,
            )
        }
    }
}

@Composable
internal fun SchedulesCard(schedules: List<IntermittentSchedulePlan>) {
    CatholicFastingSectionCard(title = stringResource(R.string.tracker_saved_schedules)) {
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
