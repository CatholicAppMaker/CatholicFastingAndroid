package com.kevpierce.catholicfastingapp.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.SeasonTone

internal const val REMINDER_TIER_CHIP_TEST_TAG_PREFIX = "reminder-tier-"
internal const val INTERMITTENT_INTENTION_CHIP_TEST_TAG_PREFIX = "intermittent-intention-"
internal const val REGION_CHIP_TEST_TAG_PREFIX = "region-"

@Composable
internal fun SectionCard(
    title: String,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    CatholicFastingSectionCard(
        title = title,
        tone = tone,
        heroTitle = heroTitle,
        content = content,
    )
}

@Composable
internal fun RowWithScroll(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.xSmall),
    ) {
        content()
    }
}

@Composable
internal fun OutlinedActionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    androidx.compose.material3.OutlinedButton(onClick = onClick, enabled = enabled) {
        Text(label)
    }
}
