package com.kevpierce.catholicfasting.feature.premium

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.billing.BillingFailureKind
import com.kevpierce.catholicfasting.core.billing.BillingMessage

@Composable
internal fun BillingMessage.localizedText(): String =
    when (this) {
        is BillingMessage.Failure -> localizedFailureText()
        else -> stringResource(simpleMessageResId())
    }

@StringRes
private fun BillingMessage.simpleMessageResId(): Int =
    when (this) {
        BillingMessage.LoadingPurchases,
        BillingMessage.RefreshingPurchases,
        BillingMessage.PlayStillConnecting,
        BillingMessage.PurchaseOptionUnavailable,
        BillingMessage.OpeningCheckout,
        BillingMessage.SubscriptionManagementUnavailable,
        BillingMessage.OpeningSubscriptionManagement,
        BillingMessage.UnableToOpenSubscriptionManagement,
        BillingMessage.UnableToConnect,
        ->
            connectionMessageResId()
        BillingMessage.PurchaseCompleted,
        BillingMessage.PurchaseCancelled,
        BillingMessage.PurchasePending,
        BillingMessage.PurchaseUpdated,
        BillingMessage.PurchasesReady,
        BillingMessage.ProductsMissing,
        BillingMessage.PremiumPurchasesRefreshed,
        BillingMessage.NoActivePremiumPurchase,
        BillingMessage.PremiumSubscriptionActive,
        BillingMessage.PendingPurchaseLocked,
        ->
            purchaseMessageResId()
        is BillingMessage.Failure -> throw IllegalArgumentException("Failure messages require dedicated handling.")
    }

@StringRes
private fun BillingMessage.connectionMessageResId(): Int =
    when (this) {
        BillingMessage.LoadingPurchases -> R.string.premium_billing_loading
        BillingMessage.RefreshingPurchases -> R.string.premium_billing_refreshing_status
        BillingMessage.PlayStillConnecting -> R.string.premium_billing_still_connecting
        BillingMessage.PurchaseOptionUnavailable -> R.string.premium_billing_purchase_option_unavailable
        BillingMessage.OpeningCheckout -> R.string.premium_billing_opening_checkout
        BillingMessage.SubscriptionManagementUnavailable -> R.string.premium_billing_manage_unavailable
        BillingMessage.OpeningSubscriptionManagement -> R.string.premium_billing_opening_manage_subscription
        BillingMessage.UnableToOpenSubscriptionManagement ->
            R.string.premium_billing_unable_to_open_manage_subscription
        BillingMessage.UnableToConnect -> R.string.premium_billing_unable_to_connect
        else -> throw IllegalArgumentException("Unsupported connection billing message.")
    }

@StringRes
private fun BillingMessage.purchaseMessageResId(): Int =
    when (this) {
        BillingMessage.PurchaseCompleted -> R.string.premium_billing_purchase_completed
        BillingMessage.PurchaseCancelled -> R.string.premium_billing_purchase_cancelled
        BillingMessage.PurchasePending -> R.string.premium_billing_purchase_pending_status
        BillingMessage.PurchaseUpdated -> R.string.premium_billing_purchase_updated
        BillingMessage.PurchasesReady -> R.string.premium_billing_purchases_ready
        BillingMessage.ProductsMissing -> R.string.premium_billing_products_missing
        BillingMessage.PremiumPurchasesRefreshed -> R.string.premium_billing_purchases_refreshed
        BillingMessage.NoActivePremiumPurchase -> R.string.premium_billing_no_active_purchase
        BillingMessage.PremiumSubscriptionActive -> R.string.premium_billing_subscription_active
        BillingMessage.PendingPurchaseLocked -> R.string.premium_billing_pending_locked
        else -> throw IllegalArgumentException("Unsupported purchase billing message.")
    }

@Composable
private fun BillingMessage.Failure.localizedFailureText(): String {
    val baseMessage =
        when (kind) {
            BillingFailureKind.SERVICE_DISCONNECTED ->
                stringResource(R.string.premium_billing_failure_service_disconnected)
            BillingFailureKind.PLAY_UNAVAILABLE ->
                stringResource(R.string.premium_billing_failure_play_unavailable)
            BillingFailureKind.BILLING_UNAVAILABLE ->
                stringResource(R.string.premium_billing_failure_billing_unavailable)
            BillingFailureKind.ITEM_UNAVAILABLE ->
                stringResource(R.string.premium_billing_failure_item_unavailable)
            BillingFailureKind.ITEM_ALREADY_OWNED ->
                stringResource(R.string.premium_billing_failure_item_already_owned)
            BillingFailureKind.ITEM_NOT_OWNED ->
                stringResource(R.string.premium_billing_failure_item_not_owned)
            BillingFailureKind.DEVELOPER_ERROR ->
                stringResource(R.string.premium_billing_failure_developer_error)
            BillingFailureKind.FEATURE_NOT_SUPPORTED ->
                stringResource(R.string.premium_billing_failure_feature_not_supported)
            BillingFailureKind.GENERIC ->
                stringResource(R.string.premium_billing_failure_generic)
        }

    return if (debugMessage.isBlank()) {
        baseMessage
    } else {
        stringResource(R.string.premium_billing_failure_with_debug, baseMessage, debugMessage)
    }
}
