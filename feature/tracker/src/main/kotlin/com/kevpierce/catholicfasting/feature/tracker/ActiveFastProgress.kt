package com.kevpierce.catholicfasting.feature.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.IntermittentFastSessionRecap
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import java.time.Duration
import java.time.Instant

internal data class ActiveFastProgress(
    val elapsedSeconds: Long,
    val remainingSeconds: Long,
    val targetSeconds: Long,
    val progress: Float,
)

internal fun activeFastProgress(
    startIso: String,
    targetHours: Int,
    now: Instant,
): ActiveFastProgress {
    val start = parseInstant(startIso)
    val elapsedSeconds =
        start
            ?.let { Duration.between(it, now).seconds.coerceAtLeast(0L) }
            ?: 0L
    val targetSeconds = targetHours.toLong().coerceAtLeast(0L) * 3600L
    val remainingSeconds = (targetSeconds - elapsedSeconds).coerceAtLeast(0L)
    val progress =
        if (targetSeconds == 0L) {
            0f
        } else {
            (elapsedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
        }
    return ActiveFastProgress(
        elapsedSeconds = elapsedSeconds,
        remainingSeconds = remainingSeconds,
        targetSeconds = targetSeconds,
        progress = progress,
    )
}

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
    now: Instant,
    onPresetInputChange: (String) -> Unit,
    onIntentionChange: (String) -> Unit,
    onReviewNoteChange: (String) -> Unit,
    onStartFast: () -> Unit,
    onEndFast: () -> Unit,
    onCancelFast: () -> Unit,
) {
    CatholicFastingSectionCard(title = stringResource(R.string.tracker_control_center)) {
        if (activeFast == null) {
            Text(
                stringResource(R.string.tracker_ready_title),
                style = CatholicFastingThemeValues.typography.sectionTitle,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(R.string.tracker_no_active_fast),
                style = CatholicFastingThemeValues.typography.body,
            )
        } else {
            val progress = activeFastProgress(activeFast.startIso, activeFast.targetHours, now)
            ActiveFastProgressHero(activeFast, progress)
            ActiveFastSummary(activeFast, progress)
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
private fun ActiveFastProgressHero(
    activeFast: ActiveIntermittentFast,
    progress: ActiveFastProgress,
) {
    val remainingDuration = Duration.ofSeconds(progress.remainingSeconds)
    val elapsedDuration = Duration.ofSeconds(progress.elapsedSeconds)
    val remainingHours = remainingDuration.toHours()
    val remainingMinutes = remainingDuration.minusHours(remainingHours).toMinutes()
    val progressDescription =
        stringResource(
            R.string.tracker_progress_accessibility,
            formatDuration(remainingDuration),
            activeFast.targetHours,
            formatDuration(elapsedDuration),
        )

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        val diameter = minOf(maxWidth, 188.dp)
        Box(
            modifier =
                Modifier
                    .size(diameter)
                    .align(Alignment.Center)
                    .testTag(TRACKER_PROGRESS_TEST_TAG)
                    .semantics(mergeDescendants = true) {
                        progressBarRangeInfo = ProgressBarRangeInfo(progress.progress, 0f..1f)
                        stateDescription = progressDescription
                    },
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { progress.progress },
                modifier = Modifier.size(diameter).clearAndSetSemantics { },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeWidth = 12.dp,
            )
            Column(
                modifier = Modifier.width(diameter * 0.75f).clearAndSetSemantics { },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.tracker_remaining),
                    style = CatholicFastingThemeValues.typography.utility,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.tracker_hours_short_value, remainingHours),
                    style = CatholicFastingThemeValues.typography.heroTitle,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.tracker_minutes_short_value, remainingMinutes),
                    style = CatholicFastingThemeValues.typography.body,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
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
private fun ActiveFastSummary(
    activeFast: ActiveIntermittentFast,
    progress: ActiveFastProgress,
) {
    val start = parseInstant(activeFast.startIso)
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
            if (start == null) {
                stringResource(R.string.tracker_unavailable)
            } else {
                formatDuration(Duration.ofSeconds(progress.elapsedSeconds))
            },
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
