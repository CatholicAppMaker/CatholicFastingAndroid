package com.kevpierce.catholicfasting.core.rules

import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.CompanionActionDestination
import com.kevpierce.catholicfasting.core.model.CompanionActionPriority
import com.kevpierce.catholicfasting.core.model.CompanionFormationState
import com.kevpierce.catholicfasting.core.model.CompanionLiveFastState
import com.kevpierce.catholicfasting.core.model.CompanionNextAction
import com.kevpierce.catholicfasting.core.model.CompanionRuleDecision
import com.kevpierce.catholicfasting.core.model.CompanionSnapshot
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.FastProgressState
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.IntermittentFastSessionRecap
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.RuleSettings
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

object CompanionSnapshotEngine {
    fun build(
        observances: List<Observance>,
        statusesById: Map<String, CompletionStatus>,
        sessions: List<IntermittentFastSession>,
        activeFast: ActiveIntermittentFast?,
        settings: RuleSettings,
        premiumSnapshot: PremiumSnapshot,
        premiumUnlocked: Boolean,
        today: LocalDate = LocalDate.now(),
        now: Instant = Instant.now(),
    ): CompanionSnapshot {
        val todayObservances = observances.filter { it.date == today.toString() }
        val nextRequired = nextRequiredObservance(observances, today)
        val progressState = fastProgressState(activeFast, sessions, settings, now)
        val latestRecap = latestRecap(sessions, settings)
        val recoveryPlan = MissedDayRecoveryEngine.plan(observances, statusesById, today)
        val formation =
            formationState(
                observances = observances,
                statusesById = statusesById,
                premiumSnapshot = premiumSnapshot,
                premiumUnlocked = premiumUnlocked,
                recoverySummary = recoveryPlan?.summaryLine,
                today = today,
            )
        val ruleDecision = ruleDecision(todayObservances, settings)
        val primaryAction =
            primaryAction(
                ruleDecision = ruleDecision,
                progressState = progressState,
                recoverySummary = recoveryPlan?.summaryLine,
                nextRequired = nextRequired,
                hasMedicalDispensation = settings.hasMedicalDispensation,
                premiumUnlocked = premiumUnlocked,
            )

        return CompanionSnapshot(
            generatedAtIso = now.toString(),
            ruleDecision = ruleDecision,
            liveFast =
                CompanionLiveFastState(
                    progress = progressState,
                    activeStartIso = activeFast?.startIso,
                    targetHours = activeFast?.targetHours?.let { max(1, it) },
                    intentionId = activeFast?.intentionId ?: latestRecap?.intentionId,
                    latestSessionRecap = latestRecap,
                ),
            nextRequiredObservance = nextRequired,
            formation = formation,
            primaryAction = primaryAction,
            secondaryActions =
                secondaryActions(
                    primaryAction = primaryAction,
                    nextRequired = nextRequired,
                    premiumUnlocked = premiumUnlocked,
                ),
        )
    }

    fun fastProgressState(
        activeFast: ActiveIntermittentFast?,
        sessions: List<IntermittentFastSession>,
        settings: RuleSettings,
        now: Instant = Instant.now(),
    ): FastProgressState = CompanionFastProgressFactory.state(activeFast, sessions, settings, now)

    fun recapFor(
        session: IntermittentFastSession,
        settings: RuleSettings,
    ): IntermittentFastSessionRecap = CompanionFastProgressFactory.recapFor(session, settings)

