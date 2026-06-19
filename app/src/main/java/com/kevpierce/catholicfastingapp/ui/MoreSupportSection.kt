package com.kevpierce.catholicfastingapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import com.kevpierce.catholicfasting.feature.premium.PremiumScreen
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceActions
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceUiState
import com.kevpierce.catholicfastingapp.R

@Composable
internal fun SupportAndPremiumSection(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    supportState: AppSupportState,
) {
    val resources = LocalResources.current
    PremiumScreen(
        billingState = billingState,
        workspaceState =
            PremiumWorkspaceUiState(
                planningData = state.planningData,
                reflections = state.reflections,
                premiumSnapshot = supportState.premiumSnapshot,
                seasonProgramActions = supportState.seasonProgramActions,
                fastPrepGuidance = supportState.fastPrepGuidance,
            ),
        actions =
            PremiumWorkspaceActions(
                onRefresh = billingActions.onRefresh,
                onManageSubscription = billingActions.onManageSubscription,
                onPurchase = billingActions.onPurchase,
                onSaveReflection = { title, body ->
                    repository
                        .addReflectionEntry(title, body)
                        .fold(
                            onSuccess = { resources.getString(R.string.status_reflection_saved) },
                            onFailure = {
                                it.message ?: resources.getString(R.string.status_reflection_save_failed)
                            },
                        )
                },
            ),
        modifier = Modifier.fillMaxSize(),
    )
}
