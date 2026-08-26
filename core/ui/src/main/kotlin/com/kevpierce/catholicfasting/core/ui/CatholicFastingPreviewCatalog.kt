package com.kevpierce.catholicfasting.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason

@Composable
fun CatholicFastingDesignSystemPreviewCatalog(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
) {
    CatholicFastingTheme {
        val spacing = CatholicFastingThemeValues.spacing
        val selectedSeason = remember { mutableStateOf("Ordinary") }

        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            CatholicFastingScreenTitle("Android liturgical companion")
            CatholicFastingCompanionCard(
                title = "Ordinary Time",
                tone = seasonTone(LiturgicalSeason.ORDINARY, darkTheme),
            ) {
                Text(
                    "A calm daily surface for guidance, planning, and recovery.",
                    style = CatholicFastingThemeValues.typography.body,
                )
                CatholicFastingStatusChips(
                    labels = listOf("Ordinary", "Lent", "Advent", "Christmas", "Easter"),
                    selectedLabel = selectedSeason.value,
                    onSelected = { selectedSeason.value = it },
                )
            }
            CatholicFastingSemanticCard(
                title = "Rule source",
                tone = CatholicFastingThemeValues.semanticTones.ruleSource,
            ) {
                Text(
                    "Rule authority stays close to fasting decisions without becoming alarmist.",
                    style = CatholicFastingThemeValues.typography.body,
                )
            }
            CatholicFastingTrackerControlPreviewSurface(active = false)
            CatholicFastingTrackerControlPreviewSurface(active = true)
            CatholicFastingReminderControlPreviewSurface(permissionGranted = false)
            CatholicFastingReminderControlPreviewSurface(permissionGranted = true)
            CatholicFastingPremiumSummaryPreviewSurface(unlocked = false)
            CatholicFastingPremiumSummaryPreviewSurface(unlocked = true)
        }
    }
}

@Preview(
    name = "Design system catalog - light",
    widthDp = 411,
    heightDp = 1400,
    showBackground = true,
)
@Composable
private fun CatholicFastingDesignSystemPreviewCatalogLight() {
    CatholicFastingDesignSystemPreviewCatalog()
}

@Preview(
    name = "Design system catalog - large font",
    widthDp = 411,
    heightDp = 1400,
    fontScale = 1.4f,
    showBackground = true,
)
@Composable
private fun CatholicFastingDesignSystemPreviewCatalogLargeFont() {
    CatholicFastingDesignSystemPreviewCatalog()
}

@Preview(
    name = "Design system catalog - compact 2x font",
    widthDp = 320,
    heightDp = 1400,
    fontScale = 2f,
    showBackground = true,
)
@Composable
private fun CatholicFastingDesignSystemPreviewCatalogCompactLargeFont() {
    CatholicFastingDesignSystemPreviewCatalog()
}

@Preview(
    name = "Design system catalog - tablet",
    widthDp = 600,
    heightDp = 1400,
    showBackground = true,
)
@Composable
private fun CatholicFastingDesignSystemPreviewCatalogTablet() {
    CatholicFastingDesignSystemPreviewCatalog()
}
