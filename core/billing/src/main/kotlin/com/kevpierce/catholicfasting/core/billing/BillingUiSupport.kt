package com.kevpierce.catholicfasting.core.billing

import com.android.billingclient.api.BillingClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal fun billingResponseMessage(
    responseCode: Int,
    debugMessage: String,
): BillingMessage =
    when (responseCode) {
        BillingClient.BillingResponseCode.OK -> BillingMessage.PurchaseCompleted
        BillingClient.BillingResponseCode.USER_CANCELED -> BillingMessage.PurchaseCancelled
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->
            BillingMessage.Failure(BillingFailureKind.SERVICE_DISCONNECTED)
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingClient.BillingResponseCode.NETWORK_ERROR,
        ->
            BillingMessage.Failure(BillingFailureKind.PLAY_UNAVAILABLE)
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
            BillingMessage.Failure(BillingFailureKind.BILLING_UNAVAILABLE)
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
            BillingMessage.Failure(BillingFailureKind.ITEM_UNAVAILABLE)
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
            BillingMessage.Failure(BillingFailureKind.ITEM_ALREADY_OWNED)
        BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->
            BillingMessage.Failure(BillingFailureKind.ITEM_NOT_OWNED)
        BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
            BillingMessage.Failure(BillingFailureKind.DEVELOPER_ERROR)
        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED ->
            BillingMessage.Failure(BillingFailureKind.FEATURE_NOT_SUPPORTED)
        else -> BillingMessage.Failure(BillingFailureKind.GENERIC, debugMessage = debugMessage)
    }

internal fun purchaseUpdateMessage(
    hasPurchased: Boolean,
    hasPending: Boolean,
): BillingMessage =
    when {
        hasPending -> BillingMessage.PurchasePending
        hasPurchased -> BillingMessage.PurchaseCompleted
        else -> BillingMessage.PurchaseUpdated
    }

internal fun productsReadyMessage(hasCatalogProducts: Boolean): BillingMessage =
    if (hasCatalogProducts) {
        BillingMessage.PurchasesReady
    } else {
        BillingMessage.ProductsMissing
    }

internal fun purchaseRefreshMessage(
    premiumUnlocked: Boolean,
    hasPendingPurchases: Boolean,
    hasCatalogProducts: Boolean,
): BillingMessage =
    when {
        hasPendingPurchases -> BillingMessage.PurchasePending
        premiumUnlocked -> BillingMessage.PremiumPurchasesRefreshed
        hasCatalogProducts -> BillingMessage.NoActivePremiumPurchase
        else -> productsReadyMessage(hasCatalogProducts = false)
    }

internal fun subscriptionHealthMessage(
    premiumUnlocked: Boolean,
    hasPendingPurchases: Boolean,
): BillingMessage =
    when {
        hasPendingPurchases -> BillingMessage.PendingPurchaseLocked
        premiumUnlocked -> BillingMessage.PremiumSubscriptionActive
        else -> BillingMessage.NoActivePremiumPurchase
    }

internal fun manageSubscriptionsUrl(
    packageName: String,
    productId: String,
): String {
    val charset = StandardCharsets.UTF_8.toString()
    val encodedPackage = URLEncoder.encode(packageName, charset)
    val encodedProduct = URLEncoder.encode(productId, charset)
    return "https://play.google.com/store/account/subscriptions?package=$encodedPackage&sku=$encodedProduct"
}
