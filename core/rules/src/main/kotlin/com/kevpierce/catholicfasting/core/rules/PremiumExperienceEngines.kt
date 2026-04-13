@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfasting.core.rules

import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import com.kevpierce.catholicfasting.core.model.MissedDayRecoveryPlan
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.PremiumAdaptiveRulePlan
import com.kevpierce.catholicfasting.core.model.PremiumAnalyticsSummary
import com.kevpierce.catholicfasting.core.model.PremiumCompanionState
import com.kevpierce.catholicfasting.core.model.PremiumConditionRules
import com.kevpierce.catholicfasting.core.model.PremiumRecoveryCoachPlan
import com.kevpierce.catholicfasting.core.model.PremiumReflection
import com.kevpierce.catholicfasting.core.model.PremiumReminderRecommendation
import com.kevpierce.catholicfasting.core.model.PremiumRuleTemplate
import com.kevpierce.catholicfasting.core.model.PremiumSeasonCompletionRow
import com.kevpierce.catholicfasting.core.model.PremiumSeasonPlan
import com.kevpierce.catholicfasting.core.model.PremiumSeasonProgram
import com.kevpierce.catholicfasting.core.model.PremiumSubscriptionState
import com.kevpierce.catholicfasting.core.model.RuleSettings
import java.text.DateFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

object PremiumSeasonPlanEngine {
    fun plan(
        season: LiturgicalSeason,
        settings: RuleSettings,
    ): PremiumSeasonPlan {
        if (settings.hasMedicalDispensation) {
            return medicalPlan()
        }

        return when (season) {
            LiturgicalSeason.ADVENT -> adventPlan()
            LiturgicalSeason.CHRISTMAS -> christmasPlan()
            LiturgicalSeason.LENT -> lentPlan()
            LiturgicalSeason.EASTER -> easterPlan()
            LiturgicalSeason.ORDINARY -> ordinaryPlan()
        }
    }
}

object PremiumReminderPlanner {
    fun recommendation(
        observances: List<Observance>,
        statusesById: Map<String, CompletionStatus>,
        now: LocalDate = LocalDate.now(),
    ): PremiumReminderRecommendation {
        val recentWindowStart = now.minusDays(30)
        val upcomingWindowEnd = now.plusDays(14)

        val recent =
            observances.filter { observance ->
                val day = LocalDate.parse(observance.date)
                day >= recentWindowStart &&
                    day <= now &&
                    observance.obligation != ObservanceObligation.NOT_APPLICABLE
            }
        val upcomingRequired =
            observances.filter { observance ->
                val day = LocalDate.parse(observance.date)
                day >= now &&
                    day <= upcomingWindowEnd &&
                    observance.obligation == ObservanceObligation.MANDATORY
            }

        val completedRecent = recent.count { statusesById[it.id]?.countsTowardProgress == true }
        val missedRecent = recent.count { statusesById[it.id] == CompletionStatus.MISSED }
        val completionRate =
            if (recent.isEmpty()) {
                1.0
            } else {
                completedRecent.toDouble() / recent.size.toDouble()
            }
        return when {
            missedRecent >= 2 || completionRate < 0.65 ->
                PremiumReminderRecommendation(
                    shouldEnableDailySupport = true,
                    shouldEnableMorning = true,
                    shouldEnableEvening = true,
                    summaryLine =
                        "Recovery mode: enable both morning and evening reminders for the next 2 weeks.",
                )
            upcomingRequired.isNotEmpty() ->
                PremiumReminderRecommendation(
                    shouldEnableDailySupport = true,
                    shouldEnableMorning = true,
                    shouldEnableEvening = false,
                    summaryLine =
                        "Preparation mode: keep morning reminders on for upcoming required observances.",
                )
            else ->
                PremiumReminderRecommendation(
                    shouldEnableDailySupport = true,
                    shouldEnableMorning = false,
                    shouldEnableEvening = true,
                    summaryLine =
                        "Maintenance mode: evening examen reminders are enough for your current rhythm.",
                )
        }
    }
}

