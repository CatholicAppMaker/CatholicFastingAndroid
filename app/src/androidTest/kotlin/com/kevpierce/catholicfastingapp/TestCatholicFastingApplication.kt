package com.kevpierce.catholicfastingapp

import android.app.Application
import com.kevpierce.catholicfasting.core.billing.BillingContainer
import com.kevpierce.catholicfasting.core.data.AppContainer

/**
 * Keeps instrumentation startup deterministic by skipping live Play Billing
 * initialization and other release-only side effects.
 */
class TestCatholicFastingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
        BillingContainer.initialize(
            context = this,
            autoConnect = false,
        )
    }
}
