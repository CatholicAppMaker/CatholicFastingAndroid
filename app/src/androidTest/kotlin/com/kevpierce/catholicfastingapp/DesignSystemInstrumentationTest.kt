package com.kevpierce.catholicfastingapp

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.billing.BillingOfferUi
import com.kevpierce.catholicfasting.core.billing.BillingState
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.data.buildSeasonalHeroState
import com.kevpierce.catholicfasting.core.model.CompanionActionDestination
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.RuleSettings
import com.kevpierce.catholicfasting.core.rules.CompanionSnapshotEngine
import com.kevpierce.catholicfasting.core.rules.ObservanceCalculator
import com.kevpierce.catholicfasting.core.rules.PremiumFastPrepGuidanceEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSeasonProgramEngine
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
import java.time.LocalDate
import com.kevpierce.catholicfasting.feature.calendar.R as CalendarR
import com.kevpierce.catholicfasting.feature.guidance.R as GuidanceR
import com.kevpierce.catholicfasting.feature.premium.R as PremiumR
import com.kevpierce.catholicfasting.feature.settings.R as SettingsR
import com.kevpierce.catholicfasting.feature.today.R as TodayR
import com.kevpierce.catholicfasting.feature.tracker.R as TrackerR

@RunWith(AndroidJUnit4::class)
class DesignSystemInstrumentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppContainer.initialize(context)
    }

    @Test
    fun todayScreenRendersFeaturedSeasonalHierarchyUnderSharedTheme() {
        val state = AppContainer.repository.dashboardState.value
        val premiumSnapshot = premiumSnapshot()
        val seasonalPack = SeasonalContentPackCatalog.pack(premiumSnapshot.season, contentLocale())

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
                            completionSummary =
                                context.resources.getQuantityString(
                                    R.plurals.summary_completion_value,
                                    state.statusesById.size,
                                    state.statusesById.size,
                                ),
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

        composeRule.onAllNodesWithText(context.getString(TodayR.string.today_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(seasonalPack.campaignTitle).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(TodayR.string.today_year_plan_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(TodayR.string.today_personal_insights_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(TodayR.string.today_devotional_gallery_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(TodayR.string.today_important_notice_title)).assertCountEquals(1)
    }

    @Test
    fun todayCompanionPrimaryActionInvokesDestinationCallback() {
        val state = AppContainer.repository.dashboardState.value
        val premiumSnapshot = premiumSnapshot()
        val seasonalPack = SeasonalContentPackCatalog.pack(premiumSnapshot.season, contentLocale())
        val companionSnapshot =
            CompanionSnapshotEngine.build(
                observances = emptyList(),
                statusesById = emptyMap(),
                sessions = emptyList(),
                activeFast = null,
                settings = state.settings,
                premiumSnapshot = premiumSnapshot,
                premiumUnlocked = false,
                today = LocalDate.of(2026, 6, 16),
            )
        var selectedDestination: CompanionActionDestination? = null

        composeRule.setContent {
            CatholicFastingTheme {
                TodayScreen(
                    uiState =
                        TodayUiState(
                            todayObservance = null,
                            companionSnapshot = companionSnapshot,
                            completionSummary = "No completions yet.",
                            premiumSnapshot = premiumSnapshot,
                            seasonalContentPack = seasonalPack,
                            dailyFormationLine = "Prepare calmly.",
                            dailyQuote = SeasonalContentSupport.dailyQuote(premiumSnapshot.season, seasonalPack, LocalDate.now()),
                            devotionalGallery = emptyList(),
                            setupProgressSummary = "Setup progress",
                            yearPlanSummary = "Year plan",
                            weeklyRecap = "Weekly recap",
                            streakMessage = "Streak summary",
                            noticeSummary = context.getString(R.string.notice_independent_app_summary),
                        ),
                    onCompanionAction = { action ->
                        selectedDestination = action.destination
                    },
                )
            }
        }

        composeRule.onAllNodesWithText(context.getString(TodayR.string.today_companion_title)).assertCountEquals(1)
        composeRule.onNodeWithText("Begin an intentional fast").performClick()
        composeRule.runOnIdle {
            assertThat(selectedDestination).isEqualTo(CompanionActionDestination.TRACK_FAST)
        }
    }

    @Test
    fun premiumScreenRendersWorkspaceSectionsUnderSharedTheme() {
        val state = AppContainer.repository.dashboardState.value
        val premiumSnapshot = premiumSnapshot()

        composeRule.setContent {
            CatholicFastingTheme {
                PremiumScreen(
                    billingState =
                        BillingState(
                            premiumUnlocked = true,
                            premiumOffers =
                                listOf(
                                    BillingOfferUi(
                                        productId = "premium.monthly",
                                        displayTitle = "Monthly",
                                        priceLabel = "$4.99",
                                        billingLabel = "Billed monthly",
                                    ),
                                ),
                        ),
                    workspaceState =
                        PremiumWorkspaceUiState(
                            planningData = state.planningData,
                            reflections = state.reflections,
                            premiumSnapshot = premiumSnapshot,
                            seasonProgramActions =
                                PremiumSeasonProgramEngine.actions(
                                    program = state.premiumCompanionState.seasonProgram,
                                    week = 1,
                                ),
                            fastPrepGuidance =
                                PremiumFastPrepGuidanceEngine.prepAndRefeed(
                                    targetHours = state.intermittentPresetHours,
                                    hasMedicalDispensation = state.settings.hasMedicalDispensation,
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

        composeRule.onAllNodesWithText(context.getString(PremiumR.string.premium_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(PremiumR.string.premium_subscriptions_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(PremiumR.string.premium_support_tips_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(PremiumR.string.premium_planning_export_title)).assertCountEquals(1)
        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onAllNodesWithText(context.getString(PremiumR.string.premium_analytics_recovery_title)).assertCountEquals(1)
        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onAllNodesWithText(context.getString(PremiumR.string.premium_reflection_journal_title)).assertCountEquals(1)
    }

    @Test
    fun guidanceScreenRendersFoodAndAuditSectionsUnderSharedTheme() {
        composeRule.setContent {
            CatholicFastingTheme {
                GuidanceScreen(
                    settings = RuleSettings(),
                    ruleBundleAudit = ObservanceCalculator.ruleBundleAudit(),
                    devotionalGallery = SacredImageryCatalog.fastingGallery.take(3),
                )
            }
        }

        composeRule.onAllNodesWithText(context.getString(GuidanceR.string.guidance_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(GuidanceR.string.guidance_food_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(GuidanceR.string.guidance_stricter_practice)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(GuidanceR.string.guidance_if_unsure)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(GuidanceR.string.guidance_rule_audit_title)).assertCountEquals(1)
        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onAllNodesWithText(context.getString(GuidanceR.string.guidance_symbol_gallery_title)).assertCountEquals(1)
    }

    @Test
    fun calendarScreenRendersAnalyticsAndSearchUnderSharedTheme() {
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

        composeRule.onAllNodesWithText(context.getString(CalendarR.string.calendar_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(CalendarR.string.calendar_progress_overview)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(CalendarR.string.calendar_search_label)).assertCountEquals(1)
    }

    @Test
    fun trackerScreenRendersControlPlannerAndRecoveryHierarchyUnderSharedTheme() {
        val state = AppContainer.repository.dashboardState.value

        composeRule.setContent {
            CatholicFastingTheme {
                TrackerScreen(
                    uiState =
                        TrackerUiState(
                            schedules = state.schedules,
                            activeScheduleId = state.activeIntermittentScheduleId,
                            sessions = state.intermittentSessions,
                            activeFast = state.activeIntermittentFast,
                            presetHours = state.intermittentPresetHours,
                            selectedIntentionId = state.launchFunnelSnapshot.selectedIntermittentIntentionId,
                            latestRecap = null,
                            premiumSnapshot = premiumSnapshot(),
                            prepGuidance =
                                PremiumFastPrepGuidanceEngine.prepAndRefeed(
                                    targetHours = state.intermittentPresetHours,
                                    hasMedicalDispensation = state.settings.hasMedicalDispensation,
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
                            onIntentionChange = { _ -> },
                            onStartFast = { _ -> },
                            onEndFast = { _ -> },
                            onCancelFast = {},
                            onSaveSchedule = { _, _, _, _ -> "saved" },
                            onDeleteSchedule = { "deleted" },
                            onApplySchedule = { "applied" },
                        ),
                )
            }
        }

        composeRule.onAllNodesWithText(context.getString(TrackerR.string.tracker_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(TrackerR.string.tracker_control_center)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(TrackerR.string.tracker_custom_schedules)).assertCountEquals(1)
        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onAllNodesWithText(context.getString(TrackerR.string.tracker_recent_summary)).assertCountEquals(1)
        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onAllNodesWithText(context.getString(TrackerR.string.tracker_preparation_recovery)).assertCountEquals(1)
    }

    @Test
    fun trackerIntentionSelectionIsUsedWhenStartingFastImmediately() {
        val state = AppContainer.repository.dashboardState.value
        var persistedIntentionId: String? = null
        var startedIntentionId: String? = null

        composeRule.setContent {
            CatholicFastingTheme {
                TrackerScreen(
                    uiState =
                        TrackerUiState(
                            schedules = emptyList(),
                            activeScheduleId = null,
                            sessions = emptyList(),
                            activeFast = null,
                            presetHours = 16,
                            selectedIntentionId = IntermittentFastIntention.PERSONAL_DISCIPLINE.name,
                            latestRecap = null,
                            premiumSnapshot = premiumSnapshot(),
                            prepGuidance =
                                PremiumFastPrepGuidanceEngine.prepAndRefeed(
                                    targetHours = 16,
                                    hasMedicalDispensation = state.settings.hasMedicalDispensation,
                                ),
                            seasonProgramActions = emptyList(),
                        ),
                    actions =
                        TrackerActions(
                            onPresetHoursChange = {},
                            onIntentionChange = { persistedIntentionId = it },
                            onStartFast = { startedIntentionId = it },
                            onEndFast = { _ -> },
                            onCancelFast = {},
                            onSaveSchedule = { _, _, _, _ -> "saved" },
                            onDeleteSchedule = { "deleted" },
                            onApplySchedule = { "applied" },
                        ),
                )
            }
        }

        composeRule.onNodeWithText(IntermittentFastIntention.PRAYER.label).performClick()
        composeRule.onNodeWithText(context.getString(TrackerR.string.tracker_start_fast)).performClick()
        composeRule.runOnIdle {
            assertThat(persistedIntentionId).isEqualTo(IntermittentFastIntention.PRAYER.name)
            assertThat(startedIntentionId).isEqualTo(IntermittentFastIntention.PRAYER.name)
        }
    }

    @Test
    fun settingsScreenRendersProfileAndNormsHierarchyUnderSharedTheme() {
        composeRule.setContent {
            CatholicFastingTheme {
                SettingsScreen(
                    settings = RuleSettings(),
                    onSettingsChange = {},
                )
            }
        }

        composeRule.onAllNodesWithText(context.getString(SettingsR.string.settings_more_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(SettingsR.string.settings_profile_norms)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(SettingsR.string.settings_region)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(SettingsR.string.settings_calendar)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(SettingsR.string.settings_friday_mode)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(SettingsR.string.settings_birth_year)).assertCountEquals(1)
    }

    @Test
    fun appShellRendersMoreHubHierarchyFromDeepLinkUnderSharedTheme() {
        AppContainer.repository.completeOnboarding()

        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp(initialDeepLink = "https://local/more/setup")
            }
        }

        composeRule.onAllNodesWithText(context.getString(R.string.more_title)).assertCountEquals(2)
        composeRule.onAllNodesWithText(context.getString(R.string.more_support_premium)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.more_setup_reminders)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.more_profile_norms)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.more_guidance_rules)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.more_history_fasting)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.more_privacy_data)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.more_reminder_center_title)).assertCountEquals(1)
    }

    @Test
    fun appShellRendersHistoryTimelineAndArticleFromDeepLink() {
        AppContainer.repository.completeOnboarding()

        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp(initialDeepLink = "https://local/more/history")
            }
        }

        composeRule.onAllNodesWithText(context.getString(R.string.more_history_fasting)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.history_timeline_section)).assertCountEquals(1)
        composeRule.onAllNodesWithText("Early Church foundations").assertCountEquals(1)
        composeRule.onNodeWithText("Early Church foundations").performClick()
        composeRule.onAllNodesWithText(context.getString(R.string.history_article_body)).assertCountEquals(1)
        composeRule
            .onAllNodesWithText("The earliest Christian fasting was not a single universal schedule.", substring = true)
            .assertCountEquals(1)
    }

    private fun premiumSnapshot() =
        PremiumSnapshotEngine.build(
            observances = AppContainer.repository.dashboardState.value.observances,
            statusesById = AppContainer.repository.dashboardState.value.statusesById,
            sessions = AppContainer.repository.dashboardState.value.intermittentSessions,
            settings = AppContainer.repository.dashboardState.value.settings,
            companionState = AppContainer.repository.dashboardState.value.premiumCompanionState,
            today = LocalDate.now(),
        )

    private fun contentLocale() =
        if (context.resources.configuration.locales[0]
                ?.language
                ?.startsWith("es") == true
        ) {
            com.kevpierce.catholicfasting.core.model.ContentLocale.SPANISH
        } else {
            com.kevpierce.catholicfasting.core.model.ContentLocale.ENGLISH
        }
}

@RunWith(AndroidJUnit4::class)
class OnboardingDesignInstrumentationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun freshLaunchShowsOnboardingHeroStructure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AppContainer.initialize(context)
        val seasonalHero = buildSeasonalHeroState()

        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_notice_title)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_profile_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_reminders_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_intention_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_premium_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(seasonalHero.campaignTitle).assertCountEquals(0)
    }
}
