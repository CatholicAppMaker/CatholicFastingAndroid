package com.kevpierce.catholicfasting.feature.guidance

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.GuidanceScenario
import com.kevpierce.catholicfasting.core.model.RuleBundleAudit
import com.kevpierce.catholicfasting.core.model.RuleSettings
import com.kevpierce.catholicfasting.core.rules.FoodGuidanceEngine
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.catholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.catholicFastingSectionCard

@Composable
fun guidanceScreen(
    settings: RuleSettings,
    ruleBundleAudit: RuleBundleAudit,
    modifier: Modifier = Modifier,
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
                .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        catholicFastingScreenTitle(stringResource(R.string.guidance_title))
        foodGuidanceCard(
            scenario = scenario,
            onScenarioChange = { scenario = it },
            snapshot = snapshot,
            recommendations = recommendations,
        )
        ruleAuditCard(
            settings = settings,
            ruleBundleAudit = ruleBundleAudit,
        )
    }
}

@Composable
private fun foodGuidanceCard(
    scenario: GuidanceScenario,
    onScenarioChange: (GuidanceScenario) -> Unit,
    snapshot: com.kevpierce.catholicfasting.core.model.FoodGuidanceSnapshot,
    recommendations: List<String>,
) {
    val spacing = CatholicFastingThemeValues.spacing

    catholicFastingSectionCard(title = stringResource(R.string.guidance_food_title)) {
        Text(snapshot.summaryLine, style = CatholicFastingThemeValues.typography.body)
        scenarioChipRow(
            spacing = spacing.xSmall,
            scenario = scenario,
            onScenarioChange = onScenarioChange,
        )
        Text(
            snapshot.whatCountsAsMeat.summary,
            style = CatholicFastingThemeValues.typography.supporting,
        )
        guidanceDetailItems(
            items = snapshot.whatCountsAsMeat.items.map { it.detail },
            labelFormatter = { detail -> stringResource(R.string.guidance_avoid_value, detail) },
            style = CatholicFastingThemeValues.typography.body,
        )
        guidanceDetailItems(
            items = snapshot.generallyPermitted.items.map { it.detail },
            labelFormatter = { detail -> stringResource(R.string.guidance_permitted_value, detail) },
            style = CatholicFastingThemeValues.typography.body,
        )
        guidanceSubsection(
            title = snapshot.mealPattern.title,
            items = snapshot.mealPattern.items.map { it.detail },
            labelFormatter = { detail -> stringResource(R.string.guidance_meal_pattern_value, detail) },
        )
        guidanceSubsection(
            title = snapshot.extraGuidance.title,
            items = snapshot.extraGuidance.items.map { it.detail },
            labelFormatter = { detail -> stringResource(R.string.guidance_common_question_value, detail) },
        )
        bulletSubsection(
            title = stringResource(R.string.guidance_stricter_practice),
            items = snapshot.stricterTraditionalPractice,
        )
        bulletSubsection(
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
private fun scenarioChipRow(
    spacing: androidx.compose.ui.unit.Dp,
    scenario: GuidanceScenario,
    onScenarioChange: (GuidanceScenario) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        GuidanceScenario.entries.forEach { entry ->
            FilterChip(
                selected = scenario == entry,
                onClick = { onScenarioChange(entry) },
                label = { Text(entry.localizedLabel()) },
            )
        }
    }
}

@Composable
private fun guidanceSubsection(
    title: String,
    items: List<String>,
    labelFormatter: @Composable (String) -> String,
) {
    Text(title, style = CatholicFastingThemeValues.typography.sectionTitle)
    guidanceDetailItems(
        items = items,
        labelFormatter = labelFormatter,
        style = CatholicFastingThemeValues.typography.supporting,
    )
}

@Composable
private fun guidanceDetailItems(
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
private fun bulletSubsection(
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
private fun ruleAuditCard(
    settings: RuleSettings,
    ruleBundleAudit: RuleBundleAudit,
) {
    catholicFastingSectionCard(title = stringResource(R.string.guidance_rule_audit_title)) {
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
