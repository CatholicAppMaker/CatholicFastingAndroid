package com.kevpierce.catholicfasting.feature.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.kevpierce.catholicfasting.core.model.CalendarWindow
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceFilter
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceSortOrder
import com.kevpierce.catholicfasting.core.rules.ObservanceQueryEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.catholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.catholicFastingSectionCard
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun calendarScreen(
    observances: List<Observance>,
    statusesById: Map<String, CompletionStatus>,
    fridayNotesById: Map<String, String>,
    premiumSnapshot: PremiumSnapshot,
    onStatusChange: (String, CompletionStatus) -> Unit,
    onFridayNoteChange: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = CatholicFastingThemeValues.spacing
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ObservanceFilter.REQUIRED_ONLY) }
    var window by rememberSaveable { mutableStateOf(CalendarWindow.ALL_YEAR) }
    var sortOrder by rememberSaveable { mutableStateOf(ObservanceSortOrder.CHRONOLOGICAL) }
    val visibleObservances =
        remember(observances, statusesById, query, filter, window, sortOrder) {
            ObservanceQueryEngine.filter(
                observances = observances,
                query = query,
                filter = filter,
                window = window,
                sortOrder = sortOrder,
                statusesById = statusesById,
                now = LocalDate.now(),
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        catholicFastingScreenTitle(stringResource(R.string.calendar_title))
        analyticsSummaryCard(premiumSnapshot)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.calendar_search_label)) },
        )
        calendarFilters(
            filter = filter,
            window = window,
            sortOrder = sortOrder,
            onFilterChange = { filter = it },
            onWindowChange = { window = it },
            onSortOrderChange = { sortOrder = it },
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.xSmall)) {
            items(visibleObservances.take(36), key = { it.id }) { observance ->
                observanceCard(
                    observance = observance,
                    selectedStatus = statusesById[observance.id] ?: CompletionStatus.NOT_STARTED,
                    fridayNote = fridayNotesById[observance.id].orEmpty(),
                    onStatusChange = onStatusChange,
                    onFridayNoteChange = onFridayNoteChange,
                )
            }
        }
    }
}

@Composable
private fun analyticsSummaryCard(premiumSnapshot: PremiumSnapshot) {
    catholicFastingSectionCard(title = stringResource(R.string.calendar_progress_overview)) {
        Text(premiumSnapshot.motivationLine, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(
                R.string.calendar_required_overall_value,
                premiumSnapshot.analyticsSummary.requiredCompletionPercent,
                premiumSnapshot.analyticsSummary.overallCompletionPercent,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.calendar_missed_substituted_value,
                premiumSnapshot.analyticsSummary.missedCount,
                premiumSnapshot.analyticsSummary.substitutedCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            premiumSnapshot.reminderRecommendation.summaryLine,
            style = CatholicFastingThemeValues.typography.supporting,
        )
        premiumSnapshot.analyticsSummary.seasonRows.take(3).forEach { row ->
            Text(
                stringResource(
                    R.string.calendar_season_percent_value,
                    row.season.localizedLabel(),
                    row.completionPercent,
                ),
                style = CatholicFastingThemeValues.typography.utility,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun calendarFilters(
    filter: ObservanceFilter,
    window: CalendarWindow,
    sortOrder: ObservanceSortOrder,
    onFilterChange: (ObservanceFilter) -> Unit,
    onWindowChange: (CalendarWindow) -> Unit,
    onSortOrderChange: (ObservanceSortOrder) -> Unit,
) {
    val spacing = CatholicFastingThemeValues.spacing
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        ObservanceFilter.entries.forEach { entry ->
            calendarSelectableChip(
                label = entry.localizedLabel(),
                selected = filter == entry,
                onClick = { onFilterChange(entry) },
            )
        }
        CalendarWindow.entries.forEach { entry ->
            calendarSelectableChip(
                label = entry.localizedLabel(),
                selected = window == entry,
                onClick = { onWindowChange(entry) },
            )
        }
        ObservanceSortOrder.entries.forEach { entry ->
            calendarSelectableChip(
                label = entry.localizedLabel(),
                selected = sortOrder == entry,
                onClick = { onSortOrderChange(entry) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun observanceCard(
    observance: Observance,
    selectedStatus: CompletionStatus,
    fridayNote: String,
    onStatusChange: (String, CompletionStatus) -> Unit,
    onFridayNoteChange: (String, String) -> Unit,
) {
    catholicFastingSectionCard(title = observance.title) {
        observanceHeader(observance)
        observanceCitations(observance)
        completionStatusChips(
            observanceId = observance.id,
            selectedStatus = selectedStatus,
            onStatusChange = onStatusChange,
        )
        if (observance.kind == ObservanceKind.FRIDAY_PENANCE) {
            fridayNoteField(
                observanceId = observance.id,
                initialValue = fridayNote,
                onFridayNoteChange = onFridayNoteChange,
            )
        }
    }
}

@Composable
private fun observanceHeader(observance: Observance) {
    Text(observance.date, style = CatholicFastingThemeValues.typography.supporting)
    Text(
        stringResource(
            R.string.calendar_kind_obligation_value,
            observance.kind.localizedLabel(),
            observance.obligation.localizedLabel(),
        ),
        style = CatholicFastingThemeValues.typography.supporting,
    )
    observance.detail?.let { Text(it, style = CatholicFastingThemeValues.typography.body) }
    Text(observance.rationale, style = CatholicFastingThemeValues.typography.utility)
}

@Composable
private fun observanceCitations(observance: Observance) {
    observance.citations.forEach { citation ->
        Text(
            stringResource(
                R.string.calendar_citation_value,
                citation.authority.localizedLabel(),
                citation.title,
                citation.shortReference,
            ),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun completionStatusChips(
    observanceId: String,
    selectedStatus: CompletionStatus,
    onStatusChange: (String, CompletionStatus) -> Unit,
) {
    val spacing = CatholicFastingThemeValues.spacing
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        CompletionStatus.entries.forEach { status ->
            calendarSelectableChip(
                label = status.localizedLabel(),
                selected = selectedStatus == status,
                onClick = { onStatusChange(observanceId, status) },
            )
        }
    }
}

@Composable
private fun calendarSelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedState =
        stringResource(
            if (selected) {
                R.string.calendar_accessibility_selected
            } else {
                R.string.calendar_accessibility_not_selected
            },
        )
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier =
            Modifier.semantics {
                contentDescription = label
                stateDescription = selectedState
            },
        label = { Text(label) },
    )
}

@Composable
private fun fridayNoteField(
    observanceId: String,
    initialValue: String,
    onFridayNoteChange: (String, String) -> Unit,
) {
    var note by rememberSaveable(observanceId, initialValue) {
        mutableStateOf(initialValue)
    }
    OutlinedTextField(
        value = note,
        onValueChange = {
            note = it
            onFridayNoteChange(observanceId, it)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.calendar_friday_note_label)) },
        supportingText = {
            Text(stringResource(R.string.calendar_friday_note_supporting))
        },
    )
}