object PremiumAnalyticsEngine {
    fun summary(
        observances: List<Observance>,
        statusesById: Map<String, CompletionStatus>,
        sessions: List<IntermittentFastSession>,
    ): PremiumAnalyticsSummary {
        val required = observances.filter { it.obligation == ObservanceObligation.MANDATORY }
        val actionable = observances.filter { it.obligation != ObservanceObligation.NOT_APPLICABLE }
        val requiredCompleted = required.count { statusesById[it.id]?.countsTowardProgress == true }
        val actionableCompleted = actionable.count { statusesById[it.id]?.countsTowardProgress == true }
        val missedCount = statusesById.values.count { it == CompletionStatus.MISSED }
        val substitutedCount = statusesById.values.count { it == CompletionStatus.SUBSTITUTED }

        val recentSessions = sessions.take(30)
        val hitTarget = recentSessions.count(IntermittentFastSession::completedTarget)
        val intermittentHitPercent =
            if (recentSessions.isEmpty()) {
                0
            } else {
                ((hitTarget.toDouble() / recentSessions.size.toDouble()) * 100.0).roundToInt()
            }

        val seasonalTotals = mutableMapOf<LiturgicalSeason, Pair<Int, Int>>()
        actionable.forEach { observance ->
            val season = LiturgicalSeasonThemeEngine.seasonFor(LocalDate.parse(observance.date))
            val existing = seasonalTotals[season] ?: (0 to 0)
            val completed = if (statusesById[observance.id]?.countsTowardProgress == true) 1 else 0
            seasonalTotals[season] = (existing.first + completed) to (existing.second + 1)
        }

        val seasonRows =
            listOf(
                LiturgicalSeason.ADVENT,
                LiturgicalSeason.CHRISTMAS,
                LiturgicalSeason.LENT,
                LiturgicalSeason.EASTER,
                LiturgicalSeason.ORDINARY,
            ).mapNotNull { season ->
                seasonalTotals[season]?.let { counts ->
                    PremiumSeasonCompletionRow(
                        id = season.name,
                        season = season,
                        completedCount = counts.first,
                        totalCount = counts.second,
                    )
                }
            }

        return PremiumAnalyticsSummary(
            requiredCompletionPercent = percent(requiredCompleted, required.size),
            overallCompletionPercent = percent(actionableCompleted, actionable.size),
            missedCount = missedCount,
            substitutedCount = substitutedCount,
            intermittentTargetHitPercent = intermittentHitPercent,
            seasonRows = seasonRows,
        )
    }
}

object PremiumReflectionEngine {
    fun reflection(
        date: LocalDate = LocalDate.now(),
        season: LiturgicalSeason,
    ): PremiumReflection {
        val options = reflectionsFor(season)
        val index = (date.dayOfYear - 1) % options.size
        return options[index]
    }
}

