package com.kevpierce.catholicfasting.feature.guidance

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.GuidanceScenario
import com.kevpierce.catholicfasting.core.model.RegionProfile

@Composable
internal fun GuidanceScenario.localizedLabel(): String =
    when (this) {
        GuidanceScenario.NORMAL_DAY -> stringResource(R.string.guidance_label_normal_day)
        GuidanceScenario.HEAVY_LABOR -> stringResource(R.string.guidance_label_heavy_labor)
        GuidanceScenario.TRAVEL -> stringResource(R.string.guidance_label_travel)
        GuidanceScenario.SOCIAL_MEAL -> stringResource(R.string.guidance_label_social_meal)
        GuidanceScenario.MEDICAL_RECOVERY -> stringResource(R.string.guidance_label_medical_recovery)
    }

@Composable
internal fun RegionProfile.localizedRegionGuidance(): String =
    when (this) {
        RegionProfile.US -> stringResource(R.string.guidance_region_us)
        RegionProfile.CANADA -> stringResource(R.string.guidance_region_canada)
        RegionProfile.OTHER -> stringResource(R.string.guidance_region_other)
    }
