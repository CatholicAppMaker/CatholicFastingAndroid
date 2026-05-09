package com.kevpierce.catholicfastingapp

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.widget.CatholicFastingWidgetReceiver
import com.kevpierce.catholicfasting.core.widget.WidgetSnapshotStore
import com.kevpierce.catholicfastingapp.notifications.NotificationActionReceiver
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser
import com.kevpierce.catholicfasting.core.widget.R as WidgetR

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
    fun backupAndTransferRulesExcludeLocalAppData() {
        assertThat(xmlExcludes(R.xml.backup_rules)).containsAtLeast(
            "sharedpref:.",
            "database:.",
            "file:.",
        )
        assertThat(xmlExcludes(R.xml.data_extraction_rules)).containsAtLeast(
            "sharedpref:.",
            "database:.",
            "file:.",
        )
    }

    @Test
    fun notificationAndWidgetReceiversAreNotExported() {
        val receivers =
            packageInfo(PackageManager.GET_RECEIVERS)
                .receivers
                .orEmpty()
                .associateBy { it.name }

        assertThat(receivers.getValue(NotificationActionReceiver::class.java.name).exported).isFalse()
        assertThat(receivers.getValue(CatholicFastingWidgetReceiver::class.java.name).exported).isFalse()
    }

    @Test
    fun mainActivityExposesShortcutMetadataAndBrowsableDeepLinks() {
        val activityInfo =
            activityInfo(
                android.content.ComponentName(context, MainActivity::class.java),
                PackageManager.GET_META_DATA,
            )

        assertThat(activityInfo.exported).isTrue()
        assertThat(activityInfo.metaData.getInt("android.app.shortcuts")).isEqualTo(R.xml.shortcuts)

        publicDeepLinks().forEach { deepLink ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).addCategory(Intent.CATEGORY_BROWSABLE)

            val resolved = intent.resolveActivity(context.packageManager)

            assertThat(resolved).isNotNull()
            assertThat(resolved!!.className).isEqualTo(MainActivity::class.java.name)
        }
    }

    @Test
    fun widgetProviderMetadataMatchesHomeScreenReleaseContract() {
        val parser = context.resources.getXml(WidgetR.xml.catholic_fasting_widget_info)
        val attributes = mutableMapOf<String, String>()
        parser.use {
            while (it.next() != XmlPullParser.END_DOCUMENT) {
                if (it.eventType == XmlPullParser.START_TAG && it.name == "appwidget-provider") {
                    repeat(it.attributeCount) { index ->
                        attributes[it.getAttributeName(index)] = it.getAttributeValue(index)
                    }
                }
            }
        }

        assertThat(attributes["updatePeriodMillis"]).isEqualTo("0")
        assertThat(attributes["widgetCategory"]).isEqualTo("0x1")
        assertThat(attributes["resizeMode"]).isEqualTo("0x3")
    }

    @Test
    fun mainActivityColdLaunchAndRecreateDoNotCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertThat(activity.intent?.dataString).isNull()
            }

            scenario.recreate()

            scenario.onActivity { activity ->
                assertThat(activity.intent?.dataString).isNull()
            }
        }
    }

    @Test
    fun publicDeepLinksResolveAndLaunchMainActivity() {
        publicDeepLinks().forEach { deepLink ->
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
                    .setPackage(context.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val resolvedActivity = intent.resolveActivity(context.packageManager)

            assertThat(resolvedActivity).isNotNull()
            assertThat(resolvedActivity!!.className).isEqualTo(MainActivity::class.java.name)

            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                scenario.onActivity { activity ->
                    assertThat(activity.intent?.dataString).isEqualTo(deepLink)
                }
            }
        }
    }

    @Test
    fun mainActivityAcceptsWidgetDeepLinkExtras() {
        listOf(AppDeepLinks.TODAY, AppDeepLinks.CALENDAR, AppDeepLinks.TRACKER).forEach { deepLink ->
            ActivityScenario
                .launch<MainActivity>(
                    Intent(context, MainActivity::class.java)
                        .putExtra(AppDeepLinks.EXTRA_INITIAL_DEEP_LINK, deepLink)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                ).use { scenario ->
                    scenario.onActivity { activity ->
                        assertThat(activity.intent.getStringExtra(AppDeepLinks.EXTRA_INITIAL_DEEP_LINK))
                            .isEqualTo(deepLink)
                    }
                }
        }
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

    private fun publicDeepLinks(): List<String> =
        listOf(
            AppDeepLinks.TODAY,
            AppDeepLinks.CALENDAR,
            AppDeepLinks.TRACKER,
            AppDeepLinks.MORE_PREMIUM,
            AppDeepLinks.MORE_SETUP,
            AppDeepLinks.MORE_PRIVACY,
            AppDeepLinks.CALENDAR_FRIDAY_NOTE,
        )

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

    private fun xmlExcludes(resourceId: Int): List<String> {
        val parser = context.resources.getXml(resourceId)
        return parser.use {
            buildList {
                while (it.next() != XmlPullParser.END_DOCUMENT) {
                    if (it.eventType == XmlPullParser.START_TAG && it.name == "exclude") {
                        add("${it.getAttributeValue(null, "domain")}:${it.getAttributeValue(null, "path")}")
                    }
                }
            }
        }
    }

    private fun packageInfo(flags: Int): android.content.pm.PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, flags)
        }

    private fun activityInfo(
        componentName: android.content.ComponentName,
        flags: Int,
    ): ActivityInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getActivityInfo(
                componentName,
                PackageManager.ComponentInfoFlags.of(flags.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getActivityInfo(componentName, flags)
        }
}
