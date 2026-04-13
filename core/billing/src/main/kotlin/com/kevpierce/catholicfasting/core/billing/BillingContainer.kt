package com.kevpierce.catholicfasting.core.billing

import android.content.Context

object BillingContainer {
    @Volatile
    private var repositoryInstance: BillingRepository? = null

    fun initialize(context: Context) {
        if (repositoryInstance == null) {
            synchronized(this) {
                if (repositoryInstance == null) {
                    repositoryInstance = BillingRepository(context.applicationContext)
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
