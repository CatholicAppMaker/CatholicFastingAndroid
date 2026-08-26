package com.kevpierce.catholicfasting.feature.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.kevpierce.catholicfasting.core.billing.BillingState
import com.kevpierce.catholicfasting.core.model.FastingPlanningData
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
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

const val PREMIUM_LIST_TEST_TAG = "premium-list"

@Composable
fun PremiumScreen(
    billingState: BillingState,
    workspaceState: PremiumWorkspaceUiState,
    actions: PremiumWorkspaceActions,
    modifier: Modifier = Modifier,
) {
    var workspaceStatus by remember { mutableStateOf("") }
    val billingStatusMessage = billingState.statusMessage?.localizedText()
    val seasonTone = rememberSeasonTone(workspaceState.premiumSnapshot.season)

    LazyColumn(
        modifier =
            modifier
                .padding(CatholicFastingThemeValues.spacing.medium)
                .testTag(PREMIUM_LIST_TEST_TAG),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.small),
    ) {
        billingHeaderItems(
            billingState = billingState,
            onRefresh = actions.onRefresh,
            onManageSubscription = actions.onManageSubscription,
        )
        premiumOfferItems(
            billingState = billingState,
            onPurchase = actions.onPurchase,
        )
        premiumWorkspaceItems(
            workspaceState = workspaceState,
            seasonTone = seasonTone,
            onSaveReflection = actions.onSaveReflection,
            onWorkspaceStatus = { workspaceStatus = it },
        )
        premiumStatusItems(
            billingStatusMessage = billingStatusMessage,
            workspaceStatus = workspaceStatus,
        )
    }
}

private fun LazyListScope.premiumStatusItems(
    billingStatusMessage: String?,
    workspaceStatus: String,
) {
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
