package com.kevpierce.catholicfasting.feature.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues

@Composable
internal fun SessionHistoryCard(sessions: List<IntermittentFastSession>) {
    CatholicFastingSectionCard(title = stringResource(R.string.tracker_recent_sessions)) {
        if (sessions.isEmpty()) {
            Text(
                text = stringResource(R.string.tracker_no_sessions),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        } else {
            sessions.take(12).forEachIndexed { index, session ->
                if (index > 0) {
                    HorizontalDivider()
                }
                SessionRow(session)
            }
        }
    }
}

@Composable
internal fun SessionSummaryCard(
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

    CatholicFastingSectionCard(title = stringResource(R.string.tracker_recent_summary)) {
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
private fun SessionRow(session: IntermittentFastSession) {
    val start = parseInstant(session.startIso)
    val end = parseInstant(session.endIso)
    val spacing = CatholicFastingThemeValues.spacing
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xxSmall),
    ) {
        Text(
            text =
                "${start?.let(::formatDateTime) ?: session.startIso} -> " +
                    "${end?.let(::formatDateTime) ?: session.endIso}",
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
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
        intentionFor(session.intentionId)?.let { intention ->
            Text(
                stringResource(R.string.tracker_intention_value, intention.label),
                style = CatholicFastingThemeValues.typography.utility,
            )
        }
        session.reviewNote?.let { note ->
            Text(
                stringResource(R.string.tracker_review_note_value, note),
                style = CatholicFastingThemeValues.typography.utility,
            )
        }
    }
}
