package com.kevpierce.catholicfasting.core.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams

object BillingContainer {
    @Volatile
    private var repositoryInstance: BillingRepository? = null

    fun initialize(
        context: Context,
        autoConnect: Boolean = true,
    ) {
        if (repositoryInstance == null) {
            synchronized(this) {
                if (repositoryInstance == null) {
                    val appContext = context.applicationContext
                    repositoryInstance =
                        BillingRepository(
                            billingClient =
                                BillingClient
                                    .newBuilder(appContext)
                                    .setListener { billingResult, purchases ->
                                        repositoryInstance?.onPurchasesUpdated(billingResult, purchases)
                                    }.enablePendingPurchases(
                                        PendingPurchasesParams
                                            .newBuilder()
                                            .enableOneTimeProducts()
                                            .build(),
                                    ).build(),
                            packageName = appContext.packageName,
                            autoConnect = autoConnect,
                        )
                }
            }
        }
    }

    val repository: BillingRepository
        get() =
            checkNotNull(repositoryInstance) {
                "BillingContainer.initialize(context) must be called before use."
            }
}
