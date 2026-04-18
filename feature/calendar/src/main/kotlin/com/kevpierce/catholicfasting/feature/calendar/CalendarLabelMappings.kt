package com.kevpierce.catholicfasting.feature.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.CalendarWindow
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import com.kevpierce.catholicfasting.core.model.ObservanceFilter
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.ObservanceSortOrder
import com.kevpierce.catholicfasting.core.model.RuleAuthority

@Composable
internal fun ObservanceFilter.localizedLabel(): String =
    when (this) {
        ObservanceFilter.ALL -> stringResource(R.string.calendar_label_filter_all)
        ObservanceFilter.REQUIRED_ONLY -> stringResource(R.string.calendar_label_filter_required)
        ObservanceFilter.TRACKED_ONLY -> stringResource(R.string.calendar_label_filter_tracked)
    }

@Composable
internal fun CalendarWindow.localizedLabel(): String =
    when (this) {
        CalendarWindow.ALL_YEAR -> stringResource(R.string.calendar_label_window_all_year)
        CalendarWindow.THIS_MONTH -> stringResource(R.string.calendar_label_window_this_month)
        CalendarWindow.NEXT_30_DAYS -> stringResource(R.string.calendar_label_window_next_30_days)
    }

@Composable
internal fun ObservanceSortOrder.localizedLabel(): String =
    when (this) {
        ObservanceSortOrder.CHRONOLOGICAL -> stringResource(R.string.calendar_label_sort_date)
        ObservanceSortOrder.REQUIRED_FIRST -> stringResource(R.string.calendar_label_sort_required_first)
    }

@Composable
internal fun CompletionStatus.localizedLabel(): String =
    when (this) {
        CompletionStatus.NOT_STARTED -> stringResource(R.string.calendar_label_status_not_started)
        CompletionStatus.COMPLETED -> stringResource(R.string.calendar_label_status_completed)
        CompletionStatus.SUBSTITUTED -> stringResource(R.string.calendar_label_status_substituted)
        CompletionStatus.DISPENSED -> stringResource(R.string.calendar_label_status_dispensed)
        CompletionStatus.MISSED -> stringResource(R.string.calendar_label_status_missed)
    }

@Composable
internal fun ObservanceKind.localizedLabel(): String =
    when (this) {
        ObservanceKind.FAST_AND_ABSTINENCE -> stringResource(R.string.calendar_label_kind_fast_abstinence)
        ObservanceKind.ABSTINENCE -> stringResource(R.string.calendar_label_kind_abstinence)
        ObservanceKind.FRIDAY_PENANCE -> stringResource(R.string.calendar_label_kind_friday_penance)
        ObservanceKind.HOLY_DAY -> stringResource(R.string.calendar_label_kind_holy_day)
        ObservanceKind.FEAST_DAY -> stringResource(R.string.calendar_label_kind_feast_day)
        ObservanceKind.MEMORIAL_DAY -> stringResource(R.string.calendar_label_kind_memorial)
        ObservanceKind.OPTIONAL_EMBER -> stringResource(R.string.calendar_label_kind_ember)
    }

@Composable
internal fun ObservanceObligation.localizedLabel(): String =
    when (this) {
        ObservanceObligation.MANDATORY -> stringResource(R.string.calendar_label_obligation_required)
        ObservanceObligation.OPTIONAL -> stringResource(R.string.calendar_label_obligation_optional)
        ObservanceObligation.NOT_APPLICABLE -> stringResource(R.string.calendar_label_obligation_not_required)
    }

@Composable
internal fun RuleAuthority.localizedLabel(): String =
    when (this) {
        RuleAuthority.UNIVERSAL_LAW -> stringResource(R.string.calendar_label_authority_universal)
        RuleAuthority.USCCB -> stringResource(R.string.calendar_label_authority_usccb)
        RuleAuthority.CCCB -> stringResource(R.string.calendar_label_authority_cccb)
        RuleAuthority.PASTORAL -> stringResource(R.string.calendar_label_authority_pastoral)
    }

@Composable
internal fun LiturgicalSeason.localizedLabel(): String =
    when (this) {
        LiturgicalSeason.ADVENT -> stringResource(R.string.calendar_label_season_advent)
        LiturgicalSeason.CHRISTMAS -> stringResource(R.string.calendar_label_season_christmas)
        LiturgicalSeason.LENT -> stringResource(R.string.calendar_label_season_lent)
        LiturgicalSeason.EASTER -> stringResource(R.string.calendar_label_season_easter)
        LiturgicalSeason.ORDINARY -> stringResource(R.string.calendar_label_season_ordinary)
    }
