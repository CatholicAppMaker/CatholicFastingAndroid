package com.kevpierce.catholicfasting.feature.tracker

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun trackerSelectedStateDescription(selected: Boolean): String =
    stringResource(
        if (selected) {
            R.string.tracker_accessibility_selected
        } else {
            R.string.tracker_accessibility_not_selected
        },
    )

internal fun parseInstant(value: String): Instant? = runCatching { Instant.parse(value) }.getOrNull()

internal fun formatDateTime(instant: Instant): String =
    DateTimeFormatter
        .ofPattern("MMM d, h:mm a")
        .withZone(ZoneId.systemDefault())
        .format(instant)

internal fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    return "${hours}h ${minutes}m"
}

internal fun sessionDurationHours(session: IntermittentFastSession): Double {
    val duration =
        parseInstant(session.startIso)?.let { start ->
            parseInstant(session.endIso)?.let { end ->
                Duration.between(start, end)
            }
        }
    return duration?.seconds?.div(3600.0) ?: 0.0
}

internal fun intentionFor(intentionId: String?): IntermittentFastIntention? =
    IntermittentFastIntention.entries.firstOrNull { it.name == intentionId }

internal fun weekdayShortLabel(
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

internal fun weekdayListText(
    context: Context,
    weekdays: List<Int>,
): String = weekdays.joinToString { weekdayShortLabel(context, it) }

internal fun actionLabelString(
    context: Context,
    resId: Int,
    value: String? = null,
): String = if (value == null) context.getString(resId) else context.getString(resId, value)
