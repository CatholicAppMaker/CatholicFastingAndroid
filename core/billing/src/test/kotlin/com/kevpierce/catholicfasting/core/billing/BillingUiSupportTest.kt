package com.kevpierce.catholicfasting.core.billing

import com.android.billingclient.api.BillingClient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BillingUiSupportTest {
    @Test
    fun billingResponseMessageUsesSpecificCanceledCopy() {
        val message =
            billingResponseMessage(
                responseCode = BillingClient.BillingResponseCode.USER_CANCELED,
                debugMessage = "",
            )

        assertThat(message).isEqualTo("Purchase cancelled.")
    }

    @Test
    fun billingResponseMessageFallsBackToDebugMessage() {
        val message =
            billingResponseMessage(
                responseCode = BillingClient.BillingResponseCode.ERROR,
                debugMessage = "Play backend unavailable",
            )

        assertThat(message).isEqualTo("Google Play purchase failed. Play backend unavailable")
    }

    @Test
    fun purchaseRefreshMessagePrioritizesPendingPurchases() {
        val message =
            purchaseRefreshMessage(
                premiumUnlocked = true,
                hasPendingPurchases = true,
                hasCatalogProducts = true,
            )

        assertThat(message).contains("pending")
        assertThat(message).contains("unlock")
    }

    @Test
    fun productsReadyMessageExplainsMissingCatalog() {
        assertThat(productsReadyMessage(hasCatalogProducts = false))
            .contains("no products were returned")
    }

    @Test
    fun subscriptionHealthMessageReflectsUnlockedState() {
        val message =
            subscriptionHealthMessage(
                premiumUnlocked = true,
                hasPendingPurchases = false,
            )

        assertThat(message).isEqualTo("Premium subscription is active.")
    }

    @Test
    fun manageSubscriptionsUrlEncodesQueryParameters() {
        val url =
            manageSubscriptionsUrl(
                packageName = "com.kevpierce.catholic fasting",
                productId = "premium.monthly:usd",
            )

        assertThat(url)
            .isEqualTo(
                "https://play.google.com/store/account/subscriptions" +
                    "?package=com.kevpierce.catholic+fasting&sku=premium.monthly%3Ausd",
            )
    }
}
