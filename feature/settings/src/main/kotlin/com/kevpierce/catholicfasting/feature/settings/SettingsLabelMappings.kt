package com.kevpierce.catholicfasting.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.AscensionObservance
import com.kevpierce.catholicfasting.core.model.CalendarMode
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.RegionProfile

@Composable
internal fun RegionProfile.localizedLabel(): String =
    when (this) {
        RegionProfile.US -> stringResource(R.string.settings_label_region_us)
        RegionProfile.CANADA -> stringResource(R.string.settings_label_region_canada)
        RegionProfile.OTHER -> stringResource(R.string.settings_label_region_other)
    }

@Composable
internal fun CalendarMode.localizedLabel(): String =
    when (this) {
        CalendarMode.USCCB -> stringResource(R.string.settings_label_calendar_usccb)
        CalendarMode.TRADITIONAL_1962 -> stringResource(R.string.settings_label_calendar_traditional)
    }

@Composable
internal fun FridayOutsideLentMode.localizedLabel(): String =
    when (this) {
        FridayOutsideLentMode.ABSTAIN_FROM_MEAT -> stringResource(R.string.settings_label_friday_abstain)
        FridayOutsideLentMode.SUBSTITUTE_PENANCE -> stringResource(R.string.settings_label_friday_substitute)
    }

@Composable
internal fun AscensionObservance.localizedLabel(): String =
    when (this) {
        AscensionObservance.THURSDAY -> stringResource(R.string.settings_label_ascension_thursday)
        AscensionObservance.SUNDAY -> stringResource(R.string.settings_label_ascension_sunday)
    }
