package com.kevpierce.catholicfasting.core.billing

import android.app.Activity
import android.content.Context
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
    val catalog: SubscriptionOfferCatalog = SubscriptionOfferCatalog.catholicFasting,
    val premiumOffers: List<BillingOfferUi> = emptyList(),
    val tipOffers: List<BillingOfferUi> = emptyList(),
)

class BillingRepository(
    private val context: Context,
) : PurchasesUpdatedListener {
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
            connectAndRefresh()
        }
    }

    fun launchPurchase(
        activity: Activity,
        productId: String,
    ) {
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
        billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build(),
        )
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        scope.launch {
            val purchaseList = purchases.orEmpty()
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    acknowledgePurchases(purchaseList)
                    refreshPurchases()
                    state.value =
                        state.value.copy(
                            isPurchasing = false,
                            statusMessage = "Purchase completed.",
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
                            statusMessage = billingResult.debugMessage.ifBlank { "Google Play purchase failed." },
                        )
                }
            }
        }
    }

    private suspend fun connectAndRefresh() {
        state.value = state.value.copy(isLoading = true)
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
        refreshPurchases()
        state.value =
            state.value.copy(
                isLoading = false,
                premiumOffers = details.subscriptionOffers(),
                tipOffers = details.tipOffers(),
                statusMessage = "Google Play purchases ready.",
            )
    }

    private suspend fun refreshPurchases() {
        val purchases =
            queryPurchases(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
            )
        val premiumUnlocked = purchases.any(Purchase::isPurchasedAndActive)
        val healthMessage = if (premiumUnlocked) "Premium subscription is active." else "Premium subscription inactive."
        state.value =
            state.value.copy(
                premiumUnlocked = premiumUnlocked,
                subscriptionHealthMessage = healthMessage,
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