object PremiumDirectionSummaryEngine {
    fun summaryText(
        date: Instant = Instant.now(),
        season: LiturgicalSeason,
        analytics: PremiumAnalyticsSummary,
        reminder: PremiumReminderRecommendation,
        plan: PremiumSeasonPlan,
        latestReflection: PremiumReflection,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String {
        val generatedAt =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(zoneId)
                .format(date)

        return listOf(
            "Catholic Fasting Premium Summary",
            "Generated: $generatedAt",
            "",
            "Season",
            "- ${season.label}",
            "- Plan: ${plan.titleLine}",
            "- Focus: ${plan.focusLine}",
            "- Intensity: ${plan.fastingIntensity}",
            "",
            "Discipline Metrics",
            "- Required completion: ${analytics.requiredCompletionPercent}%",
            "- Overall completion: ${analytics.overallCompletionPercent}%",
            "- Missed observances logged: ${analytics.missedCount}",
            "- Substituted observances logged: ${analytics.substitutedCount}",
            "- Intermittent target hit rate (recent): ${analytics.intermittentTargetHitPercent}%",
            "",
            "Reminder Strategy",
            "- Daily support: ${enabledLine(reminder.shouldEnableDailySupport)}",
            "- Morning reminder: ${enabledLine(reminder.shouldEnableMorning)}",
            "- Evening reminder: ${enabledLine(reminder.shouldEnableEvening)}",
            "- Guidance: ${reminder.summaryLine}",
            "",
            "Reflection",
            "- ${latestReflection.title}",
            "- ${latestReflection.body}",
            "- Action: ${latestReflection.action}",
        ).joinToString(separator = "\n")
    }
}

object PremiumAdaptiveRulePlanner {
    fun plan(
        season: LiturgicalSeason,
        settings: RuleSettings,
        template: PremiumRuleTemplate,
        optionalDisciplinesPerWeek: Int,
        fixedFastWeekday: Int,
        protectFeastDays: Boolean,
    ): PremiumAdaptiveRulePlan {
        val baseSeasonLine =
            when (season) {
                LiturgicalSeason.ADVENT -> "Advent emphasis: watchfulness and simplicity."
                LiturgicalSeason.CHRISTMAS -> "Christmas emphasis: grateful moderation."
                LiturgicalSeason.LENT -> "Lent emphasis: repentance and sustained sacrifice."
                LiturgicalSeason.EASTER -> "Easter emphasis: preserve gains from Lent."
                LiturgicalSeason.ORDINARY -> "Ordinary Time emphasis: steady fidelity."
            }

        if (settings.hasMedicalDispensation) {
            return PremiumAdaptiveRulePlan(
                title = "Moderated Rule of Life",
                summary = "$baseSeasonLine Keep food discipline medically safe and pastorally guided.",
                weeklyActions =
                    listOf(
                        "Anchor one stable prayer block daily.",
                        "Choose one practical charity act each week.",
                        "Use non-food substitute penance when needed.",
                    ),
                caution = "Health and pastoral guidance take priority over rigor.",
            )
        }

        val intensity = optionalDisciplinesPerWeek.coerceIn(0, 7)
        val templateLine = "${template.label} template with $intensity optional discipline(s)/week."
        val feastLine =
            if (protectFeastDays) {
                "Feast/holy days switch to celebration mode automatically."
            } else {
                "Feast/holy days are shown, but your personal disciplines remain user-controlled."
            }

        return PremiumAdaptiveRulePlan(
            title = "${template.label} Rule Plan",
            summary = "$baseSeasonLine $templateLine",
            weeklyActions =
                listOf(
                    "Primary personal fast day: ${weekdayName(fixedFastWeekday)}.",
                    "Optional disciplines this week: $intensity.",
                    "Review completion each Sunday evening and adjust the next week.",
                ),
            caution = feastLine,
        )
    }
}

object PremiumConditionReminderAdvisor {
    fun applyRules(
        rules: PremiumConditionRules,
        hasUpcomingRequiredDays: Boolean,
    ): PremiumReminderRecommendation {
        return when {
            rules.requiredDaysDoubleReminder && hasUpcomingRequiredDays ->
                PremiumReminderRecommendation(
                    shouldEnableDailySupport = true,
                    shouldEnableMorning = true,
                    shouldEnableEvening = true,
                    summaryLine = "Condition rules enabled: required-day double reminders are active.",
                )
            rules.remindIfUnloggedByNoon ->
                PremiumReminderRecommendation(
                    shouldEnableDailySupport = true,
                    shouldEnableMorning = true,
                    shouldEnableEvening = false,
                    summaryLine = "Condition rules enabled: noon check-in recovery reminders are active.",
                )
            else ->
                PremiumReminderRecommendation(
                    shouldEnableDailySupport = true,
                    shouldEnableMorning = false,
                    shouldEnableEvening = true,
                    summaryLine = "Condition rules enabled: evening examen support is active.",
                )
        }
    }
}

object MissedDayRecoveryEngine {
    fun plan(
        observances: List<Observance>,
        statusesById: Map<String, CompletionStatus>,
        today: LocalDate = LocalDate.now(),
    ): MissedDayRecoveryPlan? {
        val missedObservances =
            observances.filter { observance ->
                statusesById[observance.id] == CompletionStatus.MISSED &&
                    LocalDate.parse(observance.date) <= today
            }
        val lastMissed =
            missedObservances.maxByOrNull { LocalDate.parse(it.date) }
                ?: return null

        val nextRequired =
            observances
                .filter { it.obligation == ObservanceObligation.MANDATORY }
                .firstOrNull { LocalDate.parse(it.date) > today }

        val nextRequiredLine =
            if (nextRequired != null) {
                "Next required day: ${nextRequired.title} on ${shortDate(LocalDate.parse(nextRequired.date))}."
            } else {
                "No future required observances remain in this calendar year."
            }

        return MissedDayRecoveryPlan(
            titleLine =
                "Recent missed observance: ${lastMissed.title} (${shortDate(LocalDate.parse(lastMissed.date))}).",
            summaryLine = "Missing a day does not end your discipline. Recover with a practical next step today.",
            steps =
                listOf(
                    "Offer a short prayer of repentance and renew your intention.",
                    "Choose one concrete recovery act today (charity, Scripture, Rosary, or a simplified meal).",
                    "Plan the next required day now so it is easier to keep.",
                ),
            nextRequiredLine = nextRequiredLine,
        )
    }
}

object PremiumRecoveryCoachEngine {
    fun plan(
        missedPlan: MissedDayRecoveryPlan?,
        season: LiturgicalSeason,
    ): PremiumRecoveryCoachPlan {
        if (missedPlan == null) {
            return PremiumRecoveryCoachPlan(
                title = "Recovery Stable",
                summary = "No current missed-day alert. Stay proactive this week.",
                steps =
                    listOf(
                        "Review the next required observance date.",
                        "Keep your fixed personal fast day.",
                        "Close today with a one-minute examen.",
                    ),
            )
        }

        val seasonalAction =
            when (season) {
                LiturgicalSeason.LENT -> "Pair recovery with concrete almsgiving."
                LiturgicalSeason.ADVENT -> "Pair recovery with quiet watchfulness prayer."
                LiturgicalSeason.EASTER -> "Pair recovery with one mercy action."
                LiturgicalSeason.CHRISTMAS -> "Pair recovery with gratitude prayer after meals."
                LiturgicalSeason.ORDINARY -> "Pair recovery with faithful Friday penance."
            }

        return PremiumRecoveryCoachPlan(
            title = missedPlan.titleLine,
            summary = "${missedPlan.summaryLine} $seasonalAction",
            steps = missedPlan.steps + missedPlan.nextRequiredLine,
        )
    }
}

object PremiumSeasonProgramEngine {
    fun actions(
        program: PremiumSeasonProgram,
        week: Int,
    ): List<String> {
        val normalizedWeek = week.coerceAtLeast(1)
        return when (program) {
            PremiumSeasonProgram.LITURGICAL_RHYTHM ->
                listOf(
                    "Pray before first meal each day.",
                    "Keep one fixed weekday discipline.",
                    "Weekly review checkpoint #$normalizedWeek.",
                )
            PremiumSeasonProgram.LENT_DEEPEN ->
                listOf(
                    "Keep all required observances with planning.",
                    "Add one hidden sacrifice this week.",
                    "Link fasting to almsgiving checkpoint #$normalizedWeek.",
                )
            PremiumSeasonProgram.ADVENT_WATCH ->
                listOf(
                    "Reduce one comfort item for watchfulness.",
                    "Add a short Scripture reading before dinner.",
                    "Keep a quiet-night prayer checkpoint #$normalizedWeek.",
                )
            PremiumSeasonProgram.FRIDAY_FIDELITY ->
                listOf(
                    "Plan Friday penance by Thursday evening.",
                    "Record one charity action on Friday.",
                    "End Friday with a gratitude examen checkpoint #$normalizedWeek.",
                )
        }
    }
}

object PremiumFastPrepGuidanceEngine {
    fun prepAndRefeed(
        targetHours: Int,
        hasMedicalDispensation: Boolean,
    ): List<String> {
        if (hasMedicalDispensation) {
            return listOf(
                "Prep: choose medically safe meals and hydration.",
                "During: prioritize stability and avoid unsafe restriction.",
                "Refeed: return to normal meals gradually as advised.",
            )
        }

        return when {
            targetHours <= 18 ->
                listOf(
                    "Prep: hydrate and simplify your final meal.",
                    "During: keep prayer cues tied to hunger moments.",
                    "Refeed: break with moderate portions and protein/fiber.",
                )
            targetHours <= 36 ->
                listOf(
                    "Prep: increase hydration the day before.",
                    "During: keep intensity moderate and avoid overexertion.",
                    "Refeed: start light, then full meal after 30-60 minutes.",
                )
            else ->
                listOf(
                    "Prep: plan schedule, hydration, and pastoral prudence.",
                    "During: monitor energy and stop if health concerns arise.",
                    "Refeed: start very gently, then normalize in stages.",
                )
        }
    }
}

object PremiumMotivationEngine {
    fun line(
        season: LiturgicalSeason,
        streak: Int,
        template: PremiumRuleTemplate,
    ): String {
        val seasonPhrase =
            when (season) {
                LiturgicalSeason.ADVENT -> "Watch with hope"
                LiturgicalSeason.CHRISTMAS -> "Celebrate with gratitude"
                LiturgicalSeason.LENT -> "Repent with discipline"
                LiturgicalSeason.EASTER -> "Persevere in new life"
                LiturgicalSeason.ORDINARY -> "Stay faithful in the ordinary"
            }
        return "$seasonPhrase • ${template.label} rule • Streak ${streak}d"
    }
}

object PremiumSubscriptionHealthEvaluator {
    fun message(
        states: List<PremiumSubscriptionState>,
        premiumUnlocked: Boolean,
    ): String {
        return when {
            PremiumSubscriptionState.REVOKED in states ->
                "Subscription was revoked. Restore or update your account."
            PremiumSubscriptionState.IN_BILLING_RETRY in states ->
                "Billing issue detected. Update your payment method to keep Premium."
            PremiumSubscriptionState.IN_GRACE_PERIOD in states ->
                "You are in billing grace period. Premium remains active for now."
            PremiumSubscriptionState.EXPIRED in states ->
                "Premium subscription expired."
            PremiumSubscriptionState.SUBSCRIBED in states || premiumUnlocked ->
                "Premium subscription is active."
            else -> ""
        }
    }
}

data class PremiumSnapshot(
    val season: LiturgicalSeason,
    val seasonPlan: PremiumSeasonPlan,
    val reminderRecommendation: PremiumReminderRecommendation,
    val analyticsSummary: PremiumAnalyticsSummary,
    val reflection: PremiumReflection,
    val adaptiveRulePlan: PremiumAdaptiveRulePlan,
    val recoveryCoachPlan: PremiumRecoveryCoachPlan,
    val motivationLine: String,
)

object PremiumSnapshotEngine {
    fun build(
        observances: List<Observance>,
        statusesById: Map<String, CompletionStatus>,
        sessions: List<IntermittentFastSession>,
        settings: RuleSettings,
        companionState: PremiumCompanionState,
        today: LocalDate = LocalDate.now(),
    ): PremiumSnapshot {
        val season = LiturgicalSeasonThemeEngine.seasonFor(today)
        val analyticsSummary = PremiumAnalyticsEngine.summary(observances, statusesById, sessions)
        val reminderRecommendation = PremiumReminderPlanner.recommendation(observances, statusesById, today)
        val seasonPlan = PremiumSeasonPlanEngine.plan(season, settings)
        val reflection = PremiumReflectionEngine.reflection(today, season)
        val missedPlan = MissedDayRecoveryEngine.plan(observances, statusesById, today)
        val adaptiveRulePlan =
            PremiumAdaptiveRulePlanner.plan(
                season = season,
                settings = settings,
                template = companionState.template,
                optionalDisciplinesPerWeek = companionState.optionalDisciplinesPerWeek,
                fixedFastWeekday = companionState.fixedFastWeekday,
                protectFeastDays = companionState.protectFeastDays,
            )
        val recoveryCoachPlan = PremiumRecoveryCoachEngine.plan(missedPlan, season)
        val streak =
            observances
                .filter { LocalDate.parse(it.date) <= today }
                .sortedByDescending { it.date }
                .takeWhile { statusesById[it.id]?.countsTowardProgress == true }
                .count()

        return PremiumSnapshot(
            season = season,
            seasonPlan = seasonPlan,
            reminderRecommendation = reminderRecommendation,
            analyticsSummary = analyticsSummary,
            reflection = reflection,
            adaptiveRulePlan = adaptiveRulePlan,
            recoveryCoachPlan = recoveryCoachPlan,
            motivationLine = PremiumMotivationEngine.line(season, streak, companionState.template),
        )
    }
}

private fun percent(
    done: Int,
    total: Int,
): Int {
    if (total <= 0) {
        return 0
    }
    return ((done.toDouble() / total.toDouble()) * 100.0).roundToInt()
}

private fun enabledLine(enabled: Boolean): String = if (enabled) "On" else "Off"

private fun weekdayName(weekday: Int): String = DateFormatSymbols(Locale.getDefault()).weekdays[weekday.coerceIn(1, 7)]

private fun shortDate(date: LocalDate): String =
    date.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.US),
    )

