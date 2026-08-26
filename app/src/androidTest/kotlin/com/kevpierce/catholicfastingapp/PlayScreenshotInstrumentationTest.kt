package com.kevpierce.catholicfastingapp

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfasting.core.ui.CatholicFastingTheme
import com.kevpierce.catholicfasting.feature.calendar.CALENDAR_LIST_TEST_TAG
import com.kevpierce.catholicfastingapp.ui.CatholicFastingApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream
import com.kevpierce.catholicfasting.feature.calendar.R as CalendarR
import com.kevpierce.catholicfasting.feature.premium.R as PremiumR
import com.kevpierce.catholicfasting.feature.today.R as TodayR
import com.kevpierce.catholicfasting.feature.tracker.R as TrackerR

@RunWith(AndroidJUnit4::class)
class PlayScreenshotInstrumentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun seedTruthfulFixture() {
        ReleaseTestFixture.seed(context)
    }

    @Test
    fun captureTruthfulPlayListingSet() {
        val deepLink = mutableStateOf(AppDeepLinks.TODAY)
        composeRule.setContent {
            val route by deepLink
            CatholicFastingTheme {
                key(route) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        CatholicFastingApp(
                            initialDeepLink = route,
                            clock = ReleaseTestFixture.fixedClock,
                        )
                    }
                }
            }
        }

        capture(
            deepLink = deepLink,
            route = AppDeepLinks.TODAY,
            routeText = context.getString(TodayR.string.today_title),
            focalText = context.getString(TodayR.string.today_companion_title),
            fileName = "raw-today-1080x2400.png",
        )
        capture(
            deepLink = deepLink,
            route = AppDeepLinks.CALENDAR,
            routeText = context.getString(CalendarR.string.calendar_title),
            focalText = "Ash Wednesday",
            fileName = "raw-fasting-days-1080x2400.png",
            scrollToFocalText = true,
            lazyListTag = CALENDAR_LIST_TEST_TAG,
        )
        capture(
            deepLink = deepLink,
            route = AppDeepLinks.TRACKER,
            routeText = context.getString(TrackerR.string.tracker_title),
            focalText = context.getString(TrackerR.string.tracker_fast_in_progress),
            fileName = "raw-track-fast-1080x2400.png",
        )
        capture(
            deepLink = deepLink,
            route = AppDeepLinks.MORE_SETUP,
            routeText = context.getString(R.string.more_setup_reminders),
            focalText = context.getString(R.string.more_reminder_center_title),
            fileName = "raw-reminder-center-1080x2400.png",
            scrollToFocalText = true,
        )
        capture(
            deepLink = deepLink,
            route = AppDeepLinks.MORE_PREMIUM,
            routeText = context.getString(R.string.more_support_premium),
            focalText = context.getString(PremiumR.string.premium_guided_journey_title),
            fileName = "raw-premium-1080x2400.png",
            scrollToFocalText = true,
        )
        capture(
            deepLink = deepLink,
            route = AppDeepLinks.MORE_PRIVACY,
            routeText = context.getString(R.string.more_privacy_data),
            focalText = context.getString(R.string.more_data_stored_title),
            fileName = "raw-privacy-data-1080x2400.png",
            scrollToFocalText = true,
        )
    }

    private fun capture(
        deepLink: androidx.compose.runtime.MutableState<String>,
        route: String,
        routeText: String,
        focalText: String,
        fileName: String,
        scrollToFocalText: Boolean = false,
        lazyListTag: String? = null,
    ) {
        composeRule.runOnIdle { deepLink.value = route }
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 10_000L) {
            composeRule.onAllNodesWithText(routeText).fetchSemanticsNodes().isNotEmpty()
        }
        if (lazyListTag != null) {
            composeRule.waitUntil(timeoutMillis = 10_000L) {
                composeRule.onAllNodesWithTag(lazyListTag).fetchSemanticsNodes().isNotEmpty()
            }
        }
        val selectedNavigation =
            composeRule.onNode(
                hasText(selectedNavigationLabel(route)).and(hasClickAction()),
            )
        selectedNavigation.assertIsDisplayed().assertIsSelected()
        if (route == AppDeepLinks.MORE_SETUP ||
            route == AppDeepLinks.MORE_PREMIUM ||
            route == AppDeepLinks.MORE_PRIVACY
        ) {
            composeRule
                .onNode(hasText(routeText).and(hasClickAction()))
                .performScrollTo()
                .assertIsDisplayed()
                .assertIsSelected()
        }
        if (scrollToFocalText) {
            if (lazyListTag == null) {
                composeRule
                    .onNodeWithText(focalText, ignoreCase = true)
                    .performScrollTo()
            } else {
                composeRule
                    .onNodeWithTag(lazyListTag)
                    .performScrollToNode(hasText(focalText, ignoreCase = true))
            }
        }
        composeRule.waitUntil(timeoutMillis = 10_000L) {
            composeRule.onAllNodesWithText(focalText, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(focalText, ignoreCase = true).assertIsDisplayed()
        composeRule.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val remotePath = "$REMOTE_SCREENSHOT_DIR/$fileName"
        executeShellCommand("mkdir -p $REMOTE_SCREENSHOT_DIR")
        executeShellCommand("screencap -p $remotePath")
        assertThat(executeShellCommand("stat -c %s $remotePath").trim().toLong()).isGreaterThan(0L)
    }

    private fun selectedNavigationLabel(route: String): String =
        context.getString(
            when (route) {
                AppDeepLinks.TODAY -> R.string.nav_today
                AppDeepLinks.CALENDAR -> R.string.nav_fasting_days
                AppDeepLinks.TRACKER -> R.string.nav_track_fast
                else -> R.string.nav_more
            },
        )

    private fun executeShellCommand(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return descriptor.use { FileInputStream(it.fileDescriptor).bufferedReader().readText() }
    }

    private companion object {
        const val REMOTE_SCREENSHOT_DIR = "/sdcard/cfa-play-screenshots"
    }
}
