package com.kevpierce.catholicfasting.core.billing

sealed interface BillingMessage {
    data object LoadingPurchases : BillingMessage

    data object RefreshingPurchases : BillingMessage

    data object PlayStillConnecting : BillingMessage

    data object PurchaseOptionUnavailable : BillingMessage

    data object OpeningCheckout : BillingMessage

    data object SubscriptionManagementUnavailable : BillingMessage

    data object OpeningSubscriptionManagement : BillingMessage

    data object UnableToOpenSubscriptionManagement : BillingMessage

    data object PurchaseCompleted : BillingMessage

    data object PurchaseCancelled : BillingMessage

    data object PurchasePending : BillingMessage

    data object PurchaseUpdated : BillingMessage

    data object PurchasesReady : BillingMessage

    data object ProductsMissing : BillingMessage

    data object PremiumPurchasesRefreshed : BillingMessage

    data object NoActivePremiumPurchase : BillingMessage

    data object PremiumSubscriptionActive : BillingMessage

    data object PendingPurchaseLocked : BillingMessage

    data object UnableToConnect : BillingMessage

    data class Failure(
        val kind: BillingFailureKind,
        val debugMessage: String = "",
    ) : BillingMessage
}

enum class BillingFailureKind {
    SERVICE_DISCONNECTED,
    PLAY_UNAVAILABLE,
    BILLING_UNAVAILABLE,
    ITEM_UNAVAILABLE,
    ITEM_ALREADY_OWNED,
    ITEM_NOT_OWNED,
    DEVELOPER_ERROR,
    FEATURE_NOT_SUPPORTED,
    GENERIC,
}
