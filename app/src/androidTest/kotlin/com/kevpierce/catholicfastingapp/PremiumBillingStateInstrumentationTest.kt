@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfastingapp

import android.content.Context
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.billing.BillingFailureKind
import com.kevpierce.catholicfasting.core.billing.BillingMessage
import com.kevpierce.catholicfasting.core.billing.BillingOfferUi
import com.kevpierce.catholicfasting.core.billing.BillingState
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.rules.PremiumFastPrepGuidanceEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSeasonProgramEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshotEngine
import com.kevpierce.catholicfasting.core.ui.CatholicFastingTheme
import com.kevpierce.catholicfasting.feature.premium.PremiumScreen
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceActions
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import com.kevpierce.catholicfasting.feature.premium.R as PremiumR

@RunWith(AndroidJUnit4::class)
class PremiumBillingStateInstrumentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppContainer.resetForTesting(context)
    }

    @Test
    fun loadingBillingStateShowsRefreshingControl() {
        setPremiumScreen(BillingState(isLoading = true))

        assertText(context.getString(PremiumR.string.premium_refreshing))
    }

    @Test
    fun pendingPurchaseShowsLockedPendingCopy() {
        setPremiumScreen(
            BillingState(
                hasPendingPurchases = true,
                subscriptionHealthMessage = BillingMessage.PendingPurchaseLocked,
            ),
        )

        assertText(context.getString(PremiumR.string.premium_purchase_pending))
        assertTextAfterSwipes(context.getString(PremiumR.string.premium_billing_pending_locked))
    }

    @Test
    fun missingCatalogShowsNoOfferAndProductsMissingCopy() {
        setPremiumScreen(
            BillingState(
                hasCatalogProducts = false,
                statusMessage = BillingMessage.ProductsMissing,
            ),
        )

        assertText(context.getString(PremiumR.string.premium_no_subscription_offers))
        assertTextAfterSwipes(context.getString(PremiumR.string.premium_billing_products_missing))
    }

    @Test
    fun activeEntitlementShowsActiveStatusAndManageSubscription() {
        setPremiumScreen(
            BillingState(
                premiumUnlocked = true,
                canManageSubscription = true,
                hasCatalogProducts = true,
                activeSubscriptionProductId = "cfa_premium_yearly_v3",
                subscriptionHealthMessage = BillingMessage.PremiumSubscriptionActive,
                premiumOffers = premiumOffers(),
            ),
        )

        assertText(context.getString(PremiumR.string.premium_active))
        assertText(context.getString(PremiumR.string.premium_manage_subscription))
        assertTextAfterSwipes(context.getString(PremiumR.string.premium_billing_subscription_active))
    }

    @Test
    fun cancelledPurchaseShowsStableCancellationCopy() {
        setPremiumScreen(
            BillingState(
                hasCatalogProducts = true,
                statusMessage = BillingMessage.PurchaseCancelled,
                premiumOffers = premiumOffers(),
            ),
        )

        assertTextAfterSwipes(context.getString(PremiumR.string.premium_billing_purchase_cancelled))
    }

    @Test
    fun unavailableCheckoutShowsDeveloperConfigurationCopy() {
        setPremiumScreen(
            BillingState(
                hasCatalogProducts = true,
                statusMessage = BillingMessage.Failure(BillingFailureKind.DEVELOPER_ERROR, "bad sku"),
                premiumOffers = premiumOffers(),
            ),
        )

        assertTextAfterSwipes(
            context.getString(
                PremiumR.string.premium_billing_failure_with_debug,
                context.getString(PremiumR.string.premium_billing_failure_developer_error),
                "bad sku",
            ),
        )
    }

    private fun setPremiumScreen(billingState: BillingState) {
        val state = AppContainer.repository.dashboardState.value
        val premiumSnapshot =
            PremiumSnapshotEngine.build(
                observances = state.observances,
                statusesById = state.statusesById,
                sessions = state.intermittentSessions,
                settings = state.settings,
                companionState = state.premiumCompanionState,
                today = LocalDate.now(),
            )

        composeRule.setContent {
            CatholicFastingTheme {
                PremiumScreen(
                    billingState = billingState,
                    workspaceState =
                        PremiumWorkspaceUiState(
                            planningData = state.planningData,
                            reflections = state.reflections,
                            premiumSnapshot = premiumSnapshot,
                            seasonProgramActions =
                                PremiumSeasonProgramEngine.actions(
                                    program = state.premiumCompanionState.seasonProgram,
                                    week = 1,
                                ),
                            fastPrepGuidance =
                                PremiumFastPrepGuidanceEngine.prepAndRefeed(
                                    targetHours = state.intermittentPresetHours,
                                    hasMedicalDispensation = state.settings.hasMedicalDispensation,
                                ),
                        ),
                    actions =
                        PremiumWorkspaceActions(
                            onRefresh = {},
                            onManageSubscription = {},
                            onPurchase = {},
                            onSaveReflection = { _, _ -> "saved" },
                        ),
                )
            }
        }
    }

    private fun assertTextAfterSwipes(text: String) {
        repeat(4) {
            if (composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) {
                return
            }
            composeRule.onRoot().performTouchInput { swipeUp() }
        }
        assertText(text)
    }

    private fun assertText(text: String) {
        assertThat(composeRule.onAllNodesWithText(text).fetchSemanticsNodes().size).isAtLeast(1)
    }

    private fun premiumOffers(): List<BillingOfferUi> =
        listOf(
            BillingOfferUi("cfa_premium_yearly_v3", "Premium Yearly", "$29.99", "Billed yearly"),
            BillingOfferUi("cfa_premium_monthly_v3", "Premium Monthly", "$4.99", "Billed monthly"),
        )
}
