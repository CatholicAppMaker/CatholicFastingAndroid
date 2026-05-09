package com.kevpierce.catholicfasting.core.billing

import com.android.billingclient.api.BillingClient
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.SubscriptionOfferCatalog
import org.junit.Test

class BillingLocalReleaseContractTest {
    @Test
    fun subscriptionCatalogKeepsYearlyAnchorBeforeMonthlyOffer() {
        val offers = SubscriptionOfferCatalog.catholicFasting.offers

        assertThat(offers.map { it.id })
            .containsExactly(
                "cfa_premium_yearly_v3",
                "cfa_premium_monthly_v3",
            ).inOrder()
        assertThat(offers.first().isPrimaryAnchor).isTrue()
        assertThat(offers.drop(1).none { it.isPrimaryAnchor }).isTrue()
    }

    @Test
    fun subscriptionProductQueriesUseCatalogIdsAsPlaySubscriptionProducts() {
        val products = SubscriptionOfferCatalog.catholicFasting.subscriptionProductIds()

        assertThat(products)
            .containsExactly(
                "cfa_premium_yearly_v3",
                "cfa_premium_monthly_v3",
            ).inOrder()
        products.forEach { productId ->
            assertThat(subscriptionProductQuery(productId)).isNotNull()
        }
    }

    @Test
    fun tipProductQueriesUseStableInAppProductIds() {
        assertThat(tipProducts())
            .containsExactly(
                "com.kevpierce.catholicfasting.tip.small",
                "com.kevpierce.catholicfasting.tip.medium",
                "com.kevpierce.catholicfasting.tip.large",
            ).inOrder()

        tipProducts().forEach { productId ->
            assertThat(inAppProductQuery(productId)).isNotNull()
        }
    }

    @Test
    fun billingStateDefaultsRepresentLockedCatalogLoadingState() {
        val state = BillingState()

        assertThat(state.premiumUnlocked).isFalse()
        assertThat(state.canManageSubscription).isFalse()
        assertThat(state.hasPendingPurchases).isFalse()
        assertThat(state.hasCatalogProducts).isFalse()
        assertThat(state.statusMessage).isEqualTo(BillingMessage.LoadingPurchases)
        assertThat(state.catalog).isEqualTo(SubscriptionOfferCatalog.catholicFasting)
    }

    @Test
    fun pendingPurchasesLockPremiumAndWinOverUnlockedRefreshCopy() {
        assertThat(
            purchaseRefreshMessage(
                premiumUnlocked = true,
                hasPendingPurchases = true,
                hasCatalogProducts = true,
            ),
        ).isEqualTo(BillingMessage.PurchasePending)

        assertThat(
            subscriptionHealthMessage(
                premiumUnlocked = true,
                hasPendingPurchases = true,
            ),
        ).isEqualTo(BillingMessage.PendingPurchaseLocked)
    }

    @Test
    fun checkoutAndCatalogFallbackMessagesAreStable() {
        assertThat(productsReadyMessage(hasCatalogProducts = false)).isEqualTo(BillingMessage.ProductsMissing)
        assertThat(
            purchaseRefreshMessage(
                premiumUnlocked = false,
                hasPendingPurchases = false,
                hasCatalogProducts = false,
            ),
        ).isEqualTo(BillingMessage.ProductsMissing)
        assertThat(
            purchaseUpdateMessage(
                hasPurchased = false,
                hasPending = false,
            ),
        ).isEqualTo(BillingMessage.PurchaseUpdated)
    }

    @Test
    fun allPlayBillingResponseCodesMapToReleaseMessages() {
        val expected =
            mapOf(
                BillingClient.BillingResponseCode.OK to BillingMessage.PurchaseCompleted,
                BillingClient.BillingResponseCode.USER_CANCELED to BillingMessage.PurchaseCancelled,
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED to
                    BillingMessage.Failure(BillingFailureKind.SERVICE_DISCONNECTED),
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE to
                    BillingMessage.Failure(BillingFailureKind.PLAY_UNAVAILABLE),
                BillingClient.BillingResponseCode.NETWORK_ERROR to
                    BillingMessage.Failure(BillingFailureKind.PLAY_UNAVAILABLE),
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE to
                    BillingMessage.Failure(BillingFailureKind.BILLING_UNAVAILABLE),
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE to
                    BillingMessage.Failure(BillingFailureKind.ITEM_UNAVAILABLE),
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED to
                    BillingMessage.Failure(BillingFailureKind.ITEM_ALREADY_OWNED),
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED to
                    BillingMessage.Failure(BillingFailureKind.ITEM_NOT_OWNED),
                BillingClient.BillingResponseCode.DEVELOPER_ERROR to
                    BillingMessage.Failure(BillingFailureKind.DEVELOPER_ERROR),
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED to
                    BillingMessage.Failure(BillingFailureKind.FEATURE_NOT_SUPPORTED),
            )

        expected.forEach { (responseCode, message) ->
            assertThat(billingResponseMessage(responseCode, "ignored")).isEqualTo(message)
        }
        assertThat(billingResponseMessage(BillingClient.BillingResponseCode.ERROR, "backend"))
            .isEqualTo(BillingMessage.Failure(BillingFailureKind.GENERIC, "backend"))
    }
}
