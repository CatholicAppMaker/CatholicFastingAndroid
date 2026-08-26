@file:Suppress("LongMethod", "TooManyFunctions")

package com.kevpierce.catholicfastingapp

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DarkMode
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.then
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.ui.CatholicFastingTheme
import com.kevpierce.catholicfasting.feature.calendar.CALENDAR_LIST_TEST_TAG
import com.kevpierce.catholicfasting.feature.premium.PREMIUM_LIST_TEST_TAG
import com.kevpierce.catholicfasting.feature.today.TODAY_COMPANION_ACTION_TEST_TAG_PREFIX
import com.kevpierce.catholicfasting.feature.today.TODAY_LIST_TEST_TAG
import com.kevpierce.catholicfasting.feature.tracker.TRACKER_END_FAST_TEST_TAG
import com.kevpierce.catholicfasting.feature.tracker.TRACKER_LIST_TEST_TAG
import com.kevpierce.catholicfasting.feature.tracker.TRACKER_PROGRESS_TEST_TAG
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

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class ResponsiveMatrixInstrumentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun seedTruthfulFixture() {
        ReleaseTestFixture.seed(context)
    }

    @Test
    fun compact320x720At100PercentFont() {
        verifyProfile(ResponsiveProfile(320, 720, 1.0f))
    }

    @Test
    fun compact320x720At140PercentFont() {
        verifyProfile(ResponsiveProfile(320, 720, 1.4f))
    }

    @Test
    fun compact320x720At200PercentFont() {
        verifyProfile(ResponsiveProfile(320, 720, 2.0f))
    }

    @Test
    fun reference411x891At100PercentFont() {
        verifyProfile(ResponsiveProfile(411, 891, 1.0f))
    }

    @Test
    fun reference411x891At140PercentFont() {
        verifyProfile(ResponsiveProfile(411, 891, 1.4f))
    }

    @Test
    fun reference411x891At200PercentFont() {
        verifyProfile(ResponsiveProfile(411, 891, 2.0f))
    }

    @Test
    fun tablet600x960At100PercentFont() {
        verifyProfile(ResponsiveProfile(600, 960, 1.0f))
    }

    @Test
    fun tablet600x960At140PercentFont() {
        verifyProfile(ResponsiveProfile(600, 960, 1.4f))
    }

    @Test
    fun tablet600x960At200PercentFont() {
        verifyProfile(ResponsiveProfile(600, 960, 2.0f))
    }

    @Test
    fun captureResponsiveStressEvidence() {
        val captures =
            listOf(
                StressCapture(
                    profile = ResponsiveProfile(320, 720, 2.0f),
                    darkMode = false,
                    route = AppDeepLinks.TODAY,
                    fileName = "320x720-font200-light-today.png",
                ),
                StressCapture(
                    profile = ResponsiveProfile(320, 720, 2.0f),
                    darkMode = true,
                    route = AppDeepLinks.TRACKER,
                    fileName = "320x720-font200-dark-track-fast.png",
                ),
                StressCapture(
                    profile = ResponsiveProfile(411, 891, 1.4f),
                    darkMode = false,
                    route = AppDeepLinks.MORE_SETUP,
                    fileName = "411x891-font140-light-reminder-center.png",
                ),
                StressCapture(
                    profile = ResponsiveProfile(411, 891, 1.4f),
                    darkMode = true,
                    route = AppDeepLinks.MORE_PREMIUM,
                    fileName = "411x891-font140-dark-premium.png",
                ),
                StressCapture(
                    profile = ResponsiveProfile(600, 960, 2.0f),
                    darkMode = false,
                    route = AppDeepLinks.CALENDAR,
                    fileName = "600x960-font200-light-fasting-days.png",
                ),
                StressCapture(
                    profile = ResponsiveProfile(600, 960, 2.0f),
                    darkMode = true,
                    route = AppDeepLinks.MORE_PRIVACY,
                    fileName = "600x960-font200-dark-privacy-data.png",
                ),
            )

        val currentCapture = mutableStateOf(captures.first())
        val deepLink = mutableStateOf(captures.first().route)
        composeRule.setContent {
            val capture by currentCapture
            val route by deepLink
            DeviceConfigurationOverride(capture.profile.configuration(capture.darkMode)) {
                CatholicFastingTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Box(
                            modifier = Modifier.fillMaxSize().testTag(VIEWPORT_TEST_TAG),
                        ) {
                            key(capture.profile, capture.darkMode, route) {
                                CatholicFastingApp(
                                    initialDeepLink = route,
                                    clock = ReleaseTestFixture.fixedClock,
                                )
                            }
                        }
                    }
                }
            }
        }
        captures.forEach { capture ->
            composeRule.runOnIdle {
                currentCapture.value = capture
                deepLink.value = capture.route
            }
            waitForRoute(capture.route)
            assertViewport(capture.profile)
            verifyStressRoute(capture)
            composeRule.waitForIdle()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            captureToDevice(capture.fileName)
        }
    }

    private fun verifyProfile(profile: ResponsiveProfile) {
        val deepLink = setContentForProfile(profile, darkMode = false, initialRoute = AppDeepLinks.TODAY)
        verifyAllRoutes(profile, deepLink)
    }

    private fun verifyAllRoutes(
        profile: ResponsiveProfile,
        deepLink: MutableState<String>,
    ) {
        val routes =
            listOf(
                RouteCase(
                    deepLink = AppDeepLinks.TODAY,
                    destination = Destination.TODAY,
                ),
                RouteCase(
                    deepLink = AppDeepLinks.CALENDAR,
                    destination = Destination.FASTING_DAYS,
                ),
                RouteCase(
                    deepLink = AppDeepLinks.TRACKER,
                    destination = Destination.TRACK_FAST,
                ),
                RouteCase(
                    deepLink = AppDeepLinks.MORE_SETUP,
                    destination = Destination.MORE,
                ),
                RouteCase(
                    deepLink = AppDeepLinks.MORE_PREMIUM,
                    destination = Destination.MORE,
                ),
                RouteCase(
                    deepLink = AppDeepLinks.MORE_PRIVACY,
                    destination = Destination.MORE,
                ),
            )

        routes.forEach { route ->
            composeRule.runOnIdle { deepLink.value = route.deepLink }
            waitForRoute(route.deepLink)
            assertViewport(profile)
            assertBottomNavigation(route.destination)
            when (route.deepLink) {
                AppDeepLinks.TODAY -> verifyToday(profile)
                AppDeepLinks.CALENDAR -> verifyCalendar(profile)
                AppDeepLinks.TRACKER -> verifyTracker(profile)
                AppDeepLinks.MORE_SETUP -> verifyReminderCenter(profile)
                AppDeepLinks.MORE_PREMIUM -> verifyPremium(profile)
                AppDeepLinks.MORE_PRIVACY -> verifyPrivacy(profile)
            }
        }
    }

    private fun verifyToday(profile: ResponsiveProfile) {
        val title = onHeading(com.kevpierce.catholicfasting.feature.today.R.string.today_title)
        title.assertIsDisplayed()
        assertContained(title, profile, "Today title")

        val companion = onText(com.kevpierce.catholicfasting.feature.today.R.string.today_companion_title)
        companion.performScrollTo().assertIsDisplayed()
        assertContained(companion, profile, "Today companion")

        val action = onNodeWithTag(TODAY_COMPANION_ACTION_TEST_TAG_PREFIX + "today-rule")
        action.performScrollTo().assertIsDisplayed().assertHasClickAction()
        assertTouchTarget(action, "Today companion action")
        assertContained(action, profile, "Today companion action")

        val activeFastDetail =
            context.getString(
                TodayR.string.today_companion_active_fast_value,
                "1h 0m",
                "15h 0m",
            )
        val activeFastLine =
            context.getString(
                TodayR.string.today_companion_intention_value,
                activeFastDetail,
                IntermittentFastIntention.PRAYER.label,
            )
        val activeFast = onLiteralText(activeFastLine)
        activeFast.performScrollTo().assertIsDisplayed()
        assertContained(activeFast, profile, "Today active fast detail")

        val todayList = onNodeWithTag(TODAY_LIST_TEST_TAG)
        todayList.performScrollToNode(
            hasText(
                context.getString(TodayR.string.today_important_notice_title),
            ),
        )
        val notice = onText(com.kevpierce.catholicfasting.feature.today.R.string.today_important_notice_title)
        notice.assertIsDisplayed()
        assertContained(notice, profile, "Today notice")
    }

    private fun verifyCalendar(profile: ResponsiveProfile) {
        val title = onHeading(CalendarR.string.calendar_title)
        title.assertIsDisplayed()
        assertContained(title, profile, "Calendar title")

        val calendarList = onNodeWithTag(CALENDAR_LIST_TEST_TAG)
        val searchLabel = context.getString(CalendarR.string.calendar_search_label)
        val searchMatcher = hasText(searchLabel).and(hasSetTextAction())
        calendarList.performScrollToNode(searchMatcher)
        val search = onNode(searchMatcher)
        search.assertIsDisplayed()
        assertContained(search, profile, "Calendar search")

        val requiredLabel = context.getString(CalendarR.string.calendar_label_filter_required)
        val requiredMatcher = hasText(requiredLabel).and(hasClickAction())
        calendarList.performScrollToNode(requiredMatcher)
        val requiredFilter = onNode(requiredMatcher)
        requiredFilter.assertIsSelected().assertHasClickAction()
        assertTouchTarget(requiredFilter, "Calendar required filter")
        assertContained(requiredFilter, profile, "Calendar required filter")

        val calendarListBounds = calendarList.getUnclippedBoundsInRoot()
        assertWithMessage("Calendar list viewport height")
            .that(calendarListBounds.bottom.value - calendarListBounds.top.value)
            .isGreaterThan(0f)
        calendarList.performScrollToNode(hasText("Ash Wednesday"))
        val ashWednesday = onLiteralText("Ash Wednesday")
        ashWednesday.assertIsDisplayed()
        assertContained(ashWednesday, profile, "Ash Wednesday")
    }

    private fun verifyTracker(profile: ResponsiveProfile) {
        val title = onHeading(TrackerR.string.tracker_title)
        title.assertIsDisplayed()
        assertContained(title, profile, "Tracker title")
        val fastInProgress = onText(TrackerR.string.tracker_fast_in_progress)
        fastInProgress.performScrollTo().assertIsDisplayed()
        assertContained(fastInProgress, profile, "Tracker fast in progress")

        val progress = onNodeWithTag(TRACKER_PROGRESS_TEST_TAG)
        progress.performScrollTo().assertIsDisplayed()
        progress.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(0.0625f, 0f..1f),
            ),
        )
        progress.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                context.getString(TrackerR.string.tracker_progress_accessibility, "15h 0m", 16, "1h 0m"),
            ),
        )
        assertThat(progress.fetchSemanticsNode().config.contains(SemanticsProperties.LiveRegion)).isFalse()
        assertContained(progress, profile, "Tracker progress")

        onAllNodesWithText(context.getString(TrackerR.string.tracker_hours_short_value, 15)).assertCountEquals(0)
        onAllNodesWithText(context.getString(TrackerR.string.tracker_minutes_short_value, 0)).assertCountEquals(0)

        val elapsed = onText(TrackerR.string.tracker_elapsed_value, "1h 0m")
        elapsed.performScrollTo().assertIsDisplayed()
        assertContained(elapsed, profile, "Tracker elapsed summary")

        val endFast = onNodeWithTag(TRACKER_END_FAST_TEST_TAG)
        endFast.performScrollTo().assertIsDisplayed().assertHasClickAction()
        assertTouchTarget(endFast, "Tracker end fast")
        assertContained(endFast, profile, "Tracker end fast")

        val recoveryText = context.getString(TrackerR.string.tracker_preparation_recovery)
        onNodeWithTag(TRACKER_LIST_TEST_TAG).performScrollToNode(hasText(recoveryText))
        val recovery = onLiteralText(recoveryText)
        recovery.assertIsDisplayed()
        assertContained(recovery, profile, "Tracker recovery")
    }

    private fun verifyReminderCenter(profile: ResponsiveProfile) {
        val section =
            onNode(
                hasText(context.getString(com.kevpierce.catholicfastingapp.R.string.more_setup_reminders))
                    .and(hasClickAction()),
            )
        section
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsSelected()
            .assertHasClickAction()
        assertTouchTarget(section, "More setup chip")
        assertContained(section, profile, "More setup chip")

        val reminderCenter = onText(com.kevpierce.catholicfastingapp.R.string.more_reminder_center_title)
        reminderCenter.performScrollTo().assertIsDisplayed()
        assertContained(reminderCenter, profile, "Reminder center")

        val balanced = onNodeWithTag("reminder-tier-${ReminderTier.BALANCED.name}")
        balanced
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsSelected()
            .assertHasClickAction()
        assertTouchTarget(balanced, "Balanced reminder chip")
        assertContained(balanced, profile, "Balanced reminder chip")

        val activeFastSync = onText(com.kevpierce.catholicfastingapp.R.string.more_active_fast_sync)
        activeFastSync.performScrollTo().assertIsDisplayed()
        assertContained(activeFastSync, profile, "Active fast sync")
    }

    private fun verifyPremium(profile: ResponsiveProfile) {
        val section =
            onNode(
                hasText(context.getString(com.kevpierce.catholicfastingapp.R.string.more_support_premium))
                    .and(hasClickAction()),
            )
        section
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsSelected()
            .assertHasClickAction()
        assertTouchTarget(section, "More premium chip")
        assertContained(section, profile, "More premium chip")

        val premiumList = onNodeWithTag(PREMIUM_LIST_TEST_TAG)
        val journeyText = context.getString(PremiumR.string.premium_guided_journey_title)
        premiumList.performScrollToNode(hasText(journeyText))
        val journey = onLiteralText(journeyText)
        journey.assertIsDisplayed()
        assertContained(journey, profile, "Guided journey")

        val inactiveText = context.getString(PremiumR.string.premium_inactive)
        premiumList.performScrollToNode(hasText(inactiveText))
        val inactive = onLiteralText(inactiveText)
        inactive.assertIsDisplayed()
        assertContained(inactive, profile, "Premium inactive")

        val reflectionText = context.getString(PremiumR.string.premium_reflection_journal_title)
        premiumList.performScrollToNode(hasText(reflectionText))
        val reflection = onLiteralText(reflectionText)
        reflection.assertIsDisplayed()
        assertContained(reflection, profile, "Reflection journal")
    }

    private fun verifyPrivacy(profile: ResponsiveProfile) {
        val section =
            onNode(
                hasText(context.getString(com.kevpierce.catholicfastingapp.R.string.more_privacy_data))
                    .and(hasClickAction()),
            )
        section
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsSelected()
            .assertHasClickAction()
        assertTouchTarget(section, "More privacy chip")
        assertContained(section, profile, "More privacy chip")

        val stored = onText(com.kevpierce.catholicfastingapp.R.string.more_data_stored_title)
        stored.performScrollTo().assertIsDisplayed()
        assertContained(stored, profile, "Data stored")

        val localState = onText(com.kevpierce.catholicfastingapp.R.string.more_current_local_state_title)
        localState.performScrollTo().assertIsDisplayed()
        assertContained(localState, profile, "Current local state")
    }

    private fun verifyStressRoute(capture: StressCapture) {
        when (capture.route) {
            AppDeepLinks.TODAY -> {
                val node = onText(com.kevpierce.catholicfasting.feature.today.R.string.today_companion_title)
                node.performScrollTo().assertIsDisplayed()
                assertContained(node, capture.profile, "Stress Today companion")
            }
            AppDeepLinks.TRACKER -> {
                val node = onNodeWithTag(TRACKER_PROGRESS_TEST_TAG)
                node.performScrollTo().assertIsDisplayed()
                assertContained(node, capture.profile, "Stress Tracker progress")
            }
            AppDeepLinks.MORE_SETUP -> {
                val node = onText(com.kevpierce.catholicfastingapp.R.string.more_reminder_center_title)
                node.performScrollTo().assertIsDisplayed()
                assertContained(node, capture.profile, "Stress Reminder center")
            }
            AppDeepLinks.MORE_PREMIUM -> {
                val node = onText(PremiumR.string.premium_guided_journey_title)
                node.performScrollTo().assertIsDisplayed()
                assertContained(node, capture.profile, "Stress Premium")
            }
            AppDeepLinks.CALENDAR -> {
                val list = onNodeWithTag(CALENDAR_LIST_TEST_TAG)
                val listBounds = list.getUnclippedBoundsInRoot()
                assertWithMessage("Stress Calendar list viewport height")
                    .that(listBounds.bottom.value - listBounds.top.value)
                    .isGreaterThan(0f)
                list.performScrollToNode(hasText("Ash Wednesday"))
                val node = onLiteralText("Ash Wednesday")
                node.assertIsDisplayed()
                assertContained(node, capture.profile, "Stress Fasting Days")
            }
            AppDeepLinks.MORE_PRIVACY -> {
                val node = onText(com.kevpierce.catholicfastingapp.R.string.more_current_local_state_title)
                node.performScrollTo().assertIsDisplayed()
                assertContained(node, capture.profile, "Stress Privacy")
            }
        }
        assertBottomNavigation(
            when (capture.route) {
                AppDeepLinks.TODAY -> Destination.TODAY
                AppDeepLinks.CALENDAR -> Destination.FASTING_DAYS
                AppDeepLinks.TRACKER -> Destination.TRACK_FAST
                else -> Destination.MORE
            },
        )
    }

    private fun setContentForProfile(
        profile: ResponsiveProfile,
        darkMode: Boolean,
        initialRoute: String,
    ): MutableState<String> {
        val deepLink = mutableStateOf(initialRoute)
        composeRule.setContent {
            DeviceConfigurationOverride(profile.configuration(darkMode)) {
                val route by deepLink
                CatholicFastingTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Box(
                            modifier = Modifier.fillMaxSize().testTag(VIEWPORT_TEST_TAG),
                        ) {
                            key(route) {
                                CatholicFastingApp(
                                    initialDeepLink = route,
                                    clock = ReleaseTestFixture.fixedClock,
                                )
                            }
                        }
                    }
                }
            }
        }
        return deepLink
    }

    private fun waitForRoute(route: String) {
        val title =
            when (route) {
                AppDeepLinks.TODAY -> context.getString(com.kevpierce.catholicfasting.feature.today.R.string.today_title)
                AppDeepLinks.CALENDAR -> context.getString(CalendarR.string.calendar_title)
                AppDeepLinks.TRACKER -> context.getString(TrackerR.string.tracker_title)
                AppDeepLinks.MORE_SETUP -> context.getString(com.kevpierce.catholicfastingapp.R.string.more_setup_reminders)
                AppDeepLinks.MORE_PREMIUM -> context.getString(com.kevpierce.catholicfastingapp.R.string.more_support_premium)
                AppDeepLinks.MORE_PRIVACY -> context.getString(com.kevpierce.catholicfastingapp.R.string.more_privacy_data)
                else -> error("Unsupported responsive route: $route")
            }
        composeRule.waitUntil(timeoutMillis = 10_000L) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun assertViewport(profile: ResponsiveProfile) {
        val viewport = onNodeWithTag(VIEWPORT_TEST_TAG)
        val bounds = viewport.getUnclippedBoundsInRoot()
        val width = bounds.right.value - bounds.left.value
        val height = bounds.bottom.value - bounds.top.value
        assertWithMessage("viewport left").that(bounds.left.value).isWithin(0.5f).of(0f)
        assertWithMessage("viewport top").that(bounds.top.value).isWithin(0.5f).of(0f)
        assertWithMessage("viewport width").that(width).isWithin(0.5f).of(profile.widthDp.toFloat())
        assertWithMessage("viewport height").that(height).isWithin(0.5f).of(profile.heightDp.toFloat())
    }

    private fun assertBottomNavigation(destination: Destination) {
        val navigation =
            listOf(
                Destination.TODAY to com.kevpierce.catholicfastingapp.R.string.nav_today,
                Destination.FASTING_DAYS to com.kevpierce.catholicfastingapp.R.string.nav_fasting_days,
                Destination.TRACK_FAST to com.kevpierce.catholicfastingapp.R.string.nav_track_fast,
                Destination.MORE to com.kevpierce.catholicfastingapp.R.string.nav_more,
            )
        navigation.forEach { (itemDestination, labelRes) ->
            val matcher =
                hasText(context.getString(labelRes))
                    .and(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
                    .and(hasClickAction())
            composeRule.onAllNodes(matcher).assertCountEquals(1)
            val item = composeRule.onNode(matcher)
            item.assertIsDisplayed().assertHasClickAction()
            if (itemDestination == destination) {
                item.assertIsSelected()
            } else {
                item.assertIsNotSelected()
            }
            assertTouchTarget(item, "Bottom navigation ${context.getString(labelRes)}")
        }
    }

    private fun assertContained(
        interaction: androidx.compose.ui.test.SemanticsNodeInteraction,
        profile: ResponsiveProfile,
        label: String,
    ) {
        val bounds = interaction.getUnclippedBoundsInRoot()
        assertWithMessage("$label left").that(bounds.left.value).isAtLeast(-0.5f)
        assertWithMessage("$label top").that(bounds.top.value).isAtLeast(-0.5f)
        assertWithMessage("$label right").that(bounds.right.value).isAtMost(profile.widthDp + 0.5f)
        assertWithMessage("$label bottom").that(bounds.bottom.value).isAtMost(profile.heightDp + 0.5f)
    }

    private fun assertTouchTarget(
        interaction: androidx.compose.ui.test.SemanticsNodeInteraction,
        label: String,
    ) {
        val touchBounds = interaction.fetchSemanticsNode().touchBoundsInRoot
        val viewport = onNodeWithTag(VIEWPORT_TEST_TAG)
        val viewportBoundsDp = viewport.getUnclippedBoundsInRoot()
        val viewportWidthDp = viewportBoundsDp.right.value - viewportBoundsDp.left.value
        val viewportWidthPx = viewport.fetchSemanticsNode().boundsInRoot.width
        val density = viewportWidthPx / viewportWidthDp
        val minimumWithRoundingTolerance = 48f - 0.01f
        assertWithMessage("$label width").that(touchBounds.width / density).isAtLeast(minimumWithRoundingTolerance)
        assertWithMessage("$label height").that(touchBounds.height / density).isAtLeast(minimumWithRoundingTolerance)
    }

    private fun onText(
        resourceId: Int,
        vararg formatArgs: Any,
    ): androidx.compose.ui.test.SemanticsNodeInteraction =
        composeRule.onNodeWithText(context.getString(resourceId, *formatArgs), ignoreCase = true)

    private fun onHeading(resourceId: Int): androidx.compose.ui.test.SemanticsNodeInteraction =
        composeRule.onNode(
            hasText(context.getString(resourceId), ignoreCase = true)
                .and(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)),
        )

    private fun onLiteralText(text: String): androidx.compose.ui.test.SemanticsNodeInteraction = composeRule.onNodeWithText(text)

    private fun onNodeWithTag(tag: String): androidx.compose.ui.test.SemanticsNodeInteraction = composeRule.onNodeWithTag(tag)

    private fun onAllNodesWithText(text: String): androidx.compose.ui.test.SemanticsNodeInteractionCollection =
        composeRule
            .onAllNodesWithText(text)

    private fun onNode(matcher: SemanticsMatcher): androidx.compose.ui.test.SemanticsNodeInteraction = composeRule.onNode(matcher)

    private fun captureToDevice(fileName: String) {
        val remotePath = "$REMOTE_EVIDENCE_DIR/$fileName"
        executeShellCommand("mkdir -p $REMOTE_EVIDENCE_DIR")
        executeShellCommand("screencap -p $remotePath")
        val size = executeShellCommand("stat -c %s $remotePath").trim().toLong()
        assertThat(size).isGreaterThan(0L)
    }

    private fun executeShellCommand(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return descriptor.use { FileInputStream(it.fileDescriptor).bufferedReader().readText() }
    }

    private data class ResponsiveProfile(
        val widthDp: Int,
        val heightDp: Int,
        val fontScale: Float,
    ) {
        fun configuration(darkMode: Boolean): DeviceConfigurationOverride =
            with(DeviceConfigurationOverride.Companion) {
                ForcedSize(DpSize(widthDp.dp, heightDp.dp))
                    .then(FontScale(fontScale))
                    .then(DarkMode(darkMode))
            }
    }

    private data class RouteCase(
        val deepLink: String,
        val destination: Destination,
    )

    private data class StressCapture(
        val profile: ResponsiveProfile,
        val darkMode: Boolean,
        val route: String,
        val fileName: String,
    )

    private enum class Destination {
        TODAY,
        FASTING_DAYS,
        TRACK_FAST,
        MORE,
    }

    private companion object {
        const val VIEWPORT_TEST_TAG = "responsive-test-viewport"
        const val REMOTE_EVIDENCE_DIR = "/sdcard/cfa-responsive-evidence"
    }
}
