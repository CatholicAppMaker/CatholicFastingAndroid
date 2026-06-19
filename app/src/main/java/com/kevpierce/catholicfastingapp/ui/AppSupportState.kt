package com.kevpierce.catholicfastingapp.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.kevpierce.catholicfasting.core.data.buildOnboardingState
import com.kevpierce.catholicfasting.core.data.buildReminderCenterState
import com.kevpierce.catholicfasting.core.data.buildSeasonalHeroState
import com.kevpierce.catholicfasting.core.data.buildSetupProgressState
import com.kevpierce.catholicfasting.core.data.buildStorageDiagnosticsState
import com.kevpierce.catholicfasting.core.data.buildSyncSnapshot
import com.kevpierce.catholicfasting.core.data.buildWidgetSnapshot
import com.kevpierce.catholicfasting.core.model.CatholicFastingQuote
import com.kevpierce.catholicfasting.core.model.CompanionSnapshot
import com.kevpierce.catholicfasting.core.model.ContentLocale
import com.kevpierce.catholicfasting.core.model.OnboardingState
import com.kevpierce.catholicfasting.core.model.ReminderCenterState
import com.kevpierce.catholicfasting.core.model.RuleBundleAudit
import com.kevpierce.catholicfasting.core.model.SacredImageryItem
import com.kevpierce.catholicfasting.core.model.SeasonalContentPack
import com.kevpierce.catholicfasting.core.model.SeasonalHeroState
import com.kevpierce.catholicfasting.core.model.SetupProgressState
import com.kevpierce.catholicfasting.core.model.StorageDiagnosticsState
import com.kevpierce.catholicfasting.core.model.SyncSnapshot
import com.kevpierce.catholicfasting.core.rules.CompanionSnapshotEngine
import com.kevpierce.catholicfasting.core.rules.ObservanceCalculator
import com.kevpierce.catholicfasting.core.rules.PremiumFastPrepGuidanceEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSeasonProgramEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshotEngine
import com.kevpierce.catholicfasting.core.rules.SacredImageryCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentPackCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentSupport
import com.kevpierce.catholicfasting.core.widget.WidgetSnapshotStore
import com.kevpierce.catholicfastingapp.R
import com.kevpierce.catholicfastingapp.notifications.IntermittentFastNotificationManager
import com.kevpierce.catholicfastingapp.notifications.ReminderScheduler
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

internal data class AppSupportState(
    val premiumSnapshot: PremiumSnapshot,
    val companionSnapshot: CompanionSnapshot,
    val seasonProgramActions: List<String>,
    val fastPrepGuidance: List<String>,
    val syncSnapshot: SyncSnapshot,
    val onboardingState: OnboardingState,
    val setupProgressState: SetupProgressState,
    val reminderCenterState: ReminderCenterState,
    val storageDiagnosticsState: StorageDiagnosticsState,
    val seasonalHeroState: SeasonalHeroState,
    val ruleBundleAudit: RuleBundleAudit,
    val contentLocale: ContentLocale,
    val seasonalContentPack: SeasonalContentPack,
    val dailyFormationLine: String,
    val dailyQuote: CatholicFastingQuote,
    val devotionalGallery: List<SacredImageryItem>,
    val setupProgressSummary: String,
    val yearPlanSummary: String,
    val weeklyRecap: String,
    val streakMessage: String,
)

@Composable
internal fun AppRuntimeEffects(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
) {
    LaunchedEffect(state.activeIntermittentFast, state.intermittentPresetHours) {
        IntermittentFastNotificationManager.syncActiveFast(
            context = context,
            startIso = state.activeIntermittentFast?.startIso,
            targetHours = state.activeIntermittentFast?.targetHours ?: state.intermittentPresetHours,
        )
    }

    LaunchedEffect(state) {
        ReminderScheduler.sync(context, state)
        WidgetSnapshotStore.persist(context, state.buildWidgetSnapshot())
    }
}