    private fun ruleDecision(
        todayObservances: List<Observance>,
        settings: RuleSettings,
    ): CompanionRuleDecision {
        val actionable = todayObservances.filter { it.obligation != ObservanceObligation.NOT_APPLICABLE }
        val mandatory = actionable.filter { it.obligation == ObservanceObligation.MANDATORY }
        val first = mandatory.firstOrNull() ?: actionable.firstOrNull() ?: todayObservances.firstOrNull()
        val obligationLine =
            when {
                settings.hasMedicalDispensation ->
                    "Medical or pastoral dispensation is enabled in your profile."
                mandatory.any { it.kind == ObservanceKind.FAST_AND_ABSTINENCE } ->
                    "Today requires fasting and abstinence."
                mandatory.any { it.kind == ObservanceKind.ABSTINENCE } ->
                    "Today requires abstinence."
                mandatory.isNotEmpty() ->
                    "Today has a required fasting discipline."
                actionable.isNotEmpty() ->
                    "Today has an optional fasting discipline."
                else ->
                    "No mandatory food restriction today."
            }
        val source =
            first
                ?.citations
                ?.firstOrNull()
                ?.let { "${it.authority.label}: ${it.shortReference}" }
                ?: "Source: current Android rule bundle"

        return CompanionRuleDecision(
            obligationLine = obligationLine,
            rationale = first?.rationale ?: "Keep the next required day visible and prepare calmly.",
            sourceLine = source,
            todayTitles = todayObservances.map(Observance::title),
            hasMandatoryObservance = mandatory.isNotEmpty(),
        )
    }

    private fun formationState(
        observances: List<Observance>,
        statusesById: Map<String, CompletionStatus>,
        premiumSnapshot: PremiumSnapshot,
        premiumUnlocked: Boolean,
        recoverySummary: String?,
        today: LocalDate,
    ): CompanionFormationState {
        val required = observances.filter { it.obligation == ObservanceObligation.MANDATORY }
        val completedRequired = required.count { statusesById[it.id]?.countsTowardProgress == true }
        val currentStreak =
            observances
                .filter { LocalDate.parse(it.date) <= today }
                .sortedByDescending { it.date }
                .takeWhile { statusesById[it.id]?.countsTowardProgress == true }
                .count()

        return CompanionFormationState(
            seasonLabel = premiumSnapshot.season.label,
            journeyTitle = premiumSnapshot.seasonPlan.titleLine,
            nextJourneyActionTitle =
                premiumSnapshot.seasonPlan.practices.firstOrNull()
                    ?: premiumSnapshot.reflection.title,
            nextJourneyActionDetail = premiumSnapshot.seasonPlan.focusLine,
            completionSummary = "Required days completed: $completedRequired/${required.size}",
            recoverySummary = recoverySummary,
            currentStreak = currentStreak,
            premiumUnlocked = premiumUnlocked,
        )
    }

    private fun primaryAction(
        ruleDecision: CompanionRuleDecision,
        progressState: FastProgressState,
        recoverySummary: String?,
        nextRequired: Observance?,
        hasMedicalDispensation: Boolean,
        premiumUnlocked: Boolean,
    ): CompanionNextAction =
        when {
            recoverySummary != null -> CompanionActionFactory.recovery(recoverySummary, premiumUnlocked)
            hasMedicalDispensation -> CompanionActionFactory.medicalGuidance()
            ruleDecision.hasMandatoryObservance -> CompanionActionFactory.todayRule(ruleDecision)
            progressState is FastProgressState.Active ||
                progressState is FastProgressState.TargetReached -> CompanionActionFactory.activeFast(progressState)
            progressState is FastProgressState.CompletedRecap -> CompanionActionFactory.fastRecap(progressState)
            nextRequired != null -> CompanionActionFactory.nextRequired(nextRequired)
            else -> CompanionActionFactory.startFast()
        }

    private fun secondaryActions(
        primaryAction: CompanionNextAction,
        nextRequired: Observance?,
        premiumUnlocked: Boolean,
    ): List<CompanionNextAction> =
        listOfNotNull(
            CompanionNextAction(
                id = "track-fast",
                title = "Track a fast",
                detail = "Start, end, or review your intermittent fast.",
                destination = CompanionActionDestination.TRACK_FAST,
            ).takeIf { primaryAction.destination != CompanionActionDestination.TRACK_FAST },
            nextRequired?.let {
                CompanionNextAction(
                    id = "fasting-days",
                    title = "See fasting days",
                    detail = "Next required day: ${it.title}.",
                    destination = CompanionActionDestination.FASTING_DAYS,
                )
            },
            CompanionNextAction(
                id = "formation",
                title =
                    if (premiumUnlocked) {
                        "Open formation plan"
                    } else {
                        "Finish setup"
                    },
                detail =
                    if (premiumUnlocked) {
                        "Continue the seasonal companion plan."
                    } else {
                        "Keep reminders, profile, and first intention aligned."
                    },
                destination =
                    if (premiumUnlocked) {
                        CompanionActionDestination.PREMIUM
                    } else {
                        CompanionActionDestination.SETUP
                    },
            ),
        ).distinctBy(CompanionNextAction::id)

