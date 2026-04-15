package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.LaunchFunnelSnapshot
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.model.ReminderTier
import org.junit.Test
import java.time.Instant

class ReminderAndStorageParityTest {
    @Test
    fun reminderCenterStateReflectsGuidedTierAndQuoteTime() {
        val reminderCenterState =
            buildReminderCenterState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            selectedReminderTier = ReminderTier.GUIDED,
                            dailyQuoteReminderEnabled = true,
                            dailyQuoteReminderHour = 6,
                            dailyQuoteReminderMinute = 45,
                        ),
                ),
            )

        assertThat(reminderCenterState.selectedTier).isEqualTo(ReminderTier.GUIDED)
        assertThat(reminderCenterState.supportRemindersEnabled).isTrue()
        assertThat(reminderCenterState.morningCheckInEnabled).isTrue()
        assertThat(reminderCenterState.eveningCheckInEnabled).isTrue()
        assertThat(reminderCenterState.dailyQuoteReminderEnabled).isTrue()
        assertThat(reminderCenterState.dailyQuoteTimeLabel).isEqualTo("06:45")
    }

    @Test
    fun reminderCenterStateReflectsMinimalTierWithoutSupportCadence() {
        val reminderCenterState =
            buildReminderCenterState(
                sampleState(
                    launchFunnelSnapshot =
                        LaunchFunnelSnapshot(
                            startedAtIso = "2026-03-13T00:00:00Z",
                            selectedReminderTier = ReminderTier.MINIMAL,
                        ),
                ),
            )

        assertThat(reminderCenterState.selectedTier).isEqualTo(ReminderTier.MINIMAL)
        assertThat(reminderCenterState.supportRemindersEnabled).isFalse()
        assertThat(reminderCenterState.morningCheckInEnabled).isFalse()
        assertThat(reminderCenterState.eveningCheckInEnabled).isFalse()
        assertThat(reminderCenterState.dailyQuoteReminderEnabled).isFalse()
    }

    @Test
    fun storageDiagnosticsIncludesCountsAndWarnings() {
        val diagnostics =
            buildStorageDiagnosticsState(
                sampleState(
                    statusesById = mapOf("ash-wed" to CompletionStatus.COMPLETED),
                    fridayNotesById = mapOf("friday-1" to "Stations of the Cross"),
                    intermittentSessions =
                        listOf(
                            IntermittentFastSession(
                                id = "session-1",
                                startIso = "2026-03-13T10:00:00Z",
                                endIso = "2026-03-14T02:00:00Z",
                                targetHours = 16,
                                completedTarget = true,
                            ),
                        ),
                    reflections =
                        listOf(
                            ReflectionJournalEntry(
                                id = "reflection-1",
                                title = "Lent reset",
                                body = "Keep Friday simple and prayerful.",
                                createdAtIso = "2026-03-13T11:00:00Z",
                            ),
                        ),
                    lastSyncDateIso = "2026-03-13T12:00:00Z",
                ),
            )

        assertThat(diagnostics.lastLocalWriteIso).isEqualTo("2026-03-13T12:00:00Z")
        assertThat(diagnostics.completedObservancesCount).isEqualTo(1)
        assertThat(diagnostics.fridayNotesCount).isEqualTo(1)
        assertThat(diagnostics.intermittentSessionsCount).isEqualTo(1)
        assertThat(diagnostics.reflectionsCount).isEqualTo(1)
        assertThat(diagnostics.warnings).isEmpty()
    }

    @Test
    fun storageDiagnosticsCarriesSyncWarningsForward() {
        val diagnostics =
            buildStorageDiagnosticsState(
                sampleState(
                    fridayNotesById = mapOf("friday-1" to "Rosary"),
                ),
            )

        assertThat(diagnostics.completedObservancesCount).isEqualTo(0)
        assertThat(diagnostics.fridayNotesCount).isEqualTo(1)
        assertThat(diagnostics.warnings).contains("You have notes but no completed observances.")
    }

    @Test
    fun resolveEndedFastStateFallsBackToStoredSnapshotWhenLiveStateIsStale() {
        val now = Instant.parse("2026-03-13T12:00:00Z")
        val storedState =
            sampleState().copy(
                activeIntermittentFast =
                    ActiveIntermittentFast(
                        startIso = "2026-03-13T08:00:00Z",
                        targetHours = 4,
                    ),
            )

        val endedState =
            resolveEndedFastState(
                liveState = sampleState(),
                storedState = storedState,
                now = now,
            )

        assertThat(endedState).isNotNull()
        assertThat(endedState!!.activeIntermittentFast).isNull()
        assertThat(endedState.intermittentSessions).hasSize(1)
        assertThat(endedState.intermittentSessions.first().completedTarget).isTrue()
    }

    @Test
    fun resolveEndedFastStateUsesNotificationPayloadWhenLiveAndStoredStateAreBothStale() {
        val now = Instant.parse("2026-03-13T12:00:00Z")
        val endedState =
            resolveEndedFastState(
                liveState = sampleState(),
                storedState = sampleState(),
                fallbackActiveFast =
                    ActiveIntermittentFast(
                        startIso = "2026-03-13T08:00:00Z",
                        targetHours = 4,
                    ),
                now = now,
            )

        assertThat(endedState).isNotNull()
        assertThat(endedState!!.activeIntermittentFast).isNull()
        assertThat(endedState.intermittentSessions).hasSize(1)
        assertThat(endedState.intermittentSessions.first().completedTarget).isTrue()
    }

    private fun sampleState(
        launchFunnelSnapshot: LaunchFunnelSnapshot =
            LaunchFunnelSnapshot(startedAtIso = "2026-03-13T00:00:00Z"),
        statusesById: Map<String, CompletionStatus> = emptyMap(),
        fridayNotesById: Map<String, String> = emptyMap(),
        intermittentSessions: List<IntermittentFastSession> = emptyList(),
        reflections: List<ReflectionJournalEntry> = emptyList(),
        lastSyncDateIso: String? = null,
    ): DashboardState =
        DashboardState(
            year = 2026,
            observances = observancesFor(2026, com.kevpierce.catholicfasting.core.model.RuleSettings()),
            launchFunnelSnapshot = launchFunnelSnapshot,
            statusesById = statusesById,
            fridayNotesById = fridayNotesById,
            intermittentSessions = intermittentSessions,
            reflections = reflections,
            lastSyncDateIso = lastSyncDateIso,
        )
}
