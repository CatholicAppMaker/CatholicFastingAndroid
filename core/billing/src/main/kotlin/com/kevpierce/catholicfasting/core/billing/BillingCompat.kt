package com.kevpierce.catholicfasting.core.billing

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

internal fun ProductDetails.productIdCompat(): String = getProductId()

internal fun ProductDetails.nameCompat(): String = getName()

internal fun ProductDetails.descriptionCompat(): String = getDescription()

internal fun ProductDetails.productTypeCompat(): String = getProductType()

internal fun Purchase.purchaseStateCompat(): Int = getPurchaseState()

internal fun Purchase.isAcknowledgedCompat(): Boolean = isAcknowledged()
