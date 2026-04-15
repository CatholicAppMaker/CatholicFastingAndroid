package com.kevpierce.catholicfasting.core.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.kevpierce.catholicfasting.core.model.SubscriptionOfferCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class BillingOfferUi(
    val productId: String,
    val displayTitle: String,
    val priceLabel: String,
    val billingLabel: String,
)

data class BillingState(
    val premiumUnlocked: Boolean = false,
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val statusMessage: String = "Loading Google Play purchases.",
    val subscriptionHealthMessage: String = "",
    val canManageSubscription: Boolean = false,
    val hasPendingPurchases: Boolean = false,
    val activeSubscriptionProductId: String? = null,
    val hasCatalogProducts: Boolean = false,
    val catalog: SubscriptionOfferCatalog = SubscriptionOfferCatalog.catholicFasting,
    val premiumOffers: List<BillingOfferUi> = emptyList(),
    val tipOffers: List<BillingOfferUi> = emptyList(),
)

class BillingRepository(
    private val context: Context,
) : PurchasesUpdatedListener {
    private data class PurchaseSnapshot(
        val premiumUnlocked: Boolean,
        val hasPendingPurchases: Boolean,
        val activeSubscriptionProductId: String? = null,
        val subscriptionHealthMessage: String,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val state = MutableStateFlow(BillingState(isLoading = true))
    private val billingClient =
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build(),
            )
            .build()
    private var productDetailsById: Map<String, ProductDetails> = emptyMap()

    val billingState: StateFlow<BillingState> = state.asStateFlow()

    init {
        scope.launch {
            connectAndRefresh()
        }
    }

    fun refresh() {
        scope.launch {
            connectAndRefresh(userInitiated = true)
        }
    }

    fun launchPurchase(
        activity: Activity,
        productId: String,
    ) {
        if (!billingClient.isReady) {
            state.value =
                state.value.copy(
                    statusMessage = "Google Play is still connecting. Refresh purchases and try again.",
                )
            refresh()
            return
        }
        val productDetails = productDetailsById[productId]
        if (productDetails == null) {
            state.value = state.value.copy(statusMessage = "That purchase option is not available yet.")
            return
        }

        val productParams =
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .apply {
                    productDetails.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.offerToken
                        ?.let(::setOfferToken)
                }.build()

        state.value = state.value.copy(isPurchasing = true, statusMessage = "Opening Google Play checkout.")
        val launchResult =
            billingClient.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productParams))
                    .build(),
            )
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            state.value =
                state.value.copy(
                    isPurchasing = false,
                    statusMessage = billingResponseMessage(launchResult.responseCode, launchResult.debugMessage),
                )
        }
    }

    fun openManageSubscription() {
        val productId = state.value.activeSubscriptionProductId ?: state.value.catalog.offers.firstOrNull()?.id
        if (productId == null) {
            state.value =
                state.value.copy(
                    statusMessage = "Subscription management is not available until Play products are configured.",
                )
            return
        }

        val primaryIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    manageSubscriptionsUrl(
                        packageName = context.packageName,
                        productId = productId,
                    ),
                ),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val fallbackIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(primaryIntent) }
            .recoverCatching { context.startActivity(fallbackIntent) }
            .onSuccess {
                state.value =
                    state.value.copy(
                        statusMessage = "Opening Google Play subscription management.",
                    )
            }.onFailure {
                state.value =
                    state.value.copy(
                        statusMessage = "Unable to open Google Play subscription management on this device.",
                    )
            }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        scope.launch {
            val purchaseList = purchases.orEmpty()
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val hasPendingPurchases =
                        purchaseList.any {
                            it.purchaseStateCompat() == Purchase.PurchaseState.PENDING
                        }
                    val purchasedItems = purchaseList.filter(Purchase::isPurchasedAndActive)
                    acknowledgePurchases(purchasedItems)
                    val purchaseSnapshot = refreshPurchases()
                    state.value =
                        state.value.copy(
                            isPurchasing = false,
                            premiumUnlocked = purchaseSnapshot.premiumUnlocked,
                            hasPendingPurchases = purchaseSnapshot.hasPendingPurchases,
                            activeSubscriptionProductId = purchaseSnapshot.activeSubscriptionProductId,
                            subscriptionHealthMessage = purchaseSnapshot.subscriptionHealthMessage,
                            statusMessage =
                                purchaseUpdateMessage(
                                    hasPurchased = purchasedItems.isNotEmpty(),
                                    hasPending = hasPendingPurchases,
                                ),
                        )
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    state.value =
                        state.value.copy(
                            isPurchasing = false,
                            statusMessage = "Purchase cancelled.",
                        )
                }
                else -> {
                    state.value =
                        state.value.copy(
                            isPurchasing = false,
                            statusMessage =
                                billingResponseMessage(
                                    responseCode = billingResult.responseCode,
                                    debugMessage = billingResult.debugMessage,
                                ),
                        )
                }
            }
        }
    }

    private suspend fun connectAndRefresh(userInitiated: Boolean = false) {
        state.value =
            state.value.copy(
                isLoading = true,
                statusMessage =
                    if (userInitiated) {
                        "Refreshing Google Play purchases."
                    } else {
                        state.value.statusMessage
                    },
            )
        val connected = ensureConnected()
        if (!connected) {
            state.value =
                state.value.copy(
                    isLoading = false,
                    statusMessage = "Unable to connect to Google Play Billing.",
                )
            return
        }

        val details = queryProductDetails()
        productDetailsById = details.associateBy(ProductDetails::getProductId)
        val premiumOffers = details.subscriptionOffers()
        val tipOffers = details.tipOffers()
        val hasCatalogProducts = premiumOffers.isNotEmpty() || tipOffers.isNotEmpty()
        val purchaseSnapshot = refreshPurchases()
        state.value =
            state.value.copy(
                premiumUnlocked = purchaseSnapshot.premiumUnlocked,
                isLoading = false,
                canManageSubscription = true,
                hasPendingPurchases = purchaseSnapshot.hasPendingPurchases,
                activeSubscriptionProductId = purchaseSnapshot.activeSubscriptionProductId,
                hasCatalogProducts = hasCatalogProducts,
                subscriptionHealthMessage = purchaseSnapshot.subscriptionHealthMessage,
                premiumOffers = premiumOffers,
                tipOffers = tipOffers,
                statusMessage =
                    if (userInitiated) {
                        purchaseRefreshMessage(
                            premiumUnlocked = purchaseSnapshot.premiumUnlocked,
                            hasPendingPurchases = purchaseSnapshot.hasPendingPurchases,
                            hasCatalogProducts = hasCatalogProducts,
                        )
                    } else {
                        productsReadyMessage(hasCatalogProducts)
                    },
            )
    }

    private suspend fun refreshPurchases(): PurchaseSnapshot {
        val purchases =
            queryPurchases(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
            )
        val premiumUnlocked = purchases.any(Purchase::isPurchasedAndActive)
        val hasPendingPurchases = purchases.any { it.purchaseStateCompat() == Purchase.PurchaseState.PENDING }
        return PurchaseSnapshot(
            premiumUnlocked = premiumUnlocked,
            hasPendingPurchases = hasPendingPurchases,
            activeSubscriptionProductId =
                purchases.firstOrNull(Purchase::isPurchasedAndActive)?.productsCompat()?.firstOrNull(),
            subscriptionHealthMessage =
                subscriptionHealthMessage(
                    premiumUnlocked = premiumUnlocked,
                    hasPendingPurchases = hasPendingPurchases,
                ),
        )
    }

    private suspend fun ensureConnected(): Boolean =
        if (billingClient.isReady) {
            true
        } else {
            suspendCancellableCoroutine { continuation ->
                billingClient.startConnection(
                    object : BillingClientStateListener {
                        override fun onBillingServiceDisconnected() {
                            if (continuation.isActive) {
                                continuation.resume(false)
                            }
                        }

                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            continuation.resume(
                                billingResult.responseCode == BillingClient.BillingResponseCode.OK,
                            )
                        }
                    },
                )
            }
        }

    private suspend fun queryProductDetails(): List<ProductDetails> {
        val productList =
            state.value.catalog.subscriptionProductIds().map(::subscriptionProductQuery) +
                tipProducts().map(::inAppProductQuery)

        val params =
            QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(queryProductDetailsResult.productDetailsList)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }
    }

    private suspend fun acknowledgePurchases(purchases: List<Purchase>) {
        purchases.filterNot(Purchase::isAcknowledged).forEach { purchase ->
            suspendCancellableCoroutine { continuation ->
                billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build(),
                ) {
                    continuation.resume(Unit)
                }
            }
        }
    }

    private suspend fun queryPurchases(params: QueryPurchasesParams): List<Purchase> =
        suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchasesList)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }
}
