@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfasting.feature.premium

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.billing.BillingOfferUi
import com.kevpierce.catholicfasting.core.billing.BillingState
import com.kevpierce.catholicfasting.core.model.FastingPlanningData
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.SeasonTone
import com.kevpierce.catholicfasting.core.ui.catholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.catholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.rememberSeasonTone

data class PremiumWorkspaceUiState(
    val planningData: FastingPlanningData,
    val reflections: List<ReflectionJournalEntry>,
    val premiumSnapshot: PremiumSnapshot,
    val seasonProgramActions: List<String>,
    val fastPrepGuidance: List<String>,
)

data class PremiumWorkspaceActions(
    val onRefresh: () -> Unit,
    val onManageSubscription: () -> Unit,
    val onPurchase: (String) -> Unit,
    val onSaveReflection: (String, String) -> String,
)

@Suppress("LongMethod")
@Composable
fun premiumScreen(
    billingState: BillingState,
    workspaceState: PremiumWorkspaceUiState,
    actions: PremiumWorkspaceActions,
    modifier: Modifier = Modifier,
) {
    var workspaceStatus by remember { mutableStateOf("") }
    val billingStatusMessage = billingState.statusMessage?.localizedText()
    val spacing = CatholicFastingThemeValues.spacing
    val seasonTone = rememberSeasonTone(workspaceState.premiumSnapshot.season)

    LazyColumn(
        modifier = modifier.padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        billingHeaderItems(
            billingState = billingState,
            onRefresh = actions.onRefresh,
            onManageSubscription = actions.onManageSubscription,
        )
        offerItems(
            titleRes = R.string.premium_subscriptions_title,
            offers = billingState.premiumOffers,
            actionLabelRes = R.string.premium_subscribe,
            emptyStateRes = R.string.premium_no_subscription_offers,
            actionsDisabled = billingState.isLoading || billingState.isPurchasing,
            onPurchase = actions.onPurchase,
        )
        offerItems(
            titleRes = R.string.premium_support_tips_title,
            offers = billingState.tipOffers,
            actionLabelRes = R.string.premium_support,
            emptyStateRes = R.string.premium_no_tip_offers,
            actionsDisabled = billingState.isLoading || billingState.isPurchasing,
            onPurchase = actions.onPurchase,
        )
        item {
            workspaceSummaryCard(
                planningData = workspaceState.planningData,
                reflectionCount = workspaceState.reflections.size,
                premiumSnapshot = workspaceState.premiumSnapshot,
                seasonTone = seasonTone,
            )
        }
        item {
            seasonPlanCard(
                premiumSnapshot = workspaceState.premiumSnapshot,
                seasonProgramActions = workspaceState.seasonProgramActions,
                seasonTone = seasonTone,
            )
        }
        item {
            analyticsAndRecoveryCard(
                premiumSnapshot = workspaceState.premiumSnapshot,
                fastPrepGuidance = workspaceState.fastPrepGuidance,
            )
        }
        item {
            reflectionJournalCard(
                reflections = workspaceState.reflections,
                prompt = workspaceState.premiumSnapshot.reflection,
                onSaveReflection = actions.onSaveReflection,
                onStatus = { workspaceStatus = it },
            )
        }
        if (billingStatusMessage != null) {
            item {
                Text(billingStatusMessage)
            }
        }
        if (workspaceStatus.isNotBlank()) {
            item {
                Text(workspaceStatus)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.billingHeaderItems(
    billingState: BillingState,
    onRefresh: () -> Unit,
    onManageSubscription: () -> Unit,
) {
    val subscriptionHealthMessage = billingState.subscriptionHealthMessage

    item {
        catholicFastingScreenTitle(stringResource(R.string.premium_title))
    }
    item {
        Text(
            stringResource(R.string.premium_catalog_subtitle),
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
    item {
        Text(
            if (billingState.premiumUnlocked) {
                stringResource(R.string.premium_active)
            } else {
                stringResource(R.string.premium_inactive)
            },
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
    }
    if (subscriptionHealthMessage != null) {
        item {
            Text(
                subscriptionHealthMessage.localizedText(),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
    }
    item {
        OutlinedButton(onClick = onRefresh) {
            Text(
                if (billingState.isLoading) {
                    stringResource(R.string.premium_refreshing)
                } else {
                    stringResource(R.string.premium_restore_refresh_purchases)
                },
            )
        }
    }
    if (billingState.canManageSubscription) {
        item {
            OutlinedButton(onClick = onManageSubscription) {
                Text(stringResource(R.string.premium_manage_subscription))
            }
        }
    }
    if (billingState.hasPendingPurchases) {
        item {
            Text(stringResource(R.string.premium_purchase_pending))
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.offerItems(
    @StringRes titleRes: Int,
    offers: List<BillingOfferUi>,
    @StringRes actionLabelRes: Int,
    @StringRes emptyStateRes: Int,
    actionsDisabled: Boolean,
    onPurchase: (String) -> Unit,
) {
    item {
        Text(stringResource(titleRes), style = CatholicFastingThemeValues.typography.sectionTitle)
    }
    if (offers.isEmpty()) {
        item {
            Text(stringResource(emptyStateRes), style = CatholicFastingThemeValues.typography.supporting)
        }
        return
    }
    items(offers, key = BillingOfferUi::productId) { offer ->
        offerCard(
            offer = offer,
            actionLabel = stringResource(actionLabelRes),
            actionEnabled = !actionsDisabled,
            onAction = { onPurchase(offer.productId) },
        )
    }
}

@Composable
private fun reflectionJournalCard(
    reflections: List<ReflectionJournalEntry>,
    prompt: com.kevpierce.catholicfasting.core.model.PremiumReflection,
    onSaveReflection: (String, String) -> String,
    onStatus: (String) -> Unit,
) {
    var reflectionTitle by remember { mutableStateOf("") }
    var reflectionBody by remember { mutableStateOf("") }

    workspaceCard(title = stringResource(R.string.premium_reflection_journal_title)) {
        Text(prompt.title, style = CatholicFastingThemeValues.typography.sectionTitle)
        Text(prompt.body, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(R.string.premium_suggested_action_value, prompt.action),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        OutlinedTextField(
            value = reflectionTitle,
            onValueChange = { reflectionTitle = it },
            label = { Text(stringResource(R.string.premium_reflection_title_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = reflectionBody,
            onValueChange = { reflectionBody = it },
            label = { Text(stringResource(R.string.premium_reflection_body_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                onStatus(onSaveReflection(reflectionTitle, reflectionBody))
                reflectionTitle = ""
                reflectionBody = ""
            },
        ) {
            Text(stringResource(R.string.premium_save_reflection))
        }
        reflections.take(3).forEach { reflection ->
            Text(reflection.title, style = CatholicFastingThemeValues.typography.supporting)
            if (reflection.body.isNotBlank()) {
                Text(reflection.body, style = CatholicFastingThemeValues.typography.utility)
            }
        }
    }
}

@Composable
private fun workspaceSummaryCard(
    planningData: FastingPlanningData,
    reflectionCount: Int,
    premiumSnapshot: PremiumSnapshot,
    seasonTone: SeasonTone,
) {
    workspaceCard(
        title = stringResource(R.string.premium_planning_export_title),
        tone = seasonTone,
        heroTitle = true,
    ) {
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
private fun seasonPlanCard(
    premiumSnapshot: PremiumSnapshot,
    seasonProgramActions: List<String>,
    seasonTone: SeasonTone,
) {
    workspaceCard(
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
private fun analyticsAndRecoveryCard(
    premiumSnapshot: PremiumSnapshot,
    fastPrepGuidance: List<String>,
) {
    workspaceCard(title = stringResource(R.string.premium_analytics_recovery_title)) {
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
private fun workspaceCard(
    title: String,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    catholicFastingSectionCard(
        title = title,
        tone = tone,
        heroTitle = heroTitle,
        content = content,
    )
}

@Composable
private fun offerCard(
    offer: BillingOfferUi,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit,
) {
    workspaceCard(title = offer.displayTitle) {
        Text(offer.priceLabel, style = CatholicFastingThemeValues.typography.body)
        Text(offer.billingLabel, style = CatholicFastingThemeValues.typography.supporting)
        Button(onClick = onAction, enabled = actionEnabled) {
            Text(actionLabel)
        }
    }
}
