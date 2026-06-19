package com.kevpierce.catholicfasting.core.data

import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import java.time.Instant
import java.util.UUID

private const val MIN_INTERMITTENT_PRESET_HOURS = 12
private const val MAX_INTERMITTENT_PRESET_HOURS = 336
private const val MAX_STORED_INTERMITTENT_SESSIONS = 500

internal fun boundedPresetHours(hours: Int): Int =
    hours
        .coerceAtLeast(MIN_INTERMITTENT_PRESET_HOURS)
        .coerceAtMost(MAX_INTERMITTENT_PRESET_HOURS)

internal fun DashboardState.saveIntermittentSchedule(
    scheduleId: String?,
    name: String,
    startHour: Int,
    weekdays: Set<Int>,
): DashboardState {
    val normalizedWeekdays =
        weekdays
            .filter { it in 1..7 }
            .sorted()
    require(normalizedWeekdays.isNotEmpty()) { "Select at least one weekday for the schedule." }

    val normalizedHour = startHour.coerceIn(0, 23)
    val trimmedName = name.trim()
    val targetHours = intermittentPresetHours
    val existingIndex = schedules.indexOfFirst { it.id == scheduleId }

    return if (existingIndex >= 0) {
        val existingPlan = schedules[existingIndex]
        val updatedPlan =
            existingPlan.copy(
                name = trimmedName.ifEmpty { "Plan ${existingIndex + 1}" },
                targetHours = targetHours,
                startHour = normalizedHour,
                weekdays = normalizedWeekdays,
            )
        copy(
            schedules = schedules.toMutableList().apply { this[existingIndex] = updatedPlan },
            activeIntermittentScheduleId = updatedPlan.id,
        )
    } else {
        val newPlan =
            IntermittentSchedulePlan(
                id = UUID.randomUUID().toString(),
                name = trimmedName.ifEmpty { "Plan ${schedules.size + 1}" },
                targetHours = targetHours,
                startHour = normalizedHour,
                weekdays = normalizedWeekdays,
            )
        copy(
            schedules = schedules + newPlan,
            activeIntermittentScheduleId = newPlan.id,
        )
    }
}

internal fun DashboardState.deleteIntermittentSchedule(scheduleId: String): DashboardState {
    val updatedSchedules = schedules.filterNot { it.id == scheduleId }
    val nextActiveScheduleId =
        when {
            activeIntermittentScheduleId != scheduleId -> activeIntermittentScheduleId
            updatedSchedules.isEmpty() -> null
            else -> updatedSchedules.first().id
        }
    return copy(
        schedules = updatedSchedules,
        activeIntermittentScheduleId = nextActiveScheduleId,
    )
}

internal fun DashboardState.applyIntermittentSchedule(scheduleId: String): DashboardState {
    val plan =
        schedules.firstOrNull { it.id == scheduleId }
            ?: error("The selected schedule no longer exists.")
    return copy(
        intermittentPresetHours = boundedPresetHours(plan.targetHours),
        activeIntermittentScheduleId = plan.id,
    )
}

internal fun DashboardState.endIntermittentFast(
    now: Instant,
    reviewNote: String? = null,
): DashboardState? =
    activeIntermittentFast
        ?.let { activeFast ->
            parseCompletedFast(activeFast, now, reviewNote)?.let { session ->
                copy(
                    intermittentSessions =
                        (listOf(session) + intermittentSessions).take(MAX_STORED_INTERMITTENT_SESSIONS),
                    activeIntermittentFast = null,
                )
            }
        }

internal fun resolveEndedFastState(
    liveState: DashboardState,
    storedState: DashboardState,
    fallbackActiveFast: ActiveIntermittentFast? = null,
    now: Instant,
): DashboardState? =
    liveState.endIntermittentFast(now)
        ?: storedState.endIntermittentFast(now)
        ?: fallbackActiveFast?.let { liveState.copy(activeIntermittentFast = it).endIntermittentFast(now) }

private fun parseCompletedFast(
    activeFast: ActiveIntermittentFast,
    now: Instant,
    reviewNote: String? = null,
): IntermittentFastSession? =
    runCatching { Instant.parse(activeFast.startIso) }
        .getOrNull()
        ?.takeIf(now::isAfter)
        ?.let { start ->
            val durationInSeconds = now.epochSecond - start.epochSecond
            val completedTarget = durationInSeconds >= activeFast.targetHours * 3600L
            IntermittentFastSession(
                id = UUID.randomUUID().toString(),
                startIso = activeFast.startIso,
                endIso = now.toString(),
                targetHours = activeFast.targetHours,
                completedTarget = completedTarget,
                intentionId = activeFast.intentionId,
                reviewNote = reviewNote?.trim()?.takeIf(String::isNotBlank),
            )
        }

internal fun normalizedIntentionId(intentionId: String): String =
    IntermittentFastIntention.entries
        .firstOrNull { it.name == intentionId }
        ?.name
        ?: IntermittentFastIntention.PERSONAL_DISCIPLINE.name
