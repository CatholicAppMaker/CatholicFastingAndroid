@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfastingapp

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.billing.BillingOfferUi
import com.kevpierce.catholicfasting.core.billing.BillingState
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.ContentLocale
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.IntermittentFastSessionRecap
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.model.RuleSettings
import com.kevpierce.catholicfasting.core.rules.CompanionSnapshotEngine
import com.kevpierce.catholicfasting.core.rules.ObservanceCalculator
import com.kevpierce.catholicfasting.core.rules.PremiumFastPrepGuidanceEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSeasonProgramEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshotEngine
import com.kevpierce.catholicfasting.core.rules.SacredImageryCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentPackCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentSupport
import com.kevpierce.catholicfasting.core.ui.CatholicFastingTheme
import com.kevpierce.catholicfasting.feature.calendar.CalendarScreen
import com.kevpierce.catholicfasting.feature.guidance.GuidanceScreen
import com.kevpierce.catholicfasting.feature.premium.PremiumScreen
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceActions
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceUiState
import com.kevpierce.catholicfasting.feature.settings.SettingsScreen
import com.kevpierce.catholicfasting.feature.today.TODAY_COMPANION_ACTION_TEST_TAG_PREFIX
import com.kevpierce.catholicfasting.feature.today.TodayScreen
import com.kevpierce.catholicfasting.feature.today.TodayUiState
import com.kevpierce.catholicfasting.feature.tracker.TRACKER_END_FAST_TEST_TAG
import com.kevpierce.catholicfasting.feature.tracker.TRACKER_INTENTION_TEST_TAG_PREFIX
import com.kevpierce.catholicfasting.feature.tracker.TRACKER_REVIEW_NOTE_TEST_TAG
import com.kevpierce.catholicfasting.feature.tracker.TRACKER_START_FAST_TEST_TAG
import com.kevpierce.catholicfasting.feature.tracker.TrackerActions
import com.kevpierce.catholicfasting.feature.tracker.TrackerScreen
import com.kevpierce.catholicfasting.feature.tracker.TrackerUiState
import com.kevpierce.catholicfastingapp.ui.CatholicFastingApp
import com.kevpierce.catholicfastingapp.ui.INTERMITTENT_INTENTION_CHIP_TEST_TAG_PREFIX
import com.kevpierce.catholicfastingapp.ui.REGION_CHIP_TEST_TAG_PREFIX
import com.kevpierce.catholicfastingapp.ui.REMINDER_TIER_CHIP_TEST_TAG_PREFIX
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import com.kevpierce.catholicfasting.feature.calendar.R as CalendarR
import com.kevpierce.catholicfasting.feature.guidance.R as GuidanceR
import com.kevpierce.catholicfasting.feature.premium.R as PremiumR
import com.kevpierce.catholicfasting.feature.settings.R as SettingsR
import com.kevpierce.catholicfasting.feature.today.R as TodayR
import com.kevpierce.catholicfasting.feature.tracker.R as TrackerR

