@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfastingapp

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.billing.BillingState
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.rules.PremiumFastPrepGuidanceEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSeasonProgramEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshotEngine
import com.kevpierce.catholicfasting.core.ui.CatholicFastingTheme
import com.kevpierce.catholicfasting.feature.calendar.CalendarScreen
import com.kevpierce.catholicfasting.feature.premium.PremiumScreen
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceActions
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceUiState
import com.kevpierce.catholicfastingapp.ui.CatholicFastingApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import com.kevpierce.catholicfasting.feature.calendar.R as CalendarR
import com.kevpierce.catholicfasting.feature.premium.R as PremiumR
import com.kevpierce.catholicfasting.feature.today.R as TodayR
import com.kevpierce.catholicfasting.feature.tracker.R as TrackerR

@RunWith(AndroidJUnit4::class)
class PersistenceRecreateInstrumentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppContainer.resetForTesting(context)
    }

    @Test
    fun onboardingChoicesPersistAfterRepositoryReloadAndRecompose() {
        val repository = AppContainer.repository
        repository.setIndependentAppNoticeAcknowledged(true)
        repository.setSelectedRegion(RegionProfile.CANADA)
        repository.setReminderTier(ReminderTier.GUIDED)
        repository.completeOnboarding(Instant.parse("2026-03-13T00:00:00Z"))
        repository.flushForTesting()

        waitForReloadedState("completed guided Canada onboarding") {
            it.launchFunnelSnapshot.completedOnboardingAtIso != null &&
                it.launchFunnelSnapshot.selectedRegion == RegionProfile.CANADA &&
                it.launchFunnelSnapshot.selectedReminderTier == ReminderTier.GUIDED
        }

        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp(initialDeepLink = AppDeepLinks.TODAY)
            }
        }

        assertText(context.getString(TodayR.string.today_title))
        composeRule.onAllNodesWithText(context.getString(R.string.onboarding_title)).assertCountEquals(0)
    }

    @Test
    fun trackerStartAndCancelPersistAcrossRepositoryReloads() {
        seedCompletedOnboarding()
        composeRule.setContent {
            CatholicFastingTheme {
                CatholicFastingApp(initialDeepLink = AppDeepLinks.TRACKER)
            }
        }

        clickText(context.getString(TrackerR.string.tracker_start_fast))
        waitForLiveState("active fast visible after start") { it.activeIntermittentFast != null }
        assertText(context.getString(TrackerR.string.tracker_fast_in_progress))
        AppContainer.repository.flushForTesting()
        waitForReloadedState("active fast stored after start") { it.activeIntermittentFast != null }

        AppContainer.repository.cancelIntermittentFast()
        AppContainer.repository.flushForTesting()
        waitForReloadedState("active fast cleared after cancel") { it.activeIntermittentFast == null }
    }

    @Test
    fun calendarCompletionAndFridayNotePersistAfterRepositoryReload() {
        seedCompletedOnboarding()
        val observanceId =
            AppContainer.repository.dashboardState.value.observances
                .first { it.obligation == ObservanceObligation.MANDATORY }
                .id
        val fridayId =
            AppContainer.repository.dashboardState.value.observances
                .first { it.title.contains("Friday") }
                .id

        AppContainer.repository.setStatus(observanceId, CompletionStatus.COMPLETED)
        AppContainer.repository.setFridayNote(fridayId, "Local release note")
        AppContainer.repository.flushForTesting()

        waitForReloadedState("calendar status and note stored") {
            it.statusesById[observanceId] == CompletionStatus.COMPLETED &&
                it.fridayNotesById[fridayId] == "Local release note"
        }

        setCalendarScreen(observanceId)

        assertText(CompletionStatus.COMPLETED.label)
        assertThat(AppContainer.repository.dashboardState.value.fridayNotesById[fridayId])
            .isEqualTo("Local release note")
        assertText(context.getString(CalendarR.string.calendar_title))
    }

    @Test
    fun premiumReflectionSavePersistsAfterRepositoryReload() {
        seedCompletedOnboarding()
        setPremiumScreen()
        repeat(3) { composeRule.onRoot().performTouchInput { swipeUp() } }

        composeRule
            .onAllNodesWithText(context.getString(PremiumR.string.premium_reflection_title_label))
            .onFirst()
            .performTextInput("Release reflection")
        composeRule
            .onAllNodesWithText(context.getString(PremiumR.string.premium_reflection_body_label))
            .onFirst()
            .performTextInput("Local-only save proof")
        clickText(context.getString(PremiumR.string.premium_save_reflection))

        waitForLiveState("premium reflection visible in live state") {
            it.reflections.any { reflection ->
                reflection.title == "Release reflection" &&
                    reflection.body == "Local-only save proof"
            }
        }
        AppContainer.repository.flushForTesting()
        waitForReloadedState("premium reflection stored") {
            it.reflections.any { reflection ->
                reflection.title == "Release reflection" &&
                    reflection.body == "Local-only save proof"
            }
        }
    }

    private fun setCalendarScreen(observanceId: String) {
        val state = AppContainer.repository.dashboardState.value
        val observance = state.observances.first { it.id == observanceId }
        val premiumSnapshot =
            PremiumSnapshotEngine.build(
                observances = state.observances,
                statusesById = state.statusesById,
                sessions = state.intermittentSessions,
                settings = state.settings,
                companionState = state.premiumCompanionState,
                today = LocalDate.now(),
            )

        composeRule.setContent {
            CatholicFastingTheme {
                CalendarScreen(
                    observances = listOf(observance),
                    statusesById = state.statusesById,
                    fridayNotesById = state.fridayNotesById,
                    premiumSnapshot = premiumSnapshot,
                    onStatusChange = AppContainer.repository::setStatus,
                    onFridayNoteChange = AppContainer.repository::setFridayNote,
                )
            }
        }
    }

    private fun setPremiumScreen() {
        val repository = AppContainer.repository
        val state = repository.dashboardState.value
        val premiumSnapshot =
            PremiumSnapshotEngine.build(
                observances = state.observances,
                statusesById = state.statusesById,
                sessions = state.intermittentSessions,
                settings = state.settings,
                companionState = state.premiumCompanionState,
                today = LocalDate.now(),
            )

        composeRule.setContent {
            CatholicFastingTheme {
                PremiumScreen(
                    billingState = BillingState(premiumUnlocked = true),
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
                            onSaveReflection = { title, body ->
                                repository.addReflectionEntry(title, body).fold(
                                    onSuccess = { "saved" },
                                    onFailure = { it.message.orEmpty() },
                                )
                            },
                        ),
                )
            }
        }
    }

    private fun seedCompletedOnboarding() {
        val repository = AppContainer.repository
        repository.setIndependentAppNoticeAcknowledged(true)
        repository.setSelectedRegion(RegionProfile.US)
        repository.setReminderTier(ReminderTier.BALANCED)
        repository.completeOnboarding(Instant.parse("2026-03-13T00:00:00Z"))
        repository.flushForTesting()
        waitForReloadedState("completed onboarding seed stored") {
            it.launchFunnelSnapshot.completedOnboardingAtIso != null
        }
    }

    private fun waitForReloadedState(
        description: String,
        timeoutMs: Long = 5_000,
        predicate: (com.kevpierce.catholicfasting.core.data.DashboardState) -> Boolean,
    ) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            AppContainer.reloadForTesting(context)
            if (predicate(AppContainer.repository.dashboardState.value)) {
                return
            }
            Thread.sleep(100)
        }
        throw AssertionError("Timed out waiting for $description.")
    }

    private fun waitForLiveState(
        description: String,
        timeoutMs: Long = 5_000,
        predicate: (com.kevpierce.catholicfasting.core.data.DashboardState) -> Boolean,
    ) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (predicate(AppContainer.repository.dashboardState.value)) {
                return
            }
            Thread.sleep(100)
        }
        throw AssertionError("Timed out waiting for $description.")
    }

    private fun clickText(text: String) {
        composeRule.onAllNodesWithText(text).onFirst().performClick()
    }

    private fun assertText(text: String) {
        assertThat(composeRule.onAllNodesWithText(text).fetchSemanticsNodes().size).isAtLeast(1)
    }
}
