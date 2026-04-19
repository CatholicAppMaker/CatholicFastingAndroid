package com.kevpierce.catholicfasting.core.rules

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import org.junit.Test
import java.time.LocalDate

class RequiredDayReminderPlannerTest {
    @Test
    fun maximumRequiredRemindersRespectsQueueCapacityAndHeadroom() {
        assertThat(RequiredDayReminderPlanner.maximumRequiredReminders(existingNonRequiredPendingCount = 0))
            .isEqualTo(50)
        assertThat(RequiredDayReminderPlanner.maximumRequiredReminders(existingNonRequiredPendingCount = 10))
            .isEqualTo(50)
        assertThat(RequiredDayReminderPlanner.maximumRequiredReminders(existingNonRequiredPendingCount = 20))
            .isEqualTo(44)
        assertThat(RequiredDayReminderPlanner.maximumRequiredReminders(existingNonRequiredPendingCount = 80))
            .isEqualTo(0)
        assertThat(RequiredDayReminderPlanner.maximumRequiredReminders(existingNonRequiredPendingCount = -5))
            .isEqualTo(50)
    }

    @Test
    fun upcomingMandatoryObservancesSortsFiltersAndLimits() {
        val planned =
            RequiredDayReminderPlanner.upcomingMandatoryObservances(
                observances =
                    listOf(
                        observance(
                            id = "future-b",
                            title = "Future B",
                            date = "2026-02-20",
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                        observance(
                            id = "past",
                            title = "Past",
                            date = "2026-02-17",
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                        observance(
                            id = "today",
                            title = "Today",
                            date = "2026-02-18",
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                        observance(
                            id = "optional",
                            title = "Optional",
                            date = "2026-02-19",
                            obligation = ObservanceObligation.OPTIONAL,
                        ),
                        observance(
                            id = "future-a",
                            title = "Future A",
                            date = "2026-02-19",
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                    ),
                now = LocalDate.of(2026, 2, 18),
                limit = 2,
            )

        assertThat(planned.map { it.id }).containsExactly("today", "future-a").inOrder()
    }

    @Test
    fun upcomingMandatoryObservancesDeduplicatesByIdAndUsesStableTieBreak() {
        val planned =
            RequiredDayReminderPlanner.upcomingMandatoryObservances(
                observances =
                    listOf(
                        observance(
                            id = "dup",
                            title = "Duplicate Earlier",
                            date = "2026-03-01",
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                        observance(
                            id = "dup",
                            title = "Duplicate Later",
                            date = "2026-03-02",
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                        observance(
                            id = "b",
                            title = "Same Day B",
                            date = "2026-03-03",
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                        observance(
                            id = "a",
                            title = "Same Day A",
                            date = "2026-03-03",
                            obligation = ObservanceObligation.MANDATORY,
                        ),
                    ),
                now = LocalDate.of(2026, 2, 28),
                limit = 10,
            )

        assertThat(planned.map { it.id }).containsExactly("dup", "a", "b").inOrder()
    }

    @Test
    fun additionalRequiredReminderSlotsClampsAtZeroAndAvailableCapacity() {
        assertThat(
            RequiredDayReminderPlanner.additionalRequiredReminderSlots(
                existingRequiredPendingCount = 10,
                existingNonRequiredPendingCount = 10,
            ),
        ).isEqualTo(40)
        assertThat(
            RequiredDayReminderPlanner.additionalRequiredReminderSlots(
                existingRequiredPendingCount = 50,
                existingNonRequiredPendingCount = 10,
            ),
        ).isEqualTo(0)
        assertThat(
            RequiredDayReminderPlanner.additionalRequiredReminderSlots(
                existingRequiredPendingCount = 0,
                existingNonRequiredPendingCount = 80,
            ),
        ).isEqualTo(0)
        assertThat(
            RequiredDayReminderPlanner.additionalRequiredReminderSlots(
                existingRequiredPendingCount = -5,
                existingNonRequiredPendingCount = -2,
            ),
        ).isEqualTo(50)
    }

    private fun observance(
        id: String,
        title: String,
        date: String,
        obligation: ObservanceObligation,
    ): Observance =
        Observance(
            id = id,
            title = title,
            date = date,
            kind = ObservanceKind.HOLY_DAY,
            obligation = obligation,
            rationale = "Test rationale",
            citations = emptyList(),
            ruleVersion = "test",
        )
}
