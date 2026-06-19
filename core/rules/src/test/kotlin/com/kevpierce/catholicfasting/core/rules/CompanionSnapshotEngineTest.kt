package com.kevpierce.catholicfasting.core.rules

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.CompanionActionDestination
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.FastProgressState
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.PremiumAdaptiveRulePlan
import com.kevpierce.catholicfasting.core.model.PremiumAnalyticsSummary
import com.kevpierce.catholicfasting.core.model.PremiumRecoveryCoachPlan
import com.kevpierce.catholicfasting.core.model.PremiumReflection
import com.kevpierce.catholicfasting.core.model.PremiumReminderRecommendation
import com.kevpierce.catholicfasting.core.model.PremiumSeasonPlan
import com.kevpierce.catholicfasting.core.model.RuleAuthority
import com.kevpierce.catholicfasting.core.model.RuleCitation
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class CompanionSnapshotEngineTest {
    @Test
    fun requiredRuleTakesPrecedenceBeforeStartingFast() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances =
                    listOf(
                        observance(
                            id = "ash-wednesday",
                            title = "Ash Wednesday",
                            date = "2026-02-18",
                            kind = ObservanceKind.FAST_AND_ABSTINENCE,
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                    ),
                statusesById = emptyMap(),
                sessions = emptyList(),
                activeFast = null,
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 2, 18),
                now = Instant.parse("2026-02-18T14:00:00Z"),
            )

        assertThat(snapshot.ruleDecision.hasMandatoryObservance).isTrue()
        assertThat(snapshot.primaryAction.destination).isEqualTo(CompanionActionDestination.GUIDANCE)
        assertThat(snapshot.primaryAction.id).isEqualTo("today-rule")
    }

    @Test
    fun recoveryTakesPrecedenceOverTodayRule() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances =
                    listOf(
                        observance("missed", "Friday Abstinence", "2026-02-20", ObservanceObligation.MANDATORY),
                        observance("today", "Lenten Friday", "2026-02-27", ObservanceObligation.MANDATORY),
                    ),
                statusesById = mapOf("missed" to CompletionStatus.MISSED),
                sessions = emptyList(),
                activeFast = null,
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 2, 27),
                now = Instant.parse("2026-02-27T14:00:00Z"),
            )

        assertThat(snapshot.primaryAction.id).isEqualTo("recovery")
        assertThat(snapshot.primaryAction.destination).isEqualTo(CompanionActionDestination.FASTING_DAYS)
    }

    @Test
    fun activeFastProgressPreservesIntention() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances = emptyList(),
                statusesById = emptyMap(),
                sessions = emptyList(),
                activeFast =
                    ActiveIntermittentFast(
                        startIso = "2026-03-13T08:00:00Z",
                        targetHours = 16,
                        intentionId = IntermittentFastIntention.PRAYER.name,
                    ),
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 3, 13),
                now = Instant.parse("2026-03-13T12:00:00Z"),
            )

        assertThat(snapshot.liveFast.progress).isInstanceOf(FastProgressState.Active::class.java)
        assertThat(snapshot.liveFast.intentionId).isEqualTo(IntermittentFastIntention.PRAYER.name)
        assertThat(snapshot.primaryAction.destination).isEqualTo(CompanionActionDestination.TRACK_FAST)
    }

    @Test
    fun targetReachedKeepsTrackFastAsPrimaryAction() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances = emptyList(),
                statusesById = emptyMap(),
                sessions = emptyList(),
                activeFast =
                    ActiveIntermittentFast(
                        startIso = "2026-03-13T08:00:00Z",
                        targetHours = 16,
                    ),
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 3, 14),
                now = Instant.parse("2026-03-14T01:00:00Z"),
            )

        assertThat(snapshot.liveFast.progress).isInstanceOf(FastProgressState.TargetReached::class.java)
        assertThat(snapshot.primaryAction.id).isEqualTo("active-fast")
        assertThat(snapshot.primaryAction.destination).isEqualTo(CompanionActionDestination.TRACK_FAST)
    }

    @Test
    fun corruptActiveFastTargetFallsBackToOneHour() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances = emptyList(),
                statusesById = emptyMap(),
                sessions = emptyList(),
                activeFast =
                    ActiveIntermittentFast(
                        startIso = "2026-03-13T08:00:00Z",
                        targetHours = 0,
                    ),
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 3, 13),
                now = Instant.parse("2026-03-13T08:30:00Z"),
            )

        assertThat(snapshot.liveFast.targetHours).isEqualTo(1)
        assertThat((snapshot.liveFast.progress as FastProgressState.Active).targetHours).isEqualTo(1)
    }

    @Test
    fun recentCompletedSessionProducesRecapAction() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances = emptyList(),
                statusesById = emptyMap(),
                sessions =
                    listOf(
                        IntermittentFastSession(
                            id = "session",
                            startIso = "2026-03-13T08:00:00Z",
                            endIso = "2026-03-14T00:30:00Z",
                            targetHours = 16,
                            completedTarget = true,
                            intentionId = IntermittentFastIntention.MERCY.name,
                            reviewNote = "Shared dinner without rushing.",
                        ),
                    ),
                activeFast = null,
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 3, 14),
                now = Instant.parse("2026-03-14T01:00:00Z"),
            )

        assertThat(snapshot.liveFast.progress).isInstanceOf(FastProgressState.CompletedRecap::class.java)
        assertThat(snapshot.primaryAction.id).isEqualTo("fast-recap")
        assertThat(snapshot.liveFast.latestSessionRecap?.reviewNote).isEqualTo("Shared dinner without rushing.")
    }

    @Test
    fun olderCompletedSessionCanEnterEatingWindowWithoutBecomingPrimaryAction() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances = emptyList(),
                statusesById = emptyMap(),
                sessions =
                    listOf(
                        IntermittentFastSession(
                            id = "session",
                            startIso = "2026-03-13T08:00:00Z",
                            endIso = "2026-03-14T00:00:00Z",
                            targetHours = 16,
                            completedTarget = true,
                        ),
                    ),
                activeFast = null,
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 3, 14),
                now = Instant.parse("2026-03-14T03:00:00Z"),
            )

        assertThat(snapshot.liveFast.progress).isInstanceOf(FastProgressState.EatingWindow::class.java)
        assertThat(snapshot.primaryAction.id).isEqualTo("start-fast")
    }

    @Test
    fun nextRequiredDayBecomesPrimaryWhenNothingMoreUrgentExists() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances =
                    listOf(
                        observance(
                            id = "good-friday",
                            title = "Good Friday",
                            date = "2026-04-03",
                            kind = ObservanceKind.FAST_AND_ABSTINENCE,
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                    ),
                statusesById = emptyMap(),
                sessions = emptyList(),
                activeFast = null,
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 3, 20),
                now = Instant.parse("2026-03-20T12:00:00Z"),
            )

        assertThat(snapshot.primaryAction.id).isEqualTo("next-required")
        assertThat(snapshot.primaryAction.destination).isEqualTo(CompanionActionDestination.FASTING_DAYS)
        assertThat(snapshot.nextRequiredObservance?.title).isEqualTo("Good Friday")
    }

    @Test
    fun startFastIsDefaultWhenNoRuleFastOrRequiredDayExists() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances = emptyList(),
                statusesById = emptyMap(),
                sessions = emptyList(),
                activeFast = null,
                settings = RuleSettings(),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 6, 16),
                now = Instant.parse("2026-06-16T12:00:00Z"),
            )

        assertThat(snapshot.primaryAction.id).isEqualTo("start-fast")
        assertThat(snapshot.primaryAction.destination).isEqualTo(CompanionActionDestination.TRACK_FAST)
    }

    @Test
    fun medicalDispensationProducesPrudentRecapAndGuidanceAction() {
        val snapshot =
            CompanionSnapshotEngine.build(
                observances = emptyList(),
                statusesById = emptyMap(),
                sessions =
                    listOf(
                        IntermittentFastSession(
                            id = "short",
                            startIso = "2026-03-13T08:00:00Z",
                            endIso = "2026-03-13T09:00:00Z",
                            targetHours = 16,
                            completedTarget = false,
                        ),
                    ),
                activeFast = null,
                settings = RuleSettings(hasMedicalDispensation = true),
                premiumSnapshot = premiumSnapshot(),
                premiumUnlocked = false,
                today = LocalDate.of(2026, 3, 13),
                now = Instant.parse("2026-03-13T09:30:00Z"),
            )

        assertThat(snapshot.primaryAction.id).isEqualTo("medical-guidance")
        assertThat(snapshot.liveFast.latestSessionRecap?.title).isEqualTo("Prudent discipline logged")
    }

    private fun premiumSnapshot(): PremiumSnapshot =
        PremiumSnapshot(
            season = LiturgicalSeason.LENT,
            seasonPlan =
                PremiumSeasonPlan(
                    titleLine = "Lenten Companion",
                    focusLine = "Prepare with prayer, fasting, and mercy.",
                    practices = listOf("Choose a clear fasting intention."),
                    fastingIntensity = "Strong",
                ),
            reminderRecommendation =
                PremiumReminderRecommendation(
                    shouldEnableDailySupport = true,
                    shouldEnableMorning = true,
                    shouldEnableEvening = false,
                    summaryLine = "Morning support is enough.",
                ),
            analyticsSummary =
                PremiumAnalyticsSummary(
                    requiredCompletionPercent = 0,
                    overallCompletionPercent = 0,
                    missedCount = 0,
                    substitutedCount = 0,
                    intermittentTargetHitPercent = 0,
                    seasonRows = emptyList(),
                ),
            reflection =
                PremiumReflection(
                    title = "Review quietly",
                    body = "Keep the fast close to prayer.",
                    action = "Make one concrete act of mercy.",
                ),
            adaptiveRulePlan =
                PremiumAdaptiveRulePlan(
                    title = "Weekly rule",
                    summary = "A stable local plan.",
                    weeklyActions = emptyList(),
                    caution = "Keep health first.",
                ),
            recoveryCoachPlan =
                PremiumRecoveryCoachPlan(
                    title = "Recovery Stable",
                    summary = "No recovery action is needed.",
                    steps = emptyList(),
                ),
            motivationLine = "One faithful step.",
        )

    private fun observance(
        id: String,
        title: String,
        date: String,
        obligation: ObservanceObligation,
        kind: ObservanceKind = ObservanceKind.ABSTINENCE,
    ): Observance =
        Observance(
            id = id,
            title = title,
            date = date,
            kind = kind,
            obligation = obligation,
            detail = "Keep the day visible.",
            rationale = "Current rule metadata marks this day for discipline.",
            citations =
                listOf(
                    RuleCitation(
                        authority = RuleAuthority.USCCB,
                        title = "Fast and Abstinence",
                        shortReference = "USCCB fasting norms",
                    ),
                ),
            ruleVersion = "test",
        )
}
