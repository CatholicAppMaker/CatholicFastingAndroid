package com.kevpierce.catholicfasting.feature.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.IntermittentFastSessionRecap
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues

data class TrackerUiState(
    val schedules: List<IntermittentSchedulePlan>,
    val activeScheduleId: String?,
    val sessions: List<IntermittentFastSession>,
    val activeFast: ActiveIntermittentFast?,
    val presetHours: Int,
    val selectedIntentionId: String,
    val latestRecap: IntermittentFastSessionRecap?,
    val premiumSnapshot: PremiumSnapshot,
    val prepGuidance: List<String>,
    val seasonProgramActions: List<String>,
)

data class TrackerActions(
    val onPresetHoursChange: (Int) -> Unit,
    val onIntentionChange: (String) -> Unit,
    val onStartFast: (String) -> Unit,
    val onEndFast: (String?) -> Unit,
    val onCancelFast: () -> Unit,
    val onSaveSchedule: (String?, String, Int, Set<Int>) -> String,
    val onDeleteSchedule: (String) -> String,
    val onApplySchedule: (String) -> String,
)

const val TRACKER_INTENTION_TEST_TAG_PREFIX = "tracker-intention-"
const val TRACKER_REVIEW_NOTE_TEST_TAG = "tracker-review-note"
const val TRACKER_START_FAST_TEST_TAG = "tracker-start-fast"
const val TRACKER_END_FAST_TEST_TAG = "tracker-end-fast"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackerScreen(
    uiState: TrackerUiState,
    actions: TrackerActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var presetInput by remember(uiState.presetHours) { mutableStateOf(uiState.presetHours.toString()) }
    var selectedIntentionId by rememberSaveable(uiState.selectedIntentionId) {
        mutableStateOf(uiState.selectedIntentionId)
    }
    var reviewNote by rememberSaveable(uiState.activeFast?.startIso) { mutableStateOf("") }
    val scheduleDraft = rememberSaveable(saver = ScheduleEditorDraftState.Saver) { ScheduleEditorDraftState() }
    val editorActions =
        scheduleEditorActions(
            context = context,
            uiState = uiState,
            actions = actions,
            draft = scheduleDraft,
            currentPresetInput = { presetInput },
            onPresetInputChange = { presetInput = it },
        )

    TrackerScreenBody(
        uiState = uiState,
        actions = actions,
        scheduleDraft = scheduleDraft,
        editorActions = editorActions,
        presetInput = presetInput,
        selectedIntentionId = selectedIntentionId,
        reviewNote = reviewNote,
        onPresetInputChange = { presetInput = it },
        onIntentionSelected = { selectedIntentionId = it },
        onReviewNoteChange = { reviewNote = it },
        onReviewNoteCleared = { reviewNote = "" },
        modifier = modifier,
    )
}

@Composable
private fun TrackerScreenBody(
    uiState: TrackerUiState,
    actions: TrackerActions,
    scheduleDraft: ScheduleEditorDraftState,
    editorActions: ScheduleEditorActions,
    presetInput: String,
    selectedIntentionId: String,
    reviewNote: String,
    onPresetInputChange: (String) -> Unit,
    onIntentionSelected: (String) -> Unit,
    onReviewNoteChange: (String) -> Unit,
    onReviewNoteCleared: () -> Unit,
    modifier: Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(CatholicFastingThemeValues.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.medium),
    ) {
        item { TrackerTitle() }
        item {
            TrackerActiveFastSection(
                uiState = uiState,
                actions = actions,
                presetInput = presetInput,
                selectedIntentionId = selectedIntentionId,
                reviewNote = reviewNote,
                onPresetInputChange = onPresetInputChange,
                onIntentionSelected = onIntentionSelected,
                onReviewNoteChange = onReviewNoteChange,
                onReviewNoteCleared = onReviewNoteCleared,
            )
        }
        uiState.latestRecap?.let { recap -> item { LatestRecapCard(recap) } }
        item { SchedulesCard(schedules = uiState.schedules) }
        item { TrackerSchedulePlannerSection(uiState, scheduleDraft, editorActions) }
        item { TrackerSessionSummarySection(uiState) }
        item { TrackerSupportSection(uiState) }
        item { SessionHistoryCard(uiState.sessions) }
    }
}

@Composable
private fun TrackerTitle() {
    CatholicFastingScreenTitle(
        text = stringResource(R.string.tracker_title),
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun TrackerActiveFastSection(
    uiState: TrackerUiState,
    actions: TrackerActions,
    presetInput: String,
    selectedIntentionId: String,
    reviewNote: String,
    onPresetInputChange: (String) -> Unit,
    onIntentionSelected: (String) -> Unit,
    onReviewNoteChange: (String) -> Unit,
    onReviewNoteCleared: () -> Unit,
) {
    ActiveFastCard(
        activeFast = uiState.activeFast,
        presetHours = uiState.presetHours,
        presetInput = presetInput,
        selectedIntentionId = selectedIntentionId,
        reviewNote = reviewNote,
        onPresetInputChange = { next ->
            onPresetInputChange(next)
            next.toIntOrNull()?.let(actions.onPresetHoursChange)
        },
        onIntentionChange = { intentionId ->
            onIntentionSelected(intentionId)
            actions.onIntentionChange(intentionId)
        },
        onReviewNoteChange = onReviewNoteChange,
        onStartFast = { actions.onStartFast(selectedIntentionId) },
        onEndFast = {
            actions.onEndFast(reviewNote)
            onReviewNoteCleared()
        },
        onCancelFast = actions.onCancelFast,
    )
}

@Composable
private fun TrackerSchedulePlannerSection(
    uiState: TrackerUiState,
    scheduleDraft: ScheduleEditorDraftState,
    editorActions: ScheduleEditorActions,
) {
    SchedulePlannerCard(
        schedules = uiState.schedules,
        activeScheduleId = uiState.activeScheduleId,
        presetHours = uiState.presetHours,
        editorState = scheduleDraft.uiState,
        editorActions = editorActions,
    )
}

@Composable
private fun TrackerSessionSummarySection(uiState: TrackerUiState) {
    SessionSummaryCard(
        sessions = uiState.sessions,
        activeSchedule = uiState.schedules.firstOrNull { it.id == uiState.activeScheduleId },
    )
}

@Composable
private fun TrackerSupportSection(uiState: TrackerUiState) {
    TrackerSupportCard(
        premiumSnapshot = uiState.premiumSnapshot,
        prepGuidance = uiState.prepGuidance,
        seasonProgramActions = uiState.seasonProgramActions,
    )
}