@RunWith(AndroidJUnit4::class)
class ExpandedReleaseUiInstrumentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppContainer.resetForTesting(context)
    }

    @Test
    fun appShellTodayDeepLinkShowsTodayTab() {
        setAppShell(AppDeepLinks.TODAY)

        assertText(context.getString(TodayR.string.today_title))
    }

    @Test
    fun appShellCalendarDeepLinkShowsFastingDaysTab() {
        setAppShell(AppDeepLinks.CALENDAR)

        assertText(context.getString(CalendarR.string.calendar_title))
    }

    @Test
    fun appShellCalendarFridayNoteDeepLinkShowsCalendarSearch() {
        setAppShell(AppDeepLinks.CALENDAR_FRIDAY_NOTE)

        assertText(context.getString(CalendarR.string.calendar_search_label))
    }

    @Test
    fun appShellTrackerDeepLinkShowsTrackFastTab() {
        setAppShell(AppDeepLinks.TRACKER)

        assertText(context.getString(TrackerR.string.tracker_title))
    }

    @Test
    fun appShellPremiumDeepLinkShowsPremiumSection() {
        setAppShell(AppDeepLinks.MORE_PREMIUM)

        assertText(context.getString(R.string.more_support_premium))
    }

    @Test
    fun appShellSetupDeepLinkShowsReminderCenter() {
        setAppShell(AppDeepLinks.MORE_SETUP)

        assertText(context.getString(R.string.more_reminder_center_title))
    }

    @Test
    fun appShellHistoryDeepLinkShowsTimeline() {
        setAppShell(AppDeepLinks.MORE_HISTORY)

        assertText(context.getString(R.string.history_timeline_section))
    }

    @Test
    fun appShellPrivacyDeepLinkShowsDataTools() {
        setAppShell(AppDeepLinks.MORE_PRIVACY)

        assertText(context.getString(R.string.more_data_stored_title))
    }

    @Test
    fun appShellUnknownDeepLinkFallsBackToToday() {
        setAppShell("catholicfasting://open/unknown")

        assertText(context.getString(TodayR.string.today_title))
    }

    @Test
    fun appShellMoreHubShowsAllDestinations() {
        setAppShell(AppDeepLinks.MORE_PREMIUM)

        assertText(context.getString(R.string.more_setup_reminders))
        assertText(context.getString(R.string.more_profile_norms))
        assertText(context.getString(R.string.more_guidance_rules))
        assertText(context.getString(R.string.more_history_fasting))
        assertText(context.getString(R.string.more_privacy_data))
    }

    @Test
    fun appShellSetupShowsProgressAndReminderStrategy() {
        setAppShell(AppDeepLinks.MORE_SETUP)

        assertText(context.getString(R.string.more_setup_progress_title))
        assertText(context.getString(R.string.more_reminder_strategy_body))
    }

    @Test
    fun appShellPrivacyShowsNoTrackingCopy() {
        setAppShell(AppDeepLinks.MORE_PRIVACY)
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(R.string.more_data_no_tracking))
    }

    @Test
    fun appShellHistoryShowsFormationReference() {
        setAppShell(AppDeepLinks.MORE_HISTORY)

        assertText(context.getString(R.string.history_overview_eyebrow))
    }

    @Test
    fun appShellCompletedOnboardingHidesOnboardingTitle() {
        setAppShell(AppDeepLinks.TODAY)

        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_title)).assertCountEquals(0)
    }

    @Test
    fun appShellMoreSetupUsesCompletedSeedState() {
        seedCompletedOnboarding()

        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp(initialDeepLink = AppDeepLinks.MORE_SETUP)
            }
        }

        assertText(context.getString(R.string.more_notice_acknowledged))
    }

    @Test
    fun onboardingRouteShowsOnlyTheCurrentRequiredStep() {
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp()
            }
        }

        assertText(context.getString(R.string.onboarding_notice_title))
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_profile_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_reminders_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_intention_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_premium_title)).assertCountEquals(0)
    }

    @Test
    fun onboardingDefaultRouteShowsBlockedFinishCopy() {
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp()
            }
        }

        assertText(context.getString(R.string.onboarding_finish_blocked))
    }

    @Test
    fun onboardingRouteAdvancesThroughRegionReminderIntentionAndPayoff() {
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp()
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.onboarding_notice_accept))
            .performScrollTo()
            .performClick()
        assertText(context.getString(R.string.label_region_us))
        assertText(context.getString(R.string.label_region_canada))
        assertText(context.getString(R.string.label_region_other))
        composeRule.onAllNodesWithText(context.getString(R.string.label_reminder_minimal)).assertCountEquals(0)

        clickTaggedNodeAfterScrolling(REGION_CHIP_TEST_TAG_PREFIX + RegionProfile.US.name)
        assertText(context.getString(R.string.onboarding_reminders_title))
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_intention_title)).assertCountEquals(0)

        clickTaggedNodeAfterScrolling(REMINDER_TIER_CHIP_TEST_TAG_PREFIX + ReminderTier.MINIMAL.name)
        assertText(context.getString(R.string.onboarding_intention_title))
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_premium_title)).assertCountEquals(0)

        clickTaggedNodeAfterScrolling(
            INTERMITTENT_INTENTION_CHIP_TEST_TAG_PREFIX + IntermittentFastIntention.PRAYER.name,
        )
        assertText(context.getString(R.string.onboarding_premium_title))
        composeRule
            .onNodeWithText(context.getString(R.string.onboarding_finish))
            .assertIsEnabled()
    }

    @Test
    fun onboardingDefaultRouteStartsWithTrustNotice() {
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp()
            }
        }

        assertText(context.getString(R.string.onboarding_notice_title))
        composeRule.onAllNodesWithText(context.getString(R.string.label_reminder_minimal)).assertCountEquals(0)
    }

    @Test
    fun freshInstallCompletesReminderAndIntentionThenLandsOnTodayCompanion() {
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp()
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.onboarding_notice_accept))
            .performScrollTo()
            .performClick()
        clickTaggedNodeAfterScrolling(REGION_CHIP_TEST_TAG_PREFIX + RegionProfile.US.name)
        clickTaggedNodeAfterScrolling(REMINDER_TIER_CHIP_TEST_TAG_PREFIX + ReminderTier.MINIMAL.name)
        clickTaggedNodeAfterScrolling(
            INTERMITTENT_INTENTION_CHIP_TEST_TAG_PREFIX + IntermittentFastIntention.PRAYER.name,
        )
        composeRule.waitForIdle()
        val setupState = AppContainer.repository.dashboardState.value.launchFunnelSnapshot
        assertThat(setupState.independentAppNoticeAcknowledged).isTrue()
        assertThat(setupState.regionSelected).isTrue()
        assertThat(setupState.selectedReminderTier).isEqualTo(ReminderTier.MINIMAL)
        assertThat(setupState.reminderTierSelected).isTrue()
        assertThat(setupState.intermittentIntentionSelected).isTrue()
        assertThat(setupState.selectedIntermittentIntentionId).isEqualTo(IntermittentFastIntention.PRAYER.name)
        composeRule
            .onNodeWithText(context.getString(R.string.onboarding_finish))
            .assertIsEnabled()
            .performScrollTo()
            .performClick()
        composeRule.waitUntil {
            composeRule.onAllNodesWithText(context.getString(TodayR.string.today_title)).fetchSemanticsNodes().isNotEmpty()
        }

        assertText(context.getString(TodayR.string.today_title))
        assertText(context.getString(TodayR.string.today_companion_title))
    }

    @Test
    fun todayDirectScreenShowsHeroAndYearPlan() {
        setTodayScreen()

        assertText(context.getString(TodayR.string.today_title))
        assertText(context.getString(TodayR.string.today_year_plan_title))
    }

    @Test
    fun todayDirectScreenShowsPersonalInsights() {
        setTodayScreen()

        assertText(context.getString(TodayR.string.today_personal_insights_title))
    }

    @Test
    fun todayDirectScreenShowsDevotionalGallery() {
        setTodayScreen()
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(TodayR.string.today_devotional_gallery_title))
    }

    @Test
    fun todayDirectScreenShowsIndependentNotice() {
        setTodayScreen()
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(TodayR.string.today_important_notice_title))
    }

    @Test
    fun todayCompanionActiveFastActionRoutesFromAppShellToTrackFast() {
        seedCompletedOnboarding()
        AppContainer.repository.startIntermittentFastWithIntention(
            intentionId = IntermittentFastIntention.PENANCE.name,
            now = Instant.now().minusSeconds(3600),
        )
        AppContainer.repository.flushForTesting()
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp(initialDeepLink = AppDeepLinks.TODAY)
            }
        }

        clickFirstTaggedNodeAfterScrolling(
            TODAY_COMPANION_ACTION_TEST_TAG_PREFIX + "active-fast",
            TODAY_COMPANION_ACTION_TEST_TAG_PREFIX + "track-fast",
        )

        assertText(context.getString(TrackerR.string.tracker_title))
        assertText(context.getString(TrackerR.string.tracker_end_fast))
    }

    @Test
    fun calendarDirectScreenShowsRequiredFilterAndSearch() {
        setCalendarScreen()

        assertText(context.getString(CalendarR.string.calendar_title))
        assertText(context.getString(CalendarR.string.calendar_search_label))
    }

    @Test
    fun calendarDirectScreenShowsProgressOverview() {
        setCalendarScreen()

        assertText(context.getString(CalendarR.string.calendar_progress_overview))
    }

    @Test
    fun calendarDirectScreenShowsAshWednesdayCard() {
        setCalendarScreen()
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText("Ash Wednesday")
    }

    @Test
    fun calendarDirectScreenShowsCompletionChips() {
        setCalendarScreen()
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(CompletionStatus.COMPLETED.label)
    }

    @Test
    fun trackerDirectScreenShowsInactiveFastControls() {
        setTrackerScreen(activeFast = null)

        assertText(context.getString(TrackerR.string.tracker_no_active_fast))
        assertText(context.getString(TrackerR.string.tracker_start_fast))
    }

    @Test
    fun trackerDirectScreenShowsActiveFastControls() {
        setTrackerScreen(
            activeFast =
                ActiveIntermittentFast(
                    startIso = Instant.now().minusSeconds(3600).toString(),
                    targetHours = 16,
                ),
        )

        assertText(context.getString(TrackerR.string.tracker_fast_in_progress))
        assertText(context.getString(TrackerR.string.tracker_end_fast))
    }

    @Test
    fun trackerDirectScreenStartsFastWithSelectedIntention() {
        var persistedIntentionId: String? = null
        var startedIntentionId: String? = null

        setTrackerScreen(
            activeFast = null,
            actions =
                trackerActions(
                    onIntentionChange = { persistedIntentionId = it },
                    onStartFast = { startedIntentionId = it },
                ),
        )

        composeRule
            .onNodeWithTag(TRACKER_INTENTION_TEST_TAG_PREFIX + IntermittentFastIntention.PRAYER.name)
            .performClick()
        composeRule
            .onNodeWithTag(TRACKER_START_FAST_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertThat(persistedIntentionId).isEqualTo(IntermittentFastIntention.PRAYER.name)
            assertThat(startedIntentionId).isEqualTo(IntermittentFastIntention.PRAYER.name)
        }
    }

    @Test
    fun trackerDirectScreenSavesReviewNoteWhenEndingActiveFast() {
        var endedReviewNote: String? = null
        setTrackerScreen(
            activeFast =
                ActiveIntermittentFast(
                    startIso = Instant.now().minusSeconds(18 * 60 * 60).toString(),
                    targetHours = 16,
                    intentionId = IntermittentFastIntention.PENANCE.name,
                ),
            actions = trackerActions(onEndFast = { endedReviewNote = it }),
        )

        composeRule
            .onNodeWithTag(TRACKER_REVIEW_NOTE_TEST_TAG)
            .performTextInput("Kept the Friday fast prayerfully.")
        composeRule
            .onNodeWithTag(TRACKER_END_FAST_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertThat(endedReviewNote).isEqualTo("Kept the Friday fast prayerfully.")
        }
    }

    @Test
    fun trackerDirectScreenShowsLatestRecapIntentionAndReviewNote() {
        setTrackerScreen(
            activeFast = null,
            latestRecap =
                IntermittentFastSessionRecap(
                    durationHours = 18.0,
                    targetHours = 16,
                    completedTarget = true,
                    title = "Target reached",
                    encouragement = "You completed the fast with steadiness.",
                    suggestedNextAction = "Recover gently.",
                    intentionId = IntermittentFastIntention.PENANCE.name,
                    reviewNote = "Kept the Friday fast prayerfully.",
                ),
        )

        assertText(context.getString(TrackerR.string.tracker_latest_recap_title))
        assertText(context.getString(TrackerR.string.tracker_intention_value, IntermittentFastIntention.PENANCE.label))
        assertText(context.getString(TrackerR.string.tracker_review_note_value, "Kept the Friday fast prayerfully."))
    }

    @Test
    fun trackerDirectScreenShowsSchedulePlanner() {
        setTrackerScreen(activeFast = null)
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(TrackerR.string.tracker_custom_schedules))
    }

    @Test
    fun trackerDirectScreenShowsRecentSessionsWhenSeeded() {
        setTrackerScreen(
            activeFast = null,
            sessions =
                listOf(
                    IntermittentFastSession(
                        id = "session",
                        startIso = "2026-03-13T00:00:00Z",
                        endIso = "2026-03-13T17:00:00Z",
                        targetHours = 16,
                        completedTarget = true,
                    ),
                ),
        )
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(TrackerR.string.tracker_recent_sessions))
    }

    @Test
    fun trackerDirectScreenShowsPrepGuidance() {
        setTrackerScreen(activeFast = null)
        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(TrackerR.string.tracker_preparation_recovery))
    }

    @Test
    fun guidanceDirectScreenShowsFoodGuidance() {
        setGuidanceScreen()

        assertText(context.getString(GuidanceR.string.guidance_food_title))
    }

    @Test
    fun guidanceDirectScreenShowsRuleAudit() {
        setGuidanceScreen()

        assertText(context.getString(GuidanceR.string.guidance_rule_audit_title))
    }

    @Test
    fun guidanceDirectScreenShowsSymbolGallery() {
        setGuidanceScreen()
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(GuidanceR.string.guidance_symbol_gallery_title))
    }

    @Test
    fun settingsDirectScreenShowsRegionCalendarAndFridayMode() {
        setSettingsScreen()

        assertText(context.getString(SettingsR.string.settings_region))
        assertText(context.getString(SettingsR.string.settings_calendar))
        assertText(context.getString(SettingsR.string.settings_friday_mode))
    }

    @Test
    fun settingsDirectScreenShowsAgeAndDispensationControls() {
        setSettingsScreen()

        assertText(context.getString(SettingsR.string.settings_age_14))
        assertText(context.getString(SettingsR.string.settings_age_18))
        assertText(context.getString(SettingsR.string.settings_medical_dispensation))
    }

    @Test
    fun settingsDirectScreenShowsCanadaSelectionWhenSeeded() {
        setSettingsScreen(RuleSettings(regionProfile = RegionProfile.CANADA))

        assertText(context.getString(R.string.label_region_canada))
    }

    @Test
    fun premiumLockedScreenShowsPlansAndLegalSupport() {
        setPremiumScreen(unlocked = false)

        assertText(context.getString(PremiumR.string.premium_inactive))
        assertText(context.getString(PremiumR.string.premium_subscriptions_title))
    }

    @Test
    fun premiumUnlockedScreenShowsActiveStatus() {
        setPremiumScreen(unlocked = true)

        assertText(context.getString(PremiumR.string.premium_active))
    }

    @Test
    fun premiumScreenShowsYearlyBeforeMonthlyOffers() {
        setPremiumScreen(unlocked = false)

        assertText("Premium Yearly")
        assertText("Premium Monthly")
    }

    @Test
    fun premiumScreenHidesSupportTipSectionWhenNoTipProductsShip() {
        setPremiumScreen(unlocked = false, includeTipOffers = false)

        composeRule.onAllNodesWithText(context.getString(PremiumR.string.premium_support_tips_title)).assertCountEquals(0)
    }

    @Test
    fun premiumScreenShowsPlanningSummary() {
        setPremiumScreen(unlocked = true, includeCatalogOffers = false)

        assertText(context.getString(PremiumR.string.premium_planning_export_title))
    }

    @Test
    fun premiumScreenShowsAnalyticsRecovery() {
        setPremiumScreen(unlocked = true)
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(PremiumR.string.premium_analytics_recovery_title))
    }

    @Test
    fun premiumScreenShowsReflectionJournal() {
        setPremiumScreen(unlocked = true)
        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onRoot().performTouchInput { swipeUp() }

        assertText(context.getString(PremiumR.string.premium_reflection_journal_title))
    }

    @Test
    fun premiumScreenShowsSavedReflectionCountWhenSeeded() {
        setPremiumScreen(
            unlocked = true,
            includeCatalogOffers = false,
            reflections =
                listOf(
                    ReflectionJournalEntry(
                        id = "reflection",
                        createdAtIso = "2026-03-13T00:00:00Z",
                        title = "Quiet Friday",
                        body = "Prayerful note",
                    ),
                ),
        )

        assertText(context.getString(PremiumR.string.premium_saved_reflections_value, 1))
    }

    private fun setAppShell(deepLink: String) {
        seedCompletedOnboarding()
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp(initialDeepLink = deepLink)
            }
        }
    }

    private fun setTodayScreen() {
        val state = AppContainer.repository.dashboardState.value
        val premiumSnapshot = premiumSnapshot()
        val seasonalPack = SeasonalContentPackCatalog.pack(premiumSnapshot.season, ContentLocale.ENGLISH)
        composeRule.setContent {
            CatholicFastingTheme {
                TodayScreen(
                    uiState =
                        TodayUiState(
                            todayObservance = state.observances.firstOrNull(),
                            companionSnapshot =
                                CompanionSnapshotEngine.build(
                                    observances = state.observances,
                                    statusesById = state.statusesById,
                                    sessions = state.intermittentSessions,
                                    activeFast = state.activeIntermittentFast,
                                    settings = state.settings,
                                    premiumSnapshot = premiumSnapshot,
                                    premiumUnlocked = false,
                                ),
                            completionSummary = context.resources.getQuantityString(R.plurals.summary_completion_value, 0, 0),
                            premiumSnapshot = premiumSnapshot,
                            seasonalContentPack = seasonalPack,
                            dailyFormationLine = SeasonalContentSupport.dailyFormationLine(seasonalPack, LocalDate.now()),
                            dailyQuote = SeasonalContentSupport.dailyQuote(premiumSnapshot.season, seasonalPack, LocalDate.now()),
                            devotionalGallery = SacredImageryCatalog.fastingGallery.take(3),
                            setupProgressSummary = "Setup progress",
                            yearPlanSummary = "Year plan",
                            weeklyRecap = "Weekly recap",
                            streakMessage = "Streak summary",
                            noticeSummary = context.getString(R.string.notice_independent_app_summary),
                        ),
                )
            }
        }
    }

    private fun setCalendarScreen() {
        val state = AppContainer.repository.dashboardState.value
        composeRule.setContent {
            CatholicFastingTheme {
                CalendarScreen(
                    observances = state.observances,
                    statusesById = state.statusesById,
                    fridayNotesById = state.fridayNotesById,
                    premiumSnapshot = premiumSnapshot(),
                    onStatusChange = { _, _ -> },
                    onFridayNoteChange = { _, _ -> },
                )
            }
        }
    }

    private fun setTrackerScreen(
        activeFast: ActiveIntermittentFast?,
        sessions: List<IntermittentFastSession> = emptyList(),
        latestRecap: IntermittentFastSessionRecap? = null,
        actions: TrackerActions = trackerActions(),
    ) {
        val state = AppContainer.repository.dashboardState.value
        composeRule.setContent {
            CatholicFastingTheme {
                TrackerScreen(
                    uiState =
                        TrackerUiState(
                            schedules = state.schedules,
                            activeScheduleId = state.activeIntermittentScheduleId,
                            sessions = sessions,
                            activeFast = activeFast,
                            presetHours = state.intermittentPresetHours,
                            selectedIntentionId = state.launchFunnelSnapshot.selectedIntermittentIntentionId,
                            latestRecap = latestRecap,
                            premiumSnapshot = premiumSnapshot(),
                            prepGuidance =
                                PremiumFastPrepGuidanceEngine.prepAndRefeed(
                                    targetHours = state.intermittentPresetHours,
                                    hasMedicalDispensation = false,
                                ),
                            seasonProgramActions =
                                PremiumSeasonProgramEngine.actions(
                                    program = state.premiumCompanionState.seasonProgram,
                                    week = 1,
                                ),
                        ),
                    actions = actions,
                )
            }
        }
    }

    private fun trackerActions(
        onPresetHoursChange: (Int) -> Unit = {},
        onIntentionChange: (String) -> Unit = {},
        onStartFast: (String) -> Unit = {},
        onEndFast: (String?) -> Unit = {},
        onCancelFast: () -> Unit = {},
        onSaveSchedule: (String?, String, Int, Set<Int>) -> String = { _, _, _, _ -> "saved" },
        onDeleteSchedule: (String) -> String = { "deleted" },
        onApplySchedule: (String) -> String = { "applied" },
    ) = TrackerActions(
        onPresetHoursChange = onPresetHoursChange,
        onIntentionChange = onIntentionChange,
        onStartFast = onStartFast,
        onEndFast = onEndFast,
        onCancelFast = onCancelFast,
        onSaveSchedule = onSaveSchedule,
        onDeleteSchedule = onDeleteSchedule,
        onApplySchedule = onApplySchedule,
    )

    private fun setGuidanceScreen() {
        composeRule.setContent {
            CatholicFastingTheme {
                GuidanceScreen(
                    settings = RuleSettings(),
                    ruleBundleAudit = ObservanceCalculator.ruleBundleAudit(),
                    devotionalGallery = SacredImageryCatalog.fastingGallery.take(3),
                )
            }
        }
    }

    private fun setSettingsScreen(settings: RuleSettings = RuleSettings()) {
        composeRule.setContent {
            CatholicFastingTheme {
                SettingsScreen(
                    settings = settings,
                    onSettingsChange = {},
                )
            }
        }
    }

    private fun setPremiumScreen(
        unlocked: Boolean,
        reflections: List<ReflectionJournalEntry> = emptyList(),
        includeCatalogOffers: Boolean = true,
        includeTipOffers: Boolean = false,
    ) {
        val state = AppContainer.repository.dashboardState.value
        composeRule.setContent {
            CatholicFastingTheme {
                PremiumScreen(
                    billingState =
                        BillingState(
                            premiumUnlocked = unlocked,
                            premiumOffers =
                                if (includeCatalogOffers) {
                                    listOf(
                                        BillingOfferUi("yearly", "Premium Yearly", "$29.99", "Billed yearly"),
                                        BillingOfferUi("monthly", "Premium Monthly", "$4.99", "Billed monthly"),
                                    )
                                } else {
                                    emptyList()
                                },
                            tipOffers =
                                if (includeTipOffers) {
                                    listOf(
                                        BillingOfferUi("tip", "Support Tip", "$2.99", "One-time support"),
                                    )
                                } else {
                                    emptyList()
                                },
                            canManageSubscription = unlocked && includeCatalogOffers,
                        ),
                    workspaceState =
                        PremiumWorkspaceUiState(
                            planningData = state.planningData,
                            reflections = reflections,
                            premiumSnapshot = premiumSnapshot(),
                            seasonProgramActions =
                                PremiumSeasonProgramEngine.actions(
                                    program = state.premiumCompanionState.seasonProgram,
                                    week = 1,
                                ),
                            fastPrepGuidance =
                                PremiumFastPrepGuidanceEngine.prepAndRefeed(
                                    targetHours = state.intermittentPresetHours,
                                    hasMedicalDispensation = false,
                                ),
                        ),
                    actions =
                        PremiumWorkspaceActions(
                            onRefresh = {},
                            onManageSubscription = {},
                            onPurchase = {},
                            onSaveReflection = { _, _ -> "saved" },
                        ),
                )
            }
        }
    }

    private fun premiumSnapshot(): PremiumSnapshot {
        val state = AppContainer.repository.dashboardState.value
        return PremiumSnapshotEngine.build(
            observances = state.observances,
            statusesById = state.statusesById,
            sessions = state.intermittentSessions,
            settings = state.settings,
            companionState = state.premiumCompanionState,
            today = LocalDate.now(),
        )
    }

    private fun seedCompletedOnboarding() {
        val repository = AppContainer.repository
        repository.setIndependentAppNoticeAcknowledged(true)
        repository.setSelectedRegion(RegionProfile.US)
        repository.setReminderTier(ReminderTier.BALANCED)
        repository.setIntermittentIntention(IntermittentFastIntention.PRAYER.name)
        repository.completeOnboarding(Instant.parse("2026-03-13T00:00:00Z"))
    }

    private fun assertText(text: String) {
        assertThat(composeRule.onAllNodesWithText(text).fetchSemanticsNodes().size).isAtLeast(1)
    }

    private fun clickTaggedNodeAfterScrolling(tag: String) {
        clickFirstTaggedNodeAfterScrolling(tag)
    }

    private fun clickFirstTaggedNodeAfterScrolling(vararg tags: String) {
        repeat(6) {
            tags.forEach { tag ->
                val node = composeRule.onNodeWithTag(tag)
                if (runCatching { node.assertIsDisplayed() }.isSuccess) {
                    node.performTouchInput { click() }
                    return
                }
            }
            composeRule.onRoot().performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }
        throw AssertionError("Could not find any tagged node after scrolling: ${tags.joinToString()}")
    }
}
