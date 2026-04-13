package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test

class IntermittentScheduleMutationTest {
    @Test
    fun saveIntermittentScheduleAddsNewPlanAndMarksItActive() {
        val updated =
            sampleState().copy(intermittentPresetHours = 18)
                .saveIntermittentSchedule(
                    scheduleId = null,
                    name = "",
                    startHour = 21,
                    weekdays = setOf(2, 4, 6),
                )

        val savedPlan = updated.schedules.last()

        assertThat(savedPlan.name).isEqualTo("Plan 2")
        assertThat(savedPlan.targetHours).isEqualTo(18)
        assertThat(savedPlan.startHour).isEqualTo(21)
        assertThat(savedPlan.weekdays).containsExactly(2, 4, 6).inOrder()
        assertThat(updated.activeIntermittentScheduleId).isEqualTo(savedPlan.id)
    }

    @Test
    fun saveIntermittentScheduleUpdatesExistingPlan() {
        val existing = sampleState().schedules.first()

        val updated =
            sampleState().copy(intermittentPresetHours = 20)
                .saveIntermittentSchedule(
                    scheduleId = existing.id,
                    name = "Friday Reset",
                    startHour = 19,
                    weekdays = setOf(6),
                )

        val savedPlan = updated.schedules.first()

        assertThat(savedPlan.id).isEqualTo(existing.id)
        assertThat(savedPlan.name).isEqualTo("Friday Reset")
        assertThat(savedPlan.targetHours).isEqualTo(20)
        assertThat(savedPlan.startHour).isEqualTo(19)
        assertThat(savedPlan.weekdays).containsExactly(6)
        assertThat(updated.activeIntermittentScheduleId).isEqualTo(existing.id)
    }

    @Test
    fun applyIntermittentScheduleUpdatesPresetAndActiveId() {
        val extraPlan =
            IntermittentSchedulePlan(
                id = "lent-plan",
                name = "Lent Plan",
                targetHours = 24,
                startHour = 18,
                weekdays = listOf(3, 6),
            )

        val updated =
            sampleState().copy(
                schedules = sampleState().schedules + extraPlan,
                activeIntermittentScheduleId = sampleState().schedules.first().id,
            ).applyIntermittentSchedule(extraPlan.id)

        assertThat(updated.intermittentPresetHours).isEqualTo(24)
        assertThat(updated.activeIntermittentScheduleId).isEqualTo(extraPlan.id)
    }

    @Test
    fun deleteIntermittentScheduleFallsBackToFirstRemainingPlan() {
        val first = sampleState().schedules.first()
        val extraPlan =
            IntermittentSchedulePlan(
                id = "weekend-plan",
                name = "Weekend Plan",
                targetHours = 14,
                startHour = 17,
                weekdays = listOf(1, 7),
            )

        val updated =
            sampleState().copy(
                schedules = listOf(first, extraPlan),
                activeIntermittentScheduleId = extraPlan.id,
            ).deleteIntermittentSchedule(extraPlan.id)

        assertThat(updated.schedules).containsExactly(first)
        assertThat(updated.activeIntermittentScheduleId).isEqualTo(first.id)
    }

    private fun sampleState(): DashboardState {
        val settings = RuleSettings()
        val year = 2026
        return DashboardState(
            settings = settings,
            year = year,
            observances = observancesFor(year, settings),
        )
    }
}
