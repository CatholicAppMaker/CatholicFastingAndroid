@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfasting.feature.premium

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.billing.BillingOfferUi
import com.kevpierce.catholicfasting.core.billing.BillingState
import com.kevpierce.catholicfasting.core.model.FastingPlanningData
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot

data class PremiumWorkspaceUiState(
    val planningData: FastingPlanningData,
    val reflections: List<ReflectionJournalEntry>,
    val premiumSnapshot: PremiumSnapshot,
    val seasonProgramActions: List<String>,
    val fastPrepGuidance: List<String>,
)

data class PremiumWorkspaceActions(
    val onRefresh: () -> Unit,
    val onPurchase: (String) -> Unit,
    val onSaveReflection: (String, String) -> String,
    val onExportEncryptedBackup: (String) -> Result<String>,
    val onImportEncryptedBackup: (String, String) -> String,
    val onGenerateHouseholdShareCode: () -> Result<String>,
    val onImportHouseholdShareCode: (String) -> String,
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

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        billingHeaderItems(
            billingState = billingState,
            onRefresh = actions.onRefresh,
        )
        offerItems(
            titleRes = R.string.premium_subscriptions_title,
            offers = billingState.premiumOffers,
            actionLabelRes = R.string.premium_subscribe,
            onPurchase = actions.onPurchase,
        )
        offerItems(
            titleRes = R.string.premium_support_tips_title,
            offers = billingState.tipOffers,
            actionLabelRes = R.string.premium_support,
            onPurchase = actions.onPurchase,
        )
        item {
            workspaceSummaryCard(
                planningData = workspaceState.planningData,
                reflectionCount = workspaceState.reflections.size,
                premiumSnapshot = workspaceState.premiumSnapshot,
            )
        }
        item {
            seasonPlanCard(
                premiumSnapshot = workspaceState.premiumSnapshot,
                seasonProgramActions = workspaceState.seasonProgramActions,
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
        item {
            encryptedBackupCard(
                onExportEncryptedBackup = actions.onExportEncryptedBackup,
                onImportEncryptedBackup = actions.onImportEncryptedBackup,
                onStatus = { workspaceStatus = it },
            )
        }
        item {
            householdShareCard(
                onGenerateHouseholdShareCode = actions.onGenerateHouseholdShareCode,
                onImportHouseholdShareCode = actions.onImportHouseholdShareCode,
                onStatus = { workspaceStatus = it },
            )
        }
        if (billingState.statusMessage.isNotBlank()) {
            item {
                Text(billingState.statusMessage)
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
) {
    item {
        Text(
            stringResource(R.string.premium_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
    }
    item {
        Text(billingState.catalog.subtitle)
    }
    item {
        Text(
            if (billingState.premiumUnlocked) {
                stringResource(R.string.premium_active)
            } else {
                stringResource(R.string.premium_inactive)
            },
            style = MaterialTheme.typography.titleMedium,
        )
    }
    if (billingState.subscriptionHealthMessage.isNotBlank()) {
        item { Text(billingState.subscriptionHealthMessage) }
    }
    item {
        OutlinedButton(onClick = onRefresh) {
            Text(
                if (billingState.isLoading) {
                    stringResource(R.string.premium_refreshing)
                } else {
                    stringResource(R.string.premium_refresh_purchases)
                },
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.offerItems(
    @StringRes titleRes: Int,
    offers: List<BillingOfferUi>,
    @StringRes actionLabelRes: Int,
    onPurchase: (String) -> Unit,
) {
    item {
        Text(stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
    }
    items(offers, key = BillingOfferUi::productId) { offer ->
        offerCard(
            offer = offer,
            actionLabel = stringResource(actionLabelRes),
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
        Text(prompt.title, style = MaterialTheme.typography.titleSmall)
        Text(prompt.body)
        Text(stringResource(R.string.premium_suggested_action_value, prompt.action))
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
            Text(reflection.title, style = MaterialTheme.typography.titleSmall)
            if (reflection.body.isNotBlank()) {
                Text(reflection.body)
            }
        }
    }
}

@Composable
private fun encryptedBackupCard(
    onExportEncryptedBackup: (String) -> Result<String>,
    onImportEncryptedBackup: (String, String) -> String,
    onStatus: (String) -> Unit,
) {
    var backupPassphrase by remember { mutableStateOf("") }
    var backupCode by remember { mutableStateOf("") }
    val backupGenerated = stringResource(R.string.premium_backup_generated)
    val backupExportFailed = stringResource(R.string.premium_backup_export_failed)

    workspaceCard(title = stringResource(R.string.premium_encrypted_backup_title)) {
        OutlinedTextField(
            value = backupPassphrase,
            onValueChange = { backupPassphrase = it },
            label = { Text(stringResource(R.string.premium_backup_passphrase_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = {
                onExportEncryptedBackup(backupPassphrase)
                    .onSuccess {
                        backupCode = it
                        onStatus(backupGenerated)
                    }.onFailure {
                        onStatus(it.message ?: backupExportFailed)
                    }
            },
        ) {
            Text(stringResource(R.string.premium_generate_encrypted_backup))
        }
        OutlinedTextField(
            value = backupCode,
            onValueChange = { backupCode = it },
            label = { Text(stringResource(R.string.premium_encrypted_backup_code_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                onStatus(onImportEncryptedBackup(backupCode, backupPassphrase))
            },
        ) {
            Text(stringResource(R.string.premium_import_encrypted_backup))
        }
    }
}

@Composable
private fun householdShareCard(
    onGenerateHouseholdShareCode: () -> Result<String>,
    onImportHouseholdShareCode: (String) -> String,
    onStatus: (String) -> Unit,
) {
    var householdCode by remember { mutableStateOf("") }
    val householdCodeGenerated = stringResource(R.string.premium_household_code_generated)
    val householdCodeFailed = stringResource(R.string.premium_household_code_failed)

    workspaceCard(title = stringResource(R.string.premium_household_share_title)) {
        OutlinedButton(
            onClick = {
                onGenerateHouseholdShareCode()
                    .onSuccess {
                        householdCode = it
                        onStatus(householdCodeGenerated)
                    }.onFailure {
                        onStatus(it.message ?: householdCodeFailed)
                    }
            },
        ) {
            Text(stringResource(R.string.premium_generate_household_code))
        }
        OutlinedTextField(
            value = householdCode,
            onValueChange = { householdCode = it },
            label = { Text(stringResource(R.string.premium_household_share_code_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                onStatus(onImportHouseholdShareCode(householdCode))
            },
        ) {
            Text(stringResource(R.string.premium_import_household_code))
        }
    }
}

@Composable
private fun workspaceSummaryCard(
    planningData: FastingPlanningData,
    reflectionCount: Int,
    premiumSnapshot: PremiumSnapshot,
) {
    workspaceCard(title = stringResource(R.string.premium_planning_export_title)) {
        Text(stringResource(R.string.premium_season_value, premiumSnapshot.season.label))
        Text(stringResource(R.string.premium_required_goal_value, planningData.requiredGoal))
        Text(stringResource(R.string.premium_optional_goal_value, planningData.optionalGoal))
        Text(stringResource(R.string.premium_weekly_intentions_value, planningData.weeklyIntentions.size))
        Text(
            stringResource(
                R.string.premium_season_commitments_value,
                planningData.seasonCommitments.count { it.isEnabled },
            ),
        )
        Text(stringResource(R.string.premium_saved_reflections_value, reflectionCount))
        Text(premiumSnapshot.motivationLine)
    }
}

@Composable
private fun seasonPlanCard(
    premiumSnapshot: PremiumSnapshot,
    seasonProgramActions: List<String>,
) {
    workspaceCard(title = premiumSnapshot.seasonPlan.titleLine) {
        Text(premiumSnapshot.seasonPlan.focusLine)
        Text(stringResource(R.string.premium_fasting_intensity_value, premiumSnapshot.seasonPlan.fastingIntensity))
        premiumSnapshot.seasonPlan.practices.forEach { practice ->
            Text(stringResource(R.string.premium_bullet_value, practice))
        }
        Text(stringResource(R.string.premium_adaptive_rule), style = MaterialTheme.typography.titleSmall)
        Text(premiumSnapshot.adaptiveRulePlan.summary)
        premiumSnapshot.adaptiveRulePlan.weeklyActions.forEach { action ->
            Text(stringResource(R.string.premium_bullet_value, action))
        }
        Text(premiumSnapshot.adaptiveRulePlan.caution)
        Text(stringResource(R.string.premium_season_program), style = MaterialTheme.typography.titleSmall)
        seasonProgramActions.forEach { action ->
            Text(stringResource(R.string.premium_bullet_value, action))
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
        )
        Text(
            stringResource(
                R.string.premium_overall_completion_value,
                premiumSnapshot.analyticsSummary.overallCompletionPercent,
            ),
        )
        Text(stringResource(R.string.premium_missed_observances_value, premiumSnapshot.analyticsSummary.missedCount))
        Text(
            stringResource(
                R.string.premium_substituted_observances_value,
                premiumSnapshot.analyticsSummary.substitutedCount,
            ),
        )
        Text(
            stringResource(
                R.string.premium_intermittent_hit_rate_value,
                premiumSnapshot.analyticsSummary.intermittentTargetHitPercent,
            ),
        )
        Text(premiumSnapshot.reminderRecommendation.summaryLine)
        Text(premiumSnapshot.recoveryCoachPlan.summary)
        premiumSnapshot.recoveryCoachPlan.steps.take(3).forEach { step ->
            Text(stringResource(R.string.premium_bullet_value, step))
        }
        Text(stringResource(R.string.premium_fast_prep), style = MaterialTheme.typography.titleSmall)
        fastPrepGuidance.forEach { line ->
            Text(stringResource(R.string.premium_bullet_value, line))
        }
    }
}

@Composable
private fun workspaceCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            content()
        }
    }
}

@Composable
private fun offerCard(
    offer: BillingOfferUi,
    actionLabel: String,
    onAction: () -> Unit,
) {
    workspaceCard(title = offer.displayTitle) {
        Text(offer.priceLabel)
        Text(offer.billingLabel)
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}
