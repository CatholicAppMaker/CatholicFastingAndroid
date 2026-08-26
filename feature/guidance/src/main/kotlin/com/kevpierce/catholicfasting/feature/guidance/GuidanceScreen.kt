package com.kevpierce.catholicfasting.feature.guidance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.kevpierce.catholicfasting.core.model.GuidanceScenario
import com.kevpierce.catholicfasting.core.model.RuleBundleAudit
import com.kevpierce.catholicfasting.core.model.RuleSettings
import com.kevpierce.catholicfasting.core.model.SacredImageryItem
import com.kevpierce.catholicfasting.core.rules.FoodGuidanceEngine
import com.kevpierce.catholicfasting.core.ui.CatholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.SacredImageryRail

@Composable
fun GuidanceScreen(
    settings: RuleSettings,
    ruleBundleAudit: RuleBundleAudit,
    modifier: Modifier = Modifier,
    devotionalGallery: List<SacredImageryItem> = emptyList(),
) {
    val spacing = CatholicFastingThemeValues.spacing
    var scenario by remember { mutableStateOf(GuidanceScenario.NORMAL_DAY) }
    val snapshot = FoodGuidanceEngine.snapshot(scenario = scenario, settings = settings)
    val recommendations =
        FoodGuidanceEngine.recommendations(
            scenario = scenario,
            settings = settings,
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        CatholicFastingScreenTitle(stringResource(R.string.guidance_title))
        FoodGuidanceCard(
            scenario = scenario,
            onScenarioChange = { scenario = it },
            snapshot = snapshot,
            recommendations = recommendations,
        )
        RuleAuditCard(
            settings = settings,
            ruleBundleAudit = ruleBundleAudit,
        )
        SacredGallerySection(devotionalGallery)
    }
}

@Composable
private fun FoodGuidanceCard(
    scenario: GuidanceScenario,
    onScenarioChange: (GuidanceScenario) -> Unit,
    snapshot: com.kevpierce.catholicfasting.core.model.FoodGuidanceSnapshot,
    recommendations: List<String>,
) {
    val spacing = CatholicFastingThemeValues.spacing

    CatholicFastingSectionCard(title = stringResource(R.string.guidance_food_title)) {
        Text(snapshot.summaryLine, style = CatholicFastingThemeValues.typography.body)
        ScenarioChipRow(
            spacing = spacing.xSmall,
            scenario = scenario,
            onScenarioChange = onScenarioChange,
        )
        Text(
            snapshot.whatCountsAsMeat.summary,
            style = CatholicFastingThemeValues.typography.supporting,
        )
        GuidanceDetailItems(
            items = snapshot.whatCountsAsMeat.items.map { it.detail },
            labelFormatter = { detail -> stringResource(R.string.guidance_avoid_value, detail) },
            style = CatholicFastingThemeValues.typography.body,
        )
        GuidanceDetailItems(
            items = snapshot.generallyPermitted.items.map { it.detail },
            labelFormatter = { detail -> stringResource(R.string.guidance_permitted_value, detail) },
            style = CatholicFastingThemeValues.typography.body,
        )
        GuidanceSubsection(
            title = snapshot.mealPattern.title,
            items = snapshot.mealPattern.items.map { it.detail },
            labelFormatter = { detail -> stringResource(R.string.guidance_meal_pattern_value, detail) },
        )
        GuidanceSubsection(
            title = snapshot.extraGuidance.title,
            items = snapshot.extraGuidance.items.map { it.detail },
            labelFormatter = { detail -> stringResource(R.string.guidance_common_question_value, detail) },
        )
        BulletSubsection(
            title = stringResource(R.string.guidance_stricter_practice),
            items = snapshot.stricterTraditionalPractice,
        )
        BulletSubsection(
            title = stringResource(R.string.guidance_if_unsure),
            items = snapshot.ifUnsure,
        )
        recommendations.forEach { line ->
            Text(line, style = CatholicFastingThemeValues.typography.supporting)
        }
        Text(snapshot.caveatLine, style = CatholicFastingThemeValues.typography.utility)
        Text(snapshot.sourceLine, style = CatholicFastingThemeValues.typography.utility)
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ScenarioChipRow(
    spacing: androidx.compose.ui.unit.Dp,
    scenario: GuidanceScenario,
    onScenarioChange: (GuidanceScenario) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        GuidanceScenario.entries.forEach { entry ->
            val label = entry.localizedLabel()
            val selectedState = guidanceSelectedStateDescription(scenario == entry)
            FilterChip(
                selected = scenario == entry,
                onClick = { onScenarioChange(entry) },
                modifier =
                    Modifier.semantics {
                        stateDescription = selectedState
                    },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun GuidanceSubsection(
    title: String,
    items: List<String>,
    labelFormatter: @Composable (String) -> String,
) {
    Text(title, style = CatholicFastingThemeValues.typography.sectionTitle)
    GuidanceDetailItems(
        items = items,
        labelFormatter = labelFormatter,
        style = CatholicFastingThemeValues.typography.supporting,
    )
}

@Composable
private fun GuidanceDetailItems(
    items: List<String>,
    labelFormatter: @Composable (String) -> String,
    style: androidx.compose.ui.text.TextStyle,
) {
    items.forEach { detail ->
        Text(
            labelFormatter(detail),
            style = style,
        )
    }
}

@Composable
private fun guidanceSelectedStateDescription(selected: Boolean): String =
    stringResource(
        if (selected) {
            R.string.guidance_accessibility_selected
        } else {
            R.string.guidance_accessibility_not_selected
        },
    )

@Composable
private fun BulletSubsection(
    title: String,
    items: List<String>,
) {
    Text(title, style = CatholicFastingThemeValues.typography.sectionTitle)
    items.forEach { line ->
        Text(
            stringResource(R.string.guidance_bullet_value, line),
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@Composable
private fun RuleAuditCard(
    settings: RuleSettings,
    ruleBundleAudit: RuleBundleAudit,
) {
    CatholicFastingSectionCard(title = stringResource(R.string.guidance_rule_audit_title)) {
        Text(
            stringResource(R.string.guidance_source_value, ruleBundleAudit.source),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            if (ruleBundleAudit.isVerified) {
                stringResource(R.string.guidance_verified)
            } else {
                stringResource(R.string.guidance_needs_review)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        if (ruleBundleAudit.warnings.isEmpty()) {
            Text(
                stringResource(R.string.guidance_no_warnings),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        } else {
            ruleBundleAudit.warnings.forEach { warning ->
                Text(
                    stringResource(R.string.guidance_warning_value, warning),
                    style = CatholicFastingThemeValues.typography.supporting,
                )
            }
        }
        Text(
            settings.regionProfile.localizedRegionGuidance(),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}

@Composable
private fun SacredGallerySection(devotionalGallery: List<SacredImageryItem>) {
    SacredImageryRail(
        title = stringResource(R.string.guidance_symbol_gallery_title),
        intro = stringResource(R.string.guidance_symbol_gallery_intro),
        imagery = devotionalGallery,
    )
}