    private fun nextRequiredObservance(
        observances: List<Observance>,
        today: LocalDate,
    ): Observance? =
        observances
            .filter {
                it.obligation == ObservanceObligation.MANDATORY &&
                    !LocalDate.parse(it.date).isBefore(today)
            }.minByOrNull { it.date }

    private fun latestRecap(
        sessions: List<IntermittentFastSession>,
        settings: RuleSettings,
    ): IntermittentFastSessionRecap? = CompanionFastProgressFactory.latestRecap(sessions, settings)
}

private object CompanionFastProgressFactory {
    private const val RECENT_RECAP_HOURS = 2L

    fun state(
        activeFast: ActiveIntermittentFast?,
        sessions: List<IntermittentFastSession>,
        settings: RuleSettings,
        now: Instant,
    ): FastProgressState =
        activeFastProgress(activeFast, now)
            ?: completedFastProgress(sessions, settings, now)

    fun recapFor(
        session: IntermittentFastSession,
        settings: RuleSettings,
    ): IntermittentFastSessionRecap {
        val start = parseInstant(session.startIso)
        val end = parseInstant(session.endIso)
        val durationHours =
            if (start != null && end != null && end.isAfter(start)) {
                Duration.between(start, end).seconds / 3600.0
            } else {
                0.0
            }
        val prudent = settings.hasMedicalDispensation
        val title =
            when {
                prudent -> "Prudent discipline logged"
                session.completedTarget -> "Fast completed"
                else -> "Fast ended early"
            }
        val encouragement =
            when {
                prudent -> "You kept the discipline within medical and pastoral prudence."
                session.completedTarget -> "You reached the target. Receive the fruit quietly and recover well."
                durationHours < 1.0 -> "A short fast can still be offered with humility."
                else -> "You ended before the target. Review, recover, and keep the next step simple."
            }
        val nextAction =
            when {
                prudent -> "Review guidance before the next fast."
                session.completedTarget -> "Log a brief review note or prepare the next required day."
                else -> "Choose a smaller target or a clearer intention next time."
            }

        return IntermittentFastSessionRecap(
            durationHours = durationHours,
            targetHours = session.targetHours,
            completedTarget = session.completedTarget,
            title = title,
            encouragement = encouragement,
            suggestedNextAction = nextAction,
            intentionId = session.intentionId,
            reviewNote = session.reviewNote,
        )
    }

    fun latestRecap(
        sessions: List<IntermittentFastSession>,
        settings: RuleSettings,
    ): IntermittentFastSessionRecap? = latestSession(sessions)?.let { recapFor(it, settings) }

    private fun activeFastProgress(
        activeFast: ActiveIntermittentFast?,
        now: Instant,
    ): FastProgressState? {
        val start = activeFast?.startIso?.let(::parseInstant)
        if (activeFast == null || start == null || !now.isAfter(start)) return null

        val elapsedSeconds = Duration.between(start, now).seconds
        val safeTargetHours = max(1, activeFast.targetHours)
        val targetSeconds = safeTargetHours * 3600L
        val remainingSeconds = max(0L, targetSeconds - elapsedSeconds)

        return if (remainingSeconds == 0L) {
            FastProgressState.TargetReached(
                elapsedSeconds = elapsedSeconds,
                targetHours = safeTargetHours,
            )
        } else {
            FastProgressState.Active(
                elapsedSeconds = elapsedSeconds,
                remainingSeconds = remainingSeconds,
                targetHours = safeTargetHours,
                progress = min(1.0f, elapsedSeconds.toFloat() / targetSeconds.toFloat()),
            )
        }
    }