private fun reflectionsFor(season: LiturgicalSeason): List<PremiumReflection> =
    when (season) {
        LiturgicalSeason.ADVENT -> adventReflections()
        LiturgicalSeason.CHRISTMAS -> christmasReflections()
        LiturgicalSeason.LENT -> lentReflections()
        LiturgicalSeason.EASTER -> easterReflections()
        LiturgicalSeason.ORDINARY -> ordinaryReflections()
    }

private fun medicalPlan(): PremiumSeasonPlan =
    PremiumSeasonPlan(
        titleLine = "Medical/Pastoral Plan",
        focusLine = "Use a moderated discipline with your pastor's guidance.",
        practices =
            listOf(
                "Keep a fixed morning and evening prayer rhythm.",
                "Choose one charitable act each week.",
                "Use food discipline only as health allows.",
            ),
        fastingIntensity = "Gentle",
    )

private fun adventPlan(): PremiumSeasonPlan =
    PremiumSeasonPlan(
        titleLine = "Advent Preparation Plan",
        focusLine = "Watchfulness, restraint, and expectation of the Lord.",
        practices =
            listOf(
                "Fast lightly on Wednesdays and Fridays.",
                "Add one weekday Mass when possible.",
                "Set one concrete almsgiving commitment.",
            ),
        fastingIntensity = "Moderate",
    )

