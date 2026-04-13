package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import org.junit.Test

class SyncSnapshotTest {
    @Test
    fun syncSnapshotReportsCountsAndWarnings() {
        val snapshot =
            buildSyncSnapshot(
                DashboardState(
                    statusesById =
                        mapOf(
                            "a" to CompletionStatus.COMPLETED,
                            "b" to CompletionStatus.SUBSTITUTED,
                        ),
                    fridayNotesById = mapOf("note-1" to "Rosary"),
                    lastSyncDateIso = "2026-03-13T10:00:00Z",
                ),
            )

        assertThat(snapshot.completedObservancesCount).isEqualTo(2)
        assertThat(snapshot.fridayNotesCount).isEqualTo(1)
        assertThat(snapshot.lastSyncDateIso).isEqualTo("2026-03-13T10:00:00Z")
        assertThat(snapshot.warnings).isEmpty()
    }

    @Test
    fun syncSnapshotWarnsWhenNotesExistWithoutCompletions() {
        val snapshot =
            buildSyncSnapshot(
                DashboardState(
                    fridayNotesById = mapOf("note-1" to "Rosary"),
                ),
            )

        assertThat(snapshot.completedObservancesCount).isEqualTo(0)
        assertThat(snapshot.fridayNotesCount).isEqualTo(1)
        assertThat(snapshot.warnings).contains("You have notes but no completed observances.")
    }
}
