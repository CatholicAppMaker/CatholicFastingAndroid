package com.kevpierce.catholicfastingapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ShortcutManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.widget.CalendarWidgetLaunchActivity
import com.kevpierce.catholicfasting.core.widget.TodayWidgetLaunchActivity
import com.kevpierce.catholicfasting.core.widget.TrackerWidgetLaunchActivity
import com.kevpierce.catholicfasting.core.widget.WidgetSnapshotStore
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReleaseRoutingInstrumentationTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppContainer.initialize(context)
    }

    @Test
    fun manifestShortcutsExposeExpectedDeepLinks() {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val shortcuts =
            shortcutManager.manifestShortcuts
                .associateBy { it.id }

        assertThat(shortcuts.keys).containsExactly(
            "open_today",
            "track_fast",
            "friday_note",
            "reminder_center",
        )
        assertThat(shortcuts.getValue("open_today").intent!!.dataString).isEqualTo(AppDeepLinks.TODAY)
        assertThat(shortcuts.getValue("track_fast").intent!!.dataString).isEqualTo(AppDeepLinks.TRACKER)
        assertThat(shortcuts.getValue("friday_note").intent!!.dataString).isEqualTo(AppDeepLinks.CALENDAR_FRIDAY_NOTE)
        assertThat(shortcuts.getValue("reminder_center").intent!!.dataString).isEqualTo(AppDeepLinks.MORE_SETUP)
    }

    @Test
    fun androidSystemBackupIsDisabled() {
        assertThat(context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP).isEqualTo(0)
    }

    @Test
    fun widgetLaunchActivitiesOpenExpectedDeepLinks() {
        assertWidgetLaunchRoute(TodayWidgetLaunchActivity::class.java, AppDeepLinks.TODAY)
        assertWidgetLaunchRoute(CalendarWidgetLaunchActivity::class.java, AppDeepLinks.CALENDAR)
        assertWidgetLaunchRoute(TrackerWidgetLaunchActivity::class.java, AppDeepLinks.TRACKER)
    }

    @Test
    fun widgetSnapshotRefreshesAfterTrackerAndObservanceChanges() {
        val repository = AppContainer.repository
        val originalState = repository.dashboardState.value
        val observanceId =
            originalState.observances
                .first { it.obligation == ObservanceObligation.MANDATORY }
                .id
        val originalStatus = originalState.statusesById[observanceId] ?: CompletionStatus.NOT_STARTED
        val originalActiveFast = originalState.activeIntermittentFast

        ActivityScenario.launch(MainActivity::class.java).use {
            waitUntil("initial widget snapshot is written") {
                runBlocking { WidgetSnapshotStore.read(context).generatedAtIso.isNotBlank() }
            }

            val initialCompletionRate = runBlocking { WidgetSnapshotStore.read(context).completionRate }

            repository.startIntermittentFast()
            waitUntil("active fast is reflected in the widget snapshot") {
                runBlocking { WidgetSnapshotStore.read(context).hasActiveIntermittentFast }
            }

            repository.setStatus(observanceId, CompletionStatus.COMPLETED)
            waitUntil("completion changes are reflected in the widget snapshot") {
                runBlocking { WidgetSnapshotStore.read(context).completionRate > initialCompletionRate }
            }
        }

        repository.setStatus(observanceId, originalStatus)
        if (originalActiveFast == null) {
            repository.cancelIntermittentFast()
        }
    }

    private fun assertWidgetLaunchRoute(
        activityClass: Class<out Activity>,
        expectedDeepLink: String,
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(MainActivity::class.java.name, null, false)

        try {
            ActivityScenario.launch<Activity>(
                Intent(context, activityClass).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            ).use {
                val launchedMain = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
                assertThat(launchedMain).isNotNull()
                assertThat(launchedMain!!.intent.dataString).isEqualTo(expectedDeepLink)
                launchedMain.finish()
            }
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }

    private fun waitUntil(
        description: String,
        timeoutMs: Long = 5_000,
        condition: () -> Boolean,
    ) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (condition()) {
                return
            }
            Thread.sleep(100)
        }
        throw AssertionError("Timed out waiting for $description.")
    }
}