internal fun buildAppSupportState(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    premiumUnlocked: Boolean,
): AppSupportState =
    premiumSnapshot(state).let { snapshot ->
        val locale = currentContentLocale()
        val seasonalPack =
            SeasonalContentPackCatalog.pack(
                season = snapshot.season,
                locale = locale,
            )
        AppSupportState(
            premiumSnapshot = snapshot,
            companionSnapshot =
                CompanionSnapshotEngine.build(
                    observances = state.observances,
                    statusesById = state.statusesById,
                    sessions = state.intermittentSessions,
                    activeFast = state.activeIntermittentFast,
                    settings = state.settings,
                    premiumSnapshot = snapshot,
                    premiumUnlocked = premiumUnlocked,
                ),
            seasonProgramActions =
                PremiumSeasonProgramEngine.actions(
                    program = state.premiumCompanionState.seasonProgram,
                    week = seasonProgramWeek(state.premiumCompanionState.seasonProgramStartIso),
                ),
            fastPrepGuidance =
                PremiumFastPrepGuidanceEngine.prepAndRefeed(
                    targetHours = state.intermittentPresetHours,
                    hasMedicalDispensation = state.settings.hasMedicalDispensation,
                ),
            syncSnapshot = buildSyncSnapshot(state),
            onboardingState = buildOnboardingState(state),
            setupProgressState = buildSetupProgressState(state),
            reminderCenterState = buildReminderCenterState(state),
            storageDiagnosticsState = buildStorageDiagnosticsState(state),
            seasonalHeroState = buildSeasonalHeroState(),
            ruleBundleAudit = ObservanceCalculator.ruleBundleAudit(),
            contentLocale = locale,
            seasonalContentPack = seasonalPack,
            dailyFormationLine =
                SeasonalContentSupport.dailyFormationLine(
                    pack = seasonalPack,
                    date = LocalDate.now(),
                ),
            dailyQuote =
                SeasonalContentSupport.dailyQuote(
                    season = snapshot.season,
                    pack = seasonalPack,
                    date = LocalDate.now(),
                ),
            devotionalGallery = SacredImageryCatalog.fastingGallery,
            setupProgressSummary = setupProgressSummary(context, state),
            yearPlanSummary = yearPlanSummary(context, state),
            weeklyRecap = weeklyRecap(context, state),
            streakMessage = streakMessage(context, state),
        )
    }

private fun premiumSnapshot(state: com.kevpierce.catholicfasting.core.data.DashboardState): PremiumSnapshot =
    PremiumSnapshotEngine.build(
        observances = state.observances,
        statusesById = state.statusesById,
        sessions = state.intermittentSessions,
        settings = state.settings,
        companionState = state.premiumCompanionState,
        today = LocalDate.now(),
    )

private fun seasonProgramWeek(startIso: String): Int {
    val startedAt = runCatching { Instant.parse(startIso) }.getOrNull() ?: return 1
    val days = ChronoUnit.DAYS.between(startedAt, Instant.now())
    return (days / 7L).toInt() + 1
}

private fun currentContentLocale(): ContentLocale =
    when {
        Locale.getDefault().language.startsWith("es") -> ContentLocale.SPANISH
        Locale.getDefault().language.startsWith("fr") -> ContentLocale.FRENCH_CANADIAN
        else -> ContentLocale.ENGLISH
    }

private fun setupProgressSummary(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
): String {
    val progressState = buildSetupProgressState(state)
    return context.getString(
        R.string.summary_setup_progress_value,
        progressState.completedSteps,
        progressState.totalSteps,
    )
}

private fun yearPlanSummary(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
): String {
    val required =
        state.observances.count {
            it.obligation == com.kevpierce.catholicfasting.core.model.ObservanceObligation.MANDATORY &&
                (state.statusesById[it.id]?.countsTowardProgress == true)
        }
    val optional =
        state.observances.count {
            it.obligation == com.kevpierce.catholicfasting.core.model.ObservanceObligation.OPTIONAL &&
                (state.statusesById[it.id]?.countsTowardProgress == true)
        }
    return context.getString(
        R.string.summary_year_plan_value,
        required,
        state.planningData.requiredGoal,
        optional,
        state.planningData.optionalGoal,
    )
}

private fun weeklyRecap(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
): String {
    val weekStart = LocalDate.now().minusDays(6)
    val weeklyActionable =
        state.observances.filter {
            val date = LocalDate.parse(it.date)
            date >= weekStart &&
                date <= LocalDate.now() &&
                it.obligation != com.kevpierce.catholicfasting.core.model.ObservanceObligation.NOT_APPLICABLE
        }
    val completed =
        weeklyActionable.count { observance ->
            state.statusesById[observance.id]?.countsTowardProgress == true
        }
    return if (weeklyActionable.isEmpty()) {
        context.getString(R.string.summary_weekly_recap_empty)
    } else {
        context.resources.getQuantityString(
            R.plurals.summary_weekly_recap_value,
            weeklyActionable.size,
            completed,
            weeklyActionable.size,
        )
    }
}

private fun streakMessage(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
): String {
    val streak =
        state.observances
            .filter { LocalDate.parse(it.date) <= LocalDate.now() }
            .sortedByDescending { it.date }
            .takeWhile { state.statusesById[it.id]?.countsTowardProgress == true }
            .count()
    return when {
        streak >= 7 -> context.getString(R.string.summary_streak_stable)
        state.statusesById.values.contains(com.kevpierce.catholicfasting.core.model.CompletionStatus.MISSED) ->
            context.getString(R.string.summary_streak_recovery)
        else -> context.getString(R.string.summary_streak_momentum)
    }
}
