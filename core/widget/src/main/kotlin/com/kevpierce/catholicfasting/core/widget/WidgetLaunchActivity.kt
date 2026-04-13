package com.kevpierce.catholicfasting.core.widget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.kevpierce.catholicfasting.core.model.AppDeepLinks

open class WidgetLaunchActivity : Activity() {
    protected open val deepLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                data = intent?.data ?: deepLink?.let(android.net.Uri::parse)
            },
        )
        finish()
    }
}

class TodayWidgetLaunchActivity : WidgetLaunchActivity() {
    override val deepLink: String = AppDeepLinks.TODAY
}

class CalendarWidgetLaunchActivity : WidgetLaunchActivity() {
    override val deepLink: String = AppDeepLinks.CALENDAR
}

class TrackerWidgetLaunchActivity : WidgetLaunchActivity() {
    override val deepLink: String = AppDeepLinks.TRACKER
}
