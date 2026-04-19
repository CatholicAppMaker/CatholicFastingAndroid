package com.kevpierce.catholicfastingapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfastingapp.navigation.AppNavigationIntents
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntentInstrumentationTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun activityIntentUsesViewActionExpectedFlagsAndDeepLink() {
        val intent =
            AppNavigationIntents.activityIntent(
                context = context,
                deepLink = AppDeepLinks.MORE_SETUP,
            )

        assertThat(intent.action).isEqualTo(android.content.Intent.ACTION_VIEW)
        assertThat(intent.dataString).isEqualTo(AppDeepLinks.MORE_SETUP)
        assertThat(intent.component?.className).isEqualTo(MainActivity::class.java.name)
        assertThat(intent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
        assertThat(intent.flags and android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP).isNotEqualTo(0)
    }

    @Test
    fun activityPendingIntentLaunchesMainActivityWithExpectedDeepLink() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(MainActivity::class.java.name, null, false)

        try {
            val pendingIntent =
                AppNavigationIntents.activityPendingIntent(
                    context = context,
                    deepLink = AppDeepLinks.TRACKER,
                    requestCode = 8080,
                )

            pendingIntent.send()

            val launchedMain = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
            assertThat(launchedMain).isNotNull()
            assertThat(launchedMain!!.intent?.dataString).isEqualTo(AppDeepLinks.TRACKER)
            launchedMain.finish()
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }
}
