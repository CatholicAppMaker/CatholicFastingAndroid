package com.kevpierce.catholicfasting.feature.guidance

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.model.GuidanceScenario
import com.kevpierce.catholicfasting.core.model.RuleBundleAudit
import com.kevpierce.catholicfasting.core.model.RuleSettings
import com.kevpierce.catholicfasting.core.rules.FoodGuidanceEngine

@Composable
fun guidanceScreen(
    settings: RuleSettings,
    ruleBundleAudit: RuleBundleAudit,
    modifier: Modifier = Modifier,
) {
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
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.guidance_title), style = MaterialTheme.typography.headlineMedium)
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
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.guidance_food_title), style = MaterialTheme.typography.titleMedium)
            Text(snapshot.summaryLine)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GuidanceScenario.entries.forEach { entry ->
                    FilterChip(
                        selected = scenario == entry,
                        onClick = { onScenarioChange(entry) },
                        label = { Text(entry.label) },
                    )
                }
            }
            Text(
                snapshot.whatCountsAsMeat.summary,
                style = MaterialTheme.typography.bodyMedium,
            )
            snapshot.whatCountsAsMeat.items.forEach { item ->
                Text(stringResource(R.string.guidance_avoid_value, item.detail))
            }
            snapshot.generallyPermitted.items.forEach { item ->
                Text(stringResource(R.string.guidance_permitted_value, item.detail))
            }
            Text(snapshot.mealPattern.title, style = MaterialTheme.typography.titleSmall)
            snapshot.mealPattern.items.forEach { item ->
                Text(stringResource(R.string.guidance_meal_pattern_value, item.detail))
            }
            Text(snapshot.extraGuidance.title, style = MaterialTheme.typography.titleSmall)
            snapshot.extraGuidance.items.forEach { item ->
                Text(stringResource(R.string.guidance_common_question_value, item.detail))
            }
            Text(stringResource(R.string.guidance_stricter_practice), style = MaterialTheme.typography.titleSmall)
            snapshot.stricterTraditionalPractice.forEach { line ->
                Text(stringResource(R.string.guidance_bullet_value, line))
            }
            Text(stringResource(R.string.guidance_if_unsure), style = MaterialTheme.typography.titleSmall)
            snapshot.ifUnsure.forEach { line ->
                Text(stringResource(R.string.guidance_bullet_value, line))
            }
            recommendations.forEach { line ->
                Text(line)
            }
            Text(snapshot.caveatLine, style = MaterialTheme.typography.bodySmall)
            Text(snapshot.sourceLine, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ruleAuditCard(
    settings: RuleSettings,
    ruleBundleAudit: RuleBundleAudit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.guidance_rule_audit_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.guidance_source_value, ruleBundleAudit.source))
            Text(
                if (ruleBundleAudit.isVerified) {
                    stringResource(R.string.guidance_verified)
                } else {
                    stringResource(R.string.guidance_needs_review)
                },
            )
            if (ruleBundleAudit.warnings.isEmpty()) {
                Text(stringResource(R.string.guidance_no_warnings))
            } else {
                ruleBundleAudit.warnings.forEach { warning ->
                    Text(stringResource(R.string.guidance_warning_value, warning))
                }
            }
            Text(
                regionGuidanceText(settings),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun regionGuidanceText(settings: RuleSettings): String =
    when (settings.regionProfile) {
        com.kevpierce.catholicfasting.core.model.RegionProfile.US ->
            "Use U.S. norms as configured, then defer to your pastor and diocesan guidance."
        com.kevpierce.catholicfasting.core.model.RegionProfile.CANADA ->
            "Canadian Friday practice can vary; keep local episcopal guidance in view."
        com.kevpierce.catholicfasting.core.model.RegionProfile.OTHER ->
            "Outside the U.S. and Canada, local episcopal law and pastoral guidance take priority."
    }