private fun christmasPlan(): PremiumSeasonPlan =
    PremiumSeasonPlan(
        titleLine = "Christmas Joy Plan",
        focusLine = "Celebrate with gratitude while keeping sobriety.",
        practices =
            listOf(
                "Keep Friday penance with deliberate charity.",
                "Pray a brief thanksgiving after each meal.",
                "Avoid unnecessary excess for one chosen category.",
            ),
        fastingIntensity = "Light",
    )

private fun lentPlan(): PremiumSeasonPlan =
    PremiumSeasonPlan(
        titleLine = "Lenten Discipline Plan",
        focusLine = "Repentance, conversion, and generous self-denial.",
        practices =
            listOf(
                "Observe all required fast/abstinence days with planning.",
                "Keep one additional personal fast each week.",
                "Pair every fast with prayer and almsgiving.",
            ),
        fastingIntensity = "Strong",
    )

private fun easterPlan(): PremiumSeasonPlan =
    PremiumSeasonPlan(
        titleLine = "Easter Fidelity Plan",
        focusLine = "Sustain the fruits of Lent with steady habits.",
        practices =
            listOf(
                "Maintain Friday penance without interruption.",
                "Offer one act of encouragement or mercy weekly.",
                "Review your rule of life every Sunday evening.",
            ),
        fastingIntensity = "Light",
    )

