package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test
import java.time.Instant

class WidgetSnapshotParityTest {
    @Test
    fun widgetSnapshotReflectsTodayProgressAndActiveFast() {
        val settings = RuleSettings()
        val observances = observancesFor(2026, settings)
        val todayObservance = observances.first()
        val state =
            DashboardState(
                settings = settings,
                year = 2026,
                observances = observances,
                statusesById = mapOf(todayObservance.id to CompletionStatus.COMPLETED),
                activeIntermittentFast =
                    ActiveIntermittentFast(
                        startIso = "2026-03-13T08:00:00Z",
                        targetHours = 16,
                    ),
            )

        val snapshot = state.buildWidgetSnapshot(now = Instant.parse("2026-03-13T12:00:00Z"))

        assertThat(snapshot.todayTitle).isNotEmpty()
        assertThat(snapshot.completionRate).isAtLeast(0.0)
        assertThat(snapshot.hasActiveIntermittentFast).isTrue()
        assertThat(snapshot.activeIntermittentFastStartIso).isEqualTo("2026-03-13T08:00:00Z")
        assertThat(snapshot.activeIntermittentTargetHours).isEqualTo(16)
    }

    @Test
    fun widgetSnapshotIncludesUpcomingRequiredObservanceAndInactiveTrackerState() {
        val settings = RuleSettings()
        val observances = observancesFor(2026, settings)
        val snapshot =
            DashboardState(
                settings = settings,
                year = 2026,
                observances = observances,
            ).buildWidgetSnapshot(now = Instant.parse("2026-03-13T12:00:00Z"))

        assertThat(snapshot.nextRequiredTitle).isNotEmpty()
        assertThat(snapshot.nextRequiredDateIso).isNotNull()
        assertThat(snapshot.hasActiveIntermittentFast).isFalse()
        assertThat(snapshot.activeIntermittentFastStartIso).isNull()
        assertThat(snapshot.activeIntermittentTargetHours).isEqualTo(16)
    }
}
