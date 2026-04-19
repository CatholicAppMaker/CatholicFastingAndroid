package com.kevpierce.catholicfasting.core.billing

import android.content.Context

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
                    repositoryInstance =
                        BillingRepository(
                            context = context.applicationContext,
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
