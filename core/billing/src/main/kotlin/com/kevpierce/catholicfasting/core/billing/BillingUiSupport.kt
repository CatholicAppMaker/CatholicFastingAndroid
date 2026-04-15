package com.kevpierce.catholicfasting.core.billing

import com.android.billingclient.api.BillingClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal fun billingResponseMessage(
    responseCode: Int,
    debugMessage: String,
): String =
    when (responseCode) {
        BillingClient.BillingResponseCode.OK -> "Google Play purchase completed."
        BillingClient.BillingResponseCode.USER_CANCELED -> "Purchase cancelled."
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->
            "Google Play disconnected. Refresh purchases and try again."
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingClient.BillingResponseCode.NETWORK_ERROR,
        ->
            "Google Play is temporarily unavailable. Check your connection and try again."
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
            "Google Play Billing is unavailable on this device or account."
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
            "This purchase option is not available for this Play account yet."
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
            "You already own this purchase. Refresh purchases or manage it in Google Play."
        BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->
            "Google Play could not find an existing purchase to restore."
        BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
            "Billing configuration error. Verify the Play Console products and test account."
        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED ->
            "This device does not support the requested billing feature."
        else ->
            if (debugMessage.isBlank()) {
                "Google Play purchase failed."
            } else {
                "Google Play purchase failed. $debugMessage"
            }
    }

internal fun purchaseUpdateMessage(
    hasPurchased: Boolean,
    hasPending: Boolean,
): String =
    when {
        hasPending -> "Purchase pending. Google Play will unlock Premium after payment clears."
        hasPurchased -> "Purchase completed."
        else -> "Google Play purchase updated."
    }

internal fun productsReadyMessage(hasCatalogProducts: Boolean): String =
    if (hasCatalogProducts) {
        "Google Play purchases ready."
    } else {
        "Google Play connected, but no products were returned. Verify Play Console setup and test tracks."
    }

internal fun purchaseRefreshMessage(
    premiumUnlocked: Boolean,
    hasPendingPurchases: Boolean,
    hasCatalogProducts: Boolean,
): String =
    when {
        hasPendingPurchases ->
            "A purchase is still pending. Premium will unlock after Google Play clears payment."
        premiumUnlocked -> "Premium purchases refreshed."
        hasCatalogProducts -> "No active premium purchase was found for this Play account."
        else -> productsReadyMessage(hasCatalogProducts = false)
    }

internal fun subscriptionHealthMessage(
    premiumUnlocked: Boolean,
    hasPendingPurchases: Boolean,
): String =
    when {
        hasPendingPurchases ->
            "Google Play shows a pending purchase. Premium remains locked until payment clears."
        premiumUnlocked -> "Premium subscription is active."
        else -> "No active premium purchase was found for this Play account."
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
