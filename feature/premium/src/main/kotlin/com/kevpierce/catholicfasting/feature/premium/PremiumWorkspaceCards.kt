package com.kevpierce.catholicfasting.feature.premium

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.FastingPlanningData
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingEyebrow
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.SeasonTone

internal fun LazyListScope.premiumWorkspaceItems(
    workspaceState: PremiumWorkspaceUiState,
    seasonTone: SeasonTone,
    onSaveReflection: (String, String) -> String,
    onWorkspaceStatus: (String) -> Unit,
) {
    item {
        WorkspaceSummaryCard(
            planningData = workspaceState.planningData,
            reflectionCount = workspaceState.reflections.size,
            premiumSnapshot = workspaceState.premiumSnapshot,
            seasonTone = seasonTone,
        )
    }
    item {
        SeasonPlanCard(
            premiumSnapshot = workspaceState.premiumSnapshot,
            seasonProgramActions = workspaceState.seasonProgramActions,
            seasonTone = seasonTone,
        )
    }
    item {
        AnalyticsAndRecoveryCard(
            premiumSnapshot = workspaceState.premiumSnapshot,
            fastPrepGuidance = workspaceState.fastPrepGuidance,
        )
    }
    item {
        ReflectionJournalCard(
            reflections = workspaceState.reflections,
            prompt = workspaceState.premiumSnapshot.reflection,
            onSaveReflection = onSaveReflection,
            onStatus = onWorkspaceStatus,
        )
    }
}

@Composable
internal fun WorkspaceSummaryCard(
    planningData: FastingPlanningData,
    reflectionCount: Int,
    premiumSnapshot: PremiumSnapshot,
    seasonTone: SeasonTone,
) {
    WorkspaceCard(
        title = stringResource(R.string.premium_guided_journey_title),
        tone = seasonTone,
        heroTitle = true,
    ) {
        CatholicFastingEyebrow(
            text = stringResource(R.string.premium_planning_export_title),
            color = seasonTone.accentColor,
        )
        Text(
            stringResource(R.string.premium_season_value, premiumSnapshot.season.localizedLabel()),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.premium_required_goal_value, planningData.requiredGoal),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(R.string.premium_optional_goal_value, planningData.optionalGoal),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(R.string.premium_weekly_intentions_value, planningData.weeklyIntentions.size),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.premium_season_commitments_value,
                planningData.seasonCommitments.count { it.isEnabled },
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(R.string.premium_saved_reflections_value, reflectionCount),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(premiumSnapshot.motivationLine, style = CatholicFastingThemeValues.typography.body)
    }
}

@Composable
internal fun SeasonPlanCard(
    premiumSnapshot: PremiumSnapshot,
    seasonProgramActions: List<String>,
    seasonTone: SeasonTone,
) {
    WorkspaceCard(
        title = premiumSnapshot.seasonPlan.titleLine,
        tone = seasonTone,
    ) {
        Text(premiumSnapshot.seasonPlan.focusLine, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(R.string.premium_fasting_intensity_value, premiumSnapshot.seasonPlan.fastingIntensity),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        premiumSnapshot.seasonPlan.practices.forEach { practice ->
            Text(
                stringResource(R.string.premium_bullet_value, practice),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
        Text(
            stringResource(R.string.premium_adaptive_rule),
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        Text(premiumSnapshot.adaptiveRulePlan.summary, style = CatholicFastingThemeValues.typography.body)
        premiumSnapshot.adaptiveRulePlan.weeklyActions.forEach { action ->
            Text(
                stringResource(R.string.premium_bullet_value, action),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
        Text(premiumSnapshot.adaptiveRulePlan.caution, style = CatholicFastingThemeValues.typography.utility)
        Text(
            stringResource(R.string.premium_season_program),
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        seasonProgramActions.forEach { action ->
            Text(
                stringResource(R.string.premium_bullet_value, action),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
    }
}

@Composable
internal fun AnalyticsAndRecoveryCard(
    premiumSnapshot: PremiumSnapshot,
    fastPrepGuidance: List<String>,
) {
    WorkspaceCard(title = stringResource(R.string.premium_analytics_recovery_title)) {
        Text(
            stringResource(
                R.string.premium_required_completion_value,
                premiumSnapshot.analyticsSummary.requiredCompletionPercent,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.premium_overall_completion_value,
                premiumSnapshot.analyticsSummary.overallCompletionPercent,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(R.string.premium_missed_observances_value, premiumSnapshot.analyticsSummary.missedCount),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.premium_substituted_observances_value,
                premiumSnapshot.analyticsSummary.substitutedCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.premium_intermittent_hit_rate_value,
                premiumSnapshot.analyticsSummary.intermittentTargetHitPercent,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(premiumSnapshot.reminderRecommendation.summaryLine, style = CatholicFastingThemeValues.typography.body)
        Text(premiumSnapshot.recoveryCoachPlan.summary, style = CatholicFastingThemeValues.typography.body)
        premiumSnapshot.recoveryCoachPlan.steps.take(3).forEach { step ->
            Text(
                stringResource(R.string.premium_bullet_value, step),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
        Text(stringResource(R.string.premium_fast_prep), style = CatholicFastingThemeValues.typography.sectionTitle)
        fastPrepGuidance.forEach { line ->
            Text(
                stringResource(R.string.premium_bullet_value, line),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
    }
}

@Composable
internal fun WorkspaceCard(
    title: String,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    CatholicFastingSectionCard(
        title = title,
        tone = tone,
        heroTitle = heroTitle,
        content = content,
    )
}