private fun ordinaryPlan(): PremiumSeasonPlan =
    PremiumSeasonPlan(
        titleLine = "Ordinary Time Rule of Life",
        focusLine = "Consistency in ordinary days forms long-term holiness.",
        practices =
            listOf(
                "Choose a fixed weekly fasting day.",
                "Keep Friday penance intentionally.",
                "Track completion and review each weekend.",
            ),
        fastingIntensity = "Moderate",
    )

private fun adventReflections(): List<PremiumReflection> =
    listOf(
        PremiumReflection(
            title = "Watch in Hope",
            body = "Advent fasting prepares the heart by making room for Christ's coming.",
            action = "Keep one hidden act of restraint today.",
        ),
        PremiumReflection(
            title = "Quiet Expectation",
            body = "Silence and simplicity sharpen spiritual attention.",
            action = "Add 10 minutes of silent prayer before your next meal.",
        ),
    )

private fun christmasReflections(): List<PremiumReflection> =
    listOf(
        PremiumReflection(
            title = "Receive with Gratitude",
            body = "Feasting and fasting both become holy through thanksgiving.",
            action = "Pray a short thanksgiving after each meal today.",
        ),
        PremiumReflection(
            title = "Joy with Sobriety",
            body = "Christian joy does not require excess.",
            action = "Choose one concrete moderation in food or drink today.",
        ),
    )

private fun lentReflections(): List<PremiumReflection> =
    listOf(
        PremiumReflection(
            title = "Return to the Lord",
            body = "Fasting without prayer becomes technique; with prayer it becomes conversion.",
            action = "Pair your next hunger moment with a brief prayer of repentance.",
        ),
        PremiumReflection(
            title = "Offer the Sacrifice",
            body = "A faithful small sacrifice is better than a dramatic one you cannot sustain.",
            action = "Select one realistic discipline to keep through this week.",
        ),
    )

private fun easterReflections(): List<PremiumReflection> =
    listOf(
        PremiumReflection(
            title = "Persevere in New Life",
            body = "Easter discipline protects the grace you received in Lent.",
            action = "Renew your Friday penance plan for this week.",
        ),
        PremiumReflection(
            title = "Witness in Charity",
            body = "Resurrection joy bears fruit through mercy toward others.",
            action = "Choose one specific act of mercy today.",
        ),
    )

private fun ordinaryReflections(): List<PremiumReflection> =
    listOf(
        PremiumReflection(
            title = "Sanctify the Ordinary",
            body = "Ordinary Time is where fidelity becomes character.",
            action = "Keep your chosen discipline exactly as planned today.",
        ),
        PremiumReflection(
            title = "Small Daily Yes",
            body = "Steady obedience in little things forms long-term freedom.",
            action = "End today with a two-minute examen on your fasting intention.",
        ),
    )
