@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfastingapp

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
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
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.model.RuleSettings
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
import com.kevpierce.catholicfasting.feature.today.TodayScreen
import com.kevpierce.catholicfasting.feature.today.TodayUiState
import com.kevpierce.catholicfasting.feature.tracker.TrackerActions
import com.kevpierce.catholicfasting.feature.tracker.TrackerScreen
import com.kevpierce.catholicfasting.feature.tracker.TrackerUiState
import com.kevpierce.catholicfastingapp.ui.CatholicFastingApp
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
    fun onboardingDefaultRouteShowsTrustProfileReminderAndPremiumSections() {
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp()
            }
        }

        assertText(context.getString(R.string.onboarding_notice_title))
        assertText(context.getString(R.string.onboarding_profile_title))
        assertText(context.getString(R.string.onboarding_reminders_title))
        assertText(context.getString(R.string.onboarding_premium_title))
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
    fun onboardingDefaultRouteShowsRegionChoices() {
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp()
            }
        }

        assertText(context.getString(R.string.label_region_us))
        assertText(context.getString(R.string.label_region_canada))
        assertText(context.getString(R.string.label_region_other))
    }

    @Test
    fun onboardingDefaultRouteShowsReminderChoices() {
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp()
            }
        }

        assertText(context.getString(R.string.label_reminder_minimal))
        assertText(context.getString(R.string.label_reminder_balanced))
        assertText(context.getString(R.string.label_reminder_guided))
    }

    @Test
    fun onboardingSpanishResourcesResolveCoreCopy() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, R.string.onboarding_title)
        assertLocalizedString(spanish, R.string.onboarding_notice_title)
        assertLocalizedString(spanish, R.string.label_reminder_guided_summary)
    }

    @Test
    fun onboardingFrenchCanadianResourcesResolveCoreCopy() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, R.string.onboarding_title)
        assertLocalizedString(french, R.string.onboarding_notice_title)
        assertLocalizedString(french, R.string.label_region_canada)
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
    fun premiumScreenShowsSupportTipSection() {
        setPremiumScreen(unlocked = false)

        assertText(context.getString(PremiumR.string.premium_support_tips_title))
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

    @Test
    fun premiumSpanishResourcesResolvePlanCopy() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, PremiumR.string.premium_title)
        assertLocalizedString(spanish, PremiumR.string.premium_subscriptions_title)
        assertLocalizedString(spanish, PremiumR.string.premium_reflection_journal_title)
    }

    @Test
    fun premiumFrenchCanadianResourcesResolvePlanCopy() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, PremiumR.string.premium_title)
        assertLocalizedString(french, PremiumR.string.premium_subscriptions_title)
        assertLocalizedString(french, PremiumR.string.premium_reflection_journal_title)
    }

    @Test
    fun trackerSpanishResourcesResolveCoreControls() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, TrackerR.string.tracker_title)
        assertLocalizedString(spanish, TrackerR.string.tracker_start_fast)
        assertLocalizedString(spanish, TrackerR.string.tracker_custom_schedules)
    }

    @Test
    fun trackerFrenchCanadianResourcesResolveCoreControls() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, TrackerR.string.tracker_title)
        assertLocalizedString(french, TrackerR.string.tracker_start_fast)
        assertLocalizedString(french, TrackerR.string.tracker_custom_schedules)
    }

    @Test
    fun guidanceSpanishResourcesResolveCoreSections() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, GuidanceR.string.guidance_title)
        assertLocalizedString(spanish, GuidanceR.string.guidance_food_title)
        assertLocalizedString(spanish, GuidanceR.string.guidance_rule_audit_title)
    }

    @Test
    fun guidanceFrenchCanadianResourcesResolveCoreSections() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, GuidanceR.string.guidance_title)
        assertLocalizedString(french, GuidanceR.string.guidance_food_title)
        assertLocalizedString(french, GuidanceR.string.guidance_rule_audit_title)
    }

    @Test
    fun calendarSpanishResourcesResolveCoreSections() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, CalendarR.string.calendar_title)
        assertLocalizedString(spanish, CalendarR.string.calendar_search_label)
        assertLocalizedString(spanish, CalendarR.string.calendar_progress_overview)
    }

    @Test
    fun calendarFrenchCanadianResourcesResolveCoreSections() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, CalendarR.string.calendar_title)
        assertLocalizedString(french, CalendarR.string.calendar_search_label)
        assertLocalizedString(french, CalendarR.string.calendar_progress_overview)
    }

    @Test
    fun settingsSpanishResourcesResolveCoreSections() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, SettingsR.string.settings_more_title)
        assertLocalizedString(spanish, SettingsR.string.settings_region)
        assertLocalizedString(spanish, SettingsR.string.settings_birth_year)
    }

    @Test
    fun settingsFrenchCanadianResourcesResolveCoreSections() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, SettingsR.string.settings_more_title)
        assertLocalizedString(french, SettingsR.string.settings_region)
        assertLocalizedString(french, SettingsR.string.settings_birth_year)
    }

    @Test
    fun todaySpanishResourcesResolveCoreSections() {
        val spanish = localizedContext("es-US")

        assertLocalizedString(spanish, TodayR.string.today_title)
        assertLocalizedString(spanish, TodayR.string.today_year_plan_title)
        assertLocalizedString(spanish, TodayR.string.today_important_notice_title)
    }

    @Test
    fun todayFrenchCanadianResourcesResolveCoreSections() {
        val french = localizedContext("fr-CA")

        assertLocalizedString(french, TodayR.string.today_title)
        assertLocalizedString(french, TodayR.string.today_year_plan_title)
        assertLocalizedString(french, TodayR.string.today_important_notice_title)
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
                    actions =
                        TrackerActions(
                            onPresetHoursChange = {},
                            onStartFast = {},
                            onEndFast = {},
                            onCancelFast = {},
                            onSaveSchedule = { _, _, _, _ -> "saved" },
                            onDeleteSchedule = { "deleted" },
                            onApplySchedule = { "applied" },
                        ),
                )
            }
        }
    }

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
                                if (includeCatalogOffers) {
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
        repository.completeOnboarding(Instant.parse("2026-03-13T00:00:00Z"))
    }

    private fun localizedContext(languageTags: String): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList.forLanguageTags(languageTags))
        return context.createConfigurationContext(configuration)
    }

    private fun assertLocalizedString(
        localizedContext: Context,
        resId: Int,
    ) {
        val value = localizedContext.getString(resId)
        check(value.isNotBlank()) { "Expected localized string $resId to resolve." }
    }

    private fun assertText(text: String) {
        assertThat(composeRule.onAllNodesWithText(text).fetchSemanticsNodes().size).isAtLeast(1)
    }
}