    private fun completedFastProgress(
        sessions: List<IntermittentFastSession>,
        settings: RuleSettings,
        now: Instant,
    ): FastProgressState =
        latestSession(sessions)?.let { latest ->
            latest.endIso
                .let(::parseInstant)
                ?.let { latestEnd -> completedFastProgress(latest, latestEnd, settings, now) }
        } ?: FastProgressState.Inactive()

    private fun completedFastProgress(
        latest: IntermittentFastSession,
        latestEnd: Instant,
        settings: RuleSettings,
        now: Instant,
    ): FastProgressState {
        val recap = recapFor(latest, settings)
        val sinceEnd = Duration.between(latestEnd, now)
        val eatingWindowHours = max(0, 24 - latest.targetHours)
        val eatingWindowSeconds = eatingWindowHours * 3600L
        val elapsedWindowSeconds = sinceEnd.seconds

        return when {
            !sinceEnd.isNegative && sinceEnd.toHours() < RECENT_RECAP_HOURS ->
                FastProgressState.CompletedRecap(recap)
            elapsedWindowSeconds in 0 until eatingWindowSeconds ->
                FastProgressState.EatingWindow(
                    latestRecap = recap,
                    windowRemainingSeconds = eatingWindowSeconds - elapsedWindowSeconds,
                )
            else -> FastProgressState.Inactive()
        }
    }

    private fun latestSession(sessions: List<IntermittentFastSession>): IntermittentFastSession? =
        sessions.maxByOrNull { parseInstant(it.endIso)?.epochSecond ?: Long.MIN_VALUE }

    private fun parseInstant(value: String): Instant? = runCatching { Instant.parse(value) }.getOrNull()
}

private object CompanionActionFactory {
    fun recovery(
        recoverySummary: String,
        premiumUnlocked: Boolean,
    ) = CompanionNextAction(
        id = "recovery",
        title = "Repair the missed day",
        detail = recoverySummary,
        destination =
            if (premiumUnlocked) {
                CompanionActionDestination.PREMIUM
            } else {
                CompanionActionDestination.FASTING_DAYS
            },
        priority = CompanionActionPriority.HIGH,
        requiresPremium = false,
    )

    fun medicalGuidance() =
        CompanionNextAction(
            id = "medical-guidance",
            title = "Review prudent guidance",
            detail = "Your profile marks medical dispensation. Keep health and pastoral counsel first.",
            destination = CompanionActionDestination.GUIDANCE,
            priority = CompanionActionPriority.HIGH,
        )

    fun todayRule(ruleDecision: CompanionRuleDecision) =
        CompanionNextAction(
            id = "today-rule",
            title = "Review today's rule",
            detail = ruleDecision.rationale,
            destination = CompanionActionDestination.GUIDANCE,
            priority = CompanionActionPriority.HIGH,
        )

    fun activeFast(progressState: FastProgressState) =
        CompanionNextAction(
            id = "active-fast",
            title = "Continue your active fast",
            detail = progressState.detail,
            destination = CompanionActionDestination.TRACK_FAST,
            priority = CompanionActionPriority.HIGH,
        )

    fun fastRecap(progressState: FastProgressState.CompletedRecap) =
        CompanionNextAction(
            id = "fast-recap",
            title = "Review your fast recap",
            detail = progressState.recap.suggestedNextAction,
            destination = CompanionActionDestination.TRACK_FAST,
            priority = CompanionActionPriority.NORMAL,
        )

    fun nextRequired(nextRequired: Observance) =
        CompanionNextAction(
            id = "next-required",
            title = "Prepare ${nextRequired.title}",
            detail = nextRequired.detail ?: nextRequired.rationale,
            destination = CompanionActionDestination.FASTING_DAYS,
            priority = CompanionActionPriority.NORMAL,
        )

    fun startFast() =
        CompanionNextAction(
            id = "start-fast",
            title = "Begin an intentional fast",
            detail = "Choose a target and intention when you are ready.",
            destination = CompanionActionDestination.TRACK_FAST,
            priority = CompanionActionPriority.NORMAL,
        )
}
