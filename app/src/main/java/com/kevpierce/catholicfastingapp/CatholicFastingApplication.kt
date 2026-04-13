package com.kevpierce.catholicfastingapp

import android.app.Application
import com.kevpierce.catholicfasting.core.billing.BillingContainer
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfastingapp.notifications.IntermittentFastNotificationManager
import com.kevpierce.catholicfastingapp.notifications.NotificationChannels

class CatholicFastingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
        BillingContainer.initialize(this)
        IntermittentFastNotificationManager.ensureChannel(this)
        NotificationChannels.ensureReminderChannel(this)
    }
}
