package com.kevpierce.catholicfasting.core.rules

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.AscensionObservance
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.PremiumAnalyticsSummary
import com.kevpierce.catholicfasting.core.model.PremiumCompanionState
import com.kevpierce.catholicfasting.core.model.PremiumReflection
import com.kevpierce.catholicfasting.core.model.PremiumReminderRecommendation
import com.kevpierce.catholicfasting.core.model.PremiumRuleTemplate
import com.kevpierce.catholicfasting.core.model.PremiumSeasonPlan
import com.kevpierce.catholicfasting.core.model.PremiumSubscriptionState
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class PremiumExperienceEngineTest {
    private val defaultSettings =
        RuleSettings(
            birthYear = 1991,
            hasMedicalDispensation = false,
            ascensionObservance = AscensionObservance.SUNDAY,
            fridayOutsideLentMode = FridayOutsideLentMode.SUBSTITUTE_PENANCE,
        )

    @Test
    fun seasonPlanLentWithoutDispensationIsStrong() {
        val plan = PremiumSeasonPlanEngine.plan(LiturgicalSeason.LENT, defaultSettings)

        assertThat(plan.fastingIntensity).isEqualTo("Strong")
        assertThat(plan.titleLine.lowercase()).contains("lenten")
    }

    @Test
    fun reminderPlannerUsesRecoveryModeForMultipleMisses() {
        val recommendation =
            PremiumReminderPlanner.recommendation(
                observances =
                    listOf(
                        testObservance("a", "A", "2026-03-02", ObservanceObligation.MANDATORY),
                        testObservance("b", "B", "2026-03-05", ObservanceObligation.MANDATORY),
                        testObservance("c", "C", "2026-03-08", ObservanceObligation.MANDATORY),
                    ),
                statusesById =
                    mapOf(
                        "a" to CompletionStatus.MISSED,
                        "b" to CompletionStatus.MISSED,
                        "c" to CompletionStatus.COMPLETED,
                    ),
                now = LocalDate.of(2026, 3, 10),
            )

        assertThat(recommendation.shouldEnableDailySupport).isTrue()
        assertThat(recommendation.shouldEnableMorning).isTrue()
        assertThat(recommendation.shouldEnableEvening).isTrue()
        assertThat(recommendation.summaryLine.lowercase()).contains("recovery")
    }

    @Test
    fun reminderPlannerUsesPreparationModeWhenRequiredDaysAreUpcoming() {
        val recommendation =
            PremiumReminderPlanner.recommendation(
                observances =
                    listOf(
                        testObservance("done", "Done", "2026-03-03", ObservanceObligation.MANDATORY),
                        testObservance("upcoming", "Upcoming", "2026-03-15", ObservanceObligation.MANDATORY),
                    ),
                statusesById = mapOf("done" to CompletionStatus.COMPLETED),
                now = LocalDate.of(2026, 3, 10),
            )

        assertThat(recommendation.shouldEnableDailySupport).isTrue()
        assertThat(recommendation.shouldEnableMorning).isTrue()
        assertThat(recommendation.shouldEnableEvening).isFalse()
        assertThat(recommendation.summaryLine.lowercase()).contains("preparation")
    }

    @Test
    fun reminderPlannerUsesMaintenanceModeWhenRhythmIsStable() {
        val recommendation =
            PremiumReminderPlanner.recommendation(
                observances =
                    listOf(
                        testObservance("a", "A", "2026-03-02", ObservanceObligation.MANDATORY),
                        testObservance("b", "B", "2026-03-06", ObservanceObligation.OPTIONAL),
                    ),
                statusesById =
                    mapOf(
                        "a" to CompletionStatus.COMPLETED,
                        "b" to CompletionStatus.COMPLETED,
                    ),
                now = LocalDate.of(2026, 3, 20),
            )

        assertThat(recommendation.shouldEnableDailySupport).isTrue()
        assertThat(recommendation.shouldEnableMorning).isFalse()
        assertThat(recommendation.shouldEnableEvening).isTrue()
        assertThat(recommendation.summaryLine.lowercase()).contains("maintenance")
    }

    @Test
    fun analyticsSummaryComputesCompletionAndIntermittentRate() {
        val summary =
            PremiumAnalyticsEngine.summary(
                observances =
                    listOf(
                        testObservance("r1", "R1", "2026-02-10", ObservanceObligation.MANDATORY),
                        testObservance("r2", "R2", "2026-03-10", ObservanceObligation.MANDATORY),
                        testObservance("o1", "O1", "2026-07-10", ObservanceObligation.OPTIONAL),
                    ),
                statusesById =
                    mapOf(
                        "r1" to CompletionStatus.COMPLETED,
                        "r2" to CompletionStatus.MISSED,
                        "o1" to CompletionStatus.SUBSTITUTED,
                    ),
                sessions =
                    listOf(
                        IntermittentFastSession(
                            id = "s1",
                            startIso = "2026-01-01T00:00:00Z",
                            endIso = "2026-01-02T00:00:00Z",
                            targetHours = 16,
                            completedTarget = true,
                        ),
                        IntermittentFastSession(
                            id = "s2",
                            startIso = "2026-01-03T00:00:00Z",
                            endIso = "2026-01-03T10:00:00Z",
                            targetHours = 16,
                            completedTarget = false,
                        ),
                    ),
            )

        assertThat(summary.requiredCompletionPercent).isEqualTo(50)
        assertThat(summary.overallCompletionPercent).isEqualTo(67)
        assertThat(summary.missedCount).isEqualTo(1)
        assertThat(summary.substitutedCount).isEqualTo(1)
        assertThat(summary.intermittentTargetHitPercent).isEqualTo(50)
        assertThat(summary.seasonRows).isNotEmpty()
    }

    @Test
    fun reflectionIsDeterministicForDateAndSeason() {
        val first = PremiumReflectionEngine.reflection(LocalDate.of(2026, 12, 9), LiturgicalSeason.ADVENT)
        val second = PremiumReflectionEngine.reflection(LocalDate.of(2026, 12, 9), LiturgicalSeason.ADVENT)

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun directionSummaryContainsCoreSections() {
        val summary =
            PremiumDirectionSummaryEngine.summaryText(
                date = Instant.parse("2026-03-01T10:15:00Z"),
                season = LiturgicalSeason.LENT,
                analytics =
                    PremiumAnalyticsSummary(
                        requiredCompletionPercent = 80,
                        overallCompletionPercent = 77,
                        missedCount = 1,
                        substitutedCount = 2,
                        intermittentTargetHitPercent = 60,
                        seasonRows = emptyList(),
                    ),
                reminder =
                    PremiumReminderRecommendation(
                        shouldEnableDailySupport = true,
                        shouldEnableMorning = true,
                        shouldEnableEvening = false,
                        summaryLine = "Preparation mode",
                    ),
                plan =
                    PremiumSeasonPlan(
                        titleLine = "Lenten Discipline Plan",
                        focusLine = "Repentance focus",
                        practices = listOf("Practice 1"),
                        fastingIntensity = "Strong",
                    ),
                latestReflection =
                    PremiumReflection(
                        title = "Return to the Lord",
                        body = "Fasting with prayer.",
                        action = "Pray now.",
                    ),
            )

        assertThat(summary).contains("Catholic Fasting Premium Summary")
        assertThat(summary).contains("Discipline Metrics")
        assertThat(summary).contains("Reminder Strategy")
        assertThat(summary).contains("Reflection")
    }

    @Test
    fun snapshotBuildCarriesRecoveryGuidanceAndStreakMotivation() {
        val snapshot =
            PremiumSnapshotEngine.build(
                observances =
                    listOf(
                        testObservance("missed", "Ash Wednesday", "2026-03-05", ObservanceObligation.MANDATORY),
                        testObservance("steady", "Friday Penance", "2026-03-07", ObservanceObligation.OPTIONAL),
                        testObservance("required", "Lenten Friday", "2026-03-09", ObservanceObligation.MANDATORY),
                    ),
                statusesById =
                    mapOf(
                        "missed" to CompletionStatus.MISSED,
                        "steady" to CompletionStatus.COMPLETED,
                        "required" to CompletionStatus.COMPLETED,
                    ),
                sessions = emptyList(),
                settings = defaultSettings,
                companionState =
                    PremiumCompanionState(
                        template = PremiumRuleTemplate.TRADITIONAL,
                        optionalDisciplinesPerWeek = 3,
                        fixedFastWeekday = 6,
                        protectFeastDays = true,
                        seasonProgramStartIso = "2026-03-01T00:00:00Z",
                    ),
                today = LocalDate.of(2026, 3, 10),
            )

        assertThat(snapshot.season).isEqualTo(LiturgicalSeason.LENT)
        assertThat(snapshot.recoveryCoachPlan.title).contains("Ash Wednesday")
        assertThat(snapshot.recoveryCoachPlan.summary.lowercase()).contains("almsgiving")
        assertThat(snapshot.adaptiveRulePlan.title).isEqualTo("Traditional Rule Plan")
        assertThat(snapshot.adaptiveRulePlan.caution.lowercase()).contains("celebration mode")
        assertThat(snapshot.motivationLine).contains("Traditional rule")
        assertThat(snapshot.motivationLine).contains("Streak 2d")
    }

    @Test
    fun subscriptionHealthMatrixPrioritizesCriticalStates() {
        assertThat(
            PremiumSubscriptionHealthEvaluator.message(
                states = listOf(PremiumSubscriptionState.REVOKED),
                premiumUnlocked = true,
            ),
        ).isEqualTo("Subscription was revoked. Restore or update your account.")
        assertThat(
            PremiumSubscriptionHealthEvaluator.message(
                states = listOf(PremiumSubscriptionState.IN_BILLING_RETRY),
                premiumUnlocked = true,
            ),
        ).isEqualTo("Billing issue detected. Update your payment method to keep Premium.")
        assertThat(
            PremiumSubscriptionHealthEvaluator.message(
                states = listOf(PremiumSubscriptionState.IN_GRACE_PERIOD),
                premiumUnlocked = true,
            ),
        ).isEqualTo("You are in billing grace period. Premium remains active for now.")
        assertThat(
            PremiumSubscriptionHealthEvaluator.message(
                states = listOf(PremiumSubscriptionState.EXPIRED),
                premiumUnlocked = false,
            ),
        ).isEqualTo("Premium subscription expired.")
        assertThat(
            PremiumSubscriptionHealthEvaluator.message(
                states = listOf(PremiumSubscriptionState.SUBSCRIBED),
                premiumUnlocked = false,
            ),
        ).isEqualTo("Premium subscription is active.")
        assertThat(
            PremiumSubscriptionHealthEvaluator.message(
                states = emptyList(),
                premiumUnlocked = true,
            ),
        ).isEqualTo("Premium subscription is active.")
        assertThat(
            PremiumSubscriptionHealthEvaluator.message(
                states = emptyList(),
                premiumUnlocked = false,
            ),
        ).isEmpty()
    }

    @Test
    fun subscriptionHealthMatrixUsesPriorityOrderWhenMultipleStatesExist() {
        val revokedMessage =
            PremiumSubscriptionHealthEvaluator.message(
                states =
                    listOf(
                        PremiumSubscriptionState.SUBSCRIBED,
                        PremiumSubscriptionState.EXPIRED,
                        PremiumSubscriptionState.IN_BILLING_RETRY,
                        PremiumSubscriptionState.REVOKED,
                    ),
                premiumUnlocked = true,
            )
        val graceMessage =
            PremiumSubscriptionHealthEvaluator.message(
                states =
                    listOf(
                        PremiumSubscriptionState.EXPIRED,
                        PremiumSubscriptionState.IN_GRACE_PERIOD,
                        PremiumSubscriptionState.SUBSCRIBED,
                    ),
                premiumUnlocked = true,
            )

        assertThat(revokedMessage).isEqualTo("Subscription was revoked. Restore or update your account.")
        assertThat(graceMessage).isEqualTo("You are in billing grace period. Premium remains active for now.")
    }

    private fun testObservance(
        id: String,
        title: String,
        date: String,
        obligation: ObservanceObligation,
    ): Observance =
        Observance(
            id = id,
            title = title,
            date = date,
            kind = ObservanceKind.FEAST_DAY,
            obligation = obligation,
            rationale = "Test rationale",
            citations = emptyList(),
            ruleVersion = "test",
        )
}
