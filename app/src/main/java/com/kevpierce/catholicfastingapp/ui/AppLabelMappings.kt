package com.kevpierce.catholicfastingapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.CalendarMode
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfastingapp.R

@Composable
internal fun RegionProfile.localizedLabel(): String =
    when (this) {
        RegionProfile.US -> stringResource(R.string.label_region_us)
        RegionProfile.CANADA -> stringResource(R.string.label_region_canada)
        RegionProfile.OTHER -> stringResource(R.string.label_region_other)
    }

@Composable
internal fun CalendarMode.localizedLabel(): String =
    when (this) {
        CalendarMode.USCCB -> stringResource(R.string.label_calendar_usccb)
        CalendarMode.TRADITIONAL_1962 -> stringResource(R.string.label_calendar_traditional)
    }

@Composable
internal fun FridayOutsideLentMode.localizedLabel(): String =
    when (this) {
        FridayOutsideLentMode.ABSTAIN_FROM_MEAT -> stringResource(R.string.label_friday_abstain)
        FridayOutsideLentMode.SUBSTITUTE_PENANCE -> stringResource(R.string.label_friday_substitute)
    }

@Composable
internal fun ReminderTier.localizedLabel(): String =
    when (this) {
        ReminderTier.MINIMAL -> stringResource(R.string.label_reminder_minimal)
        ReminderTier.BALANCED -> stringResource(R.string.label_reminder_balanced)
        ReminderTier.GUIDED -> stringResource(R.string.label_reminder_guided)
    }

@Composable
internal fun ReminderTier.localizedSummary(): String =
    when (this) {
        ReminderTier.MINIMAL -> stringResource(R.string.label_reminder_minimal_summary)
        ReminderTier.BALANCED -> stringResource(R.string.label_reminder_balanced_summary)
        ReminderTier.GUIDED -> stringResource(R.string.label_reminder_guided_summary)
    }

@Composable
internal fun selectedStateDescription(selected: Boolean): String =
    stringResource(
        if (selected) {
            R.string.accessibility_selected
        } else {
            R.string.accessibility_not_selected
        },
    )
