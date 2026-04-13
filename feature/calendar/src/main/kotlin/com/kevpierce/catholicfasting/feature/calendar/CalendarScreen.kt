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
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.model.CalendarWindow
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceFilter
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceSortOrder
import com.kevpierce.catholicfasting.core.rules.ObservanceQueryEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
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
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.calendar_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.calendar_progress_overview), style = MaterialTheme.typography.titleLarge)
            Text(premiumSnapshot.motivationLine)
            Text(
                stringResource(
                    R.string.calendar_required_overall_value,
                    premiumSnapshot.analyticsSummary.requiredCompletionPercent,
                    premiumSnapshot.analyticsSummary.overallCompletionPercent,
                ),
            )
            Text(
                stringResource(
                    R.string.calendar_missed_substituted_value,
                    premiumSnapshot.analyticsSummary.missedCount,
                    premiumSnapshot.analyticsSummary.substitutedCount,
                ),
            )
            Text(premiumSnapshot.reminderRecommendation.summaryLine)
            premiumSnapshot.analyticsSummary.seasonRows.take(3).forEach { row ->
                Text(stringResource(R.string.calendar_season_percent_value, row.season.label, row.completionPercent))
            }
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
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ObservanceFilter.entries.forEach { entry ->
            FilterChip(
                selected = filter == entry,
                onClick = { onFilterChange(entry) },
                label = { Text(entry.label) },
            )
        }
        CalendarWindow.entries.forEach { entry ->
            FilterChip(
                selected = window == entry,
                onClick = { onWindowChange(entry) },
                label = { Text(entry.label) },
            )
        }
        ObservanceSortOrder.entries.forEach { entry ->
            FilterChip(
                selected = sortOrder == entry,
                onClick = { onSortOrderChange(entry) },
                label = { Text(entry.label) },
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
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(observance.title, style = MaterialTheme.typography.titleMedium)
            Text(observance.date)
            Text(
                stringResource(
                    R.string.calendar_kind_obligation_value,
                    observance.kind.label,
                    observance.obligation.label,
                ),
            )
            observance.detail?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text(observance.rationale, style = MaterialTheme.typography.bodySmall)
            observance.citations.forEach { citation ->
                Text(
                    stringResource(
                        R.string.calendar_citation_value,
                        citation.authority.label,
                        citation.title,
                        citation.shortReference,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompletionStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusChange(observance.id, status) },
                        label = { Text(status.label) },
                    )
                }
            }
            if (observance.kind == ObservanceKind.FRIDAY_PENANCE) {
                fridayNoteField(
                    observanceId = observance.id,
                    initialValue = fridayNote,
                    onFridayNoteChange = onFridayNoteChange,
                )
            }
        }
    }
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
