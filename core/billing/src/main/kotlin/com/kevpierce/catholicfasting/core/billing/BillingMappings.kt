package com.kevpierce.catholicfasting.core.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.kevpierce.catholicfasting.core.model.SubscriptionOfferCatalog

internal fun List<ProductDetails>.subscriptionOffers(): List<BillingOfferUi> =
    filter { it.productTypeCompat() == BillingClient.ProductType.SUBS }
        .map { product ->
            val pricing =
                product.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.pricingPhases
                    ?.pricingPhaseList
                    ?.firstOrNull()
            BillingOfferUi(
                productId = product.productIdCompat(),
                displayTitle = product.nameCompat(),
                priceLabel = pricing?.formattedPrice ?: "Unavailable",
                billingLabel = product.descriptionCompat().ifBlank { "Auto-renewing subscription" },
            )
        }

internal fun List<ProductDetails>.tipOffers(): List<BillingOfferUi> =
    filter { it.productTypeCompat() == BillingClient.ProductType.INAPP }
        .map { product ->
            BillingOfferUi(
                productId = product.productIdCompat(),
                displayTitle = product.nameCompat(),
                priceLabel = product.oneTimePurchaseOfferDetails?.formattedPrice ?: "Unavailable",
                billingLabel = product.descriptionCompat().ifBlank { "One-time support tip" },
            )
        }

internal fun Purchase.isPurchasedAndActive(): Boolean {
    return purchaseStateCompat() == Purchase.PurchaseState.PURCHASED && isAcknowledgedCompat()
}

internal fun SubscriptionOfferCatalog.subscriptionProductIds(): List<String> = offers.map { it.id }

internal fun tipProducts(): List<String> = BillingProductCatalog.tipProducts

internal fun subscriptionProductQuery(productId: String): QueryProductDetailsParams.Product =
    QueryProductDetailsParams.Product.newBuilder()
        .setProductId(productId)
        .setProductType(BillingClient.ProductType.SUBS)
        .build()

internal fun inAppProductQuery(productId: String): QueryProductDetailsParams.Product =
    QueryProductDetailsParams.Product.newBuilder()
        .setProductId(productId)
        .setProductType(BillingClient.ProductType.INAPP)
        .build()
