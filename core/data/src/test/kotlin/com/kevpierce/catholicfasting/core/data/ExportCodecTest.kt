package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.PremiumChecklistItem
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test
import java.time.Instant

class ExportCodecTest {
    @Test
    fun encryptedBackupRoundTripsDashboardState() {
        val state =
            sampleState().copy(
                statusesById = mapOf("ash-wednesday" to CompletionStatus.COMPLETED),
                fridayNotesById = mapOf("friday-note" to "Rosary and almsgiving"),
                reflections =
                    listOf(
                        ReflectionJournalEntry(
                            id = "reflection-1",
                            createdAtIso = "2026-03-13T12:00:00Z",
                            title = "Lent check-in",
                            body = "Stayed steady after a missed day.",
                        ),
                    ),
            )

        val encrypted = ExportCodec.createEncryptedBackup(state, passphrase = "fidelity-passphrase")
        val restored = ExportCodec.importEncryptedBackup(encrypted, passphrase = "fidelity-passphrase")

        assertThat(restored.settings).isEqualTo(state.settings)
        assertThat(restored.statusesById).containsExactlyEntriesIn(state.statusesById)
        assertThat(restored.fridayNotesById).containsExactlyEntriesIn(state.fridayNotesById)
        assertThat(restored.reflections).isEqualTo(state.reflections)
        assertThat(restored.planningData).isEqualTo(state.planningData)
        assertThat(restored.checklist).isEqualTo(state.checklist)
    }

    @Test
    fun householdShareCodeUpdatesPlanningSchedulesAndChecklistOnly() {
        val source =
            sampleState().copy(
                checklist =
                    listOf(
                        PremiumChecklistItem(
                            id = "share-ready",
                            title = "Share the household fasting plan",
                            isDone = true,
                        ),
                    ),
            )
        val current =
            sampleState().copy(
                statusesById = mapOf("kept-local" to CompletionStatus.MISSED),
                reflections =
                    listOf(
                        ReflectionJournalEntry(
                            id = "keep-reflection",
                            createdAtIso = "2026-03-13T12:00:00Z",
                            title = "Keep local",
                            body = "This should not be replaced.",
                        ),
                    ),
            )

        val householdCode = ExportCodec.createHouseholdShareCode(source)
        val imported = ExportCodec.importHouseholdShareCode(householdCode, current)

        assertThat(imported.planningData).isEqualTo(source.planningData)
        assertThat(imported.schedules).isEqualTo(source.schedules)
        assertThat(imported.checklist).isEqualTo(source.checklist)
        assertThat(imported.statusesById).containsExactlyEntriesIn(current.statusesById)
        assertThat(imported.reflections).isEqualTo(current.reflections)
    }

    @Test
    fun encryptedBackupRequiresMatchingPassphrase() {
        val encrypted = ExportCodec.createEncryptedBackup(sampleState(), passphrase = "fidelity-passphrase")

        val failure =
            runCatching {
                ExportCodec.importEncryptedBackup(encrypted, passphrase = "wrong-passphrase")
            }.exceptionOrNull()

        assertThat(failure).isNotNull()
    }

    @Test
    fun encryptedBackupRejectsMalformedCode() {
        val failure =
            runCatching {
                ExportCodec.importEncryptedBackup("not-a-valid-backup-payload", passphrase = "fidelity-passphrase")
            }.exceptionOrNull()

        assertThat(failure).isNotNull()
    }

    @Test
    fun householdShareRejectsBlankCode() {
        val failure =
            runCatching {
                ExportCodec.importHouseholdShareCode("", sampleState())
            }.exceptionOrNull()

        assertThat(failure).isNotNull()
        assertThat(failure!!.message).contains("Paste a household share code first.")
    }

    private fun sampleState(): DashboardState {
        val settings = RuleSettings()
        val year = 2026
        return DashboardState(
            settings = settings,
            year = year,
            observances = observancesFor(year, settings),
            premiumCompanionState =
                DashboardState().premiumCompanionState.copy(
                    seasonProgramStartIso = Instant.parse("2026-03-13T00:00:00Z").toString(),
                ),
        )
    }
}
