package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.model.RuleSettings
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Instant

class RepositoryMutationContractTest {
    @Test
    fun repositoryLoadsDefaultCalendarForCurrentYear() {
        val repository = repository()

        assertThat(repository.dashboardState.value.observances).isNotEmpty()
        assertThat(repository.dashboardState.value.settings.regionProfile).isEqualTo(RegionProfile.US)
    }

    @Test
    fun updateYearRebuildsCalendarForRequestedYear() {
        val repository = repository()

        repository.updateYear(2027)

        assertThat(repository.dashboardState.value.year).isEqualTo(2027)
        assertThat(
            repository.dashboardState.value.observances
                .map { it.date.substring(0, 4) },
        ).contains("2027")
    }

    @Test
    fun updateSettingsRebuildsCalendarAndStoresSelectedRegion() {
        val repository = repository()

        repository.updateSettings(RuleSettings(regionProfile = RegionProfile.CANADA))

        assertThat(repository.dashboardState.value.settings.regionProfile).isEqualTo(RegionProfile.CANADA)
        assertThat(repository.dashboardState.value.launchFunnelSnapshot.selectedRegion).isEqualTo(RegionProfile.CANADA)
        assertThat(repository.dashboardState.value.launchFunnelSnapshot.regionSelected).isTrue()
    }

    @Test
    fun noticeAcknowledgementOnlyChangesLaunchSnapshot() {
        val repository = repository()

        repository.setIndependentAppNoticeAcknowledged(true)

        assertThat(repository.dashboardState.value.launchFunnelSnapshot.independentAppNoticeAcknowledged).isTrue()
        assertThat(repository.dashboardState.value.launchFunnelSnapshot.completedOnboardingAtIso).isNull()
    }

    @Test
    fun reminderTierChangeUpdatesReminderCenterFlags() {
        val repository = repository()

        repository.setReminderTier(ReminderTier.GUIDED)

        val reminderCenter = buildReminderCenterState(repository.dashboardState.value)
        assertThat(reminderCenter.selectedTier).isEqualTo(ReminderTier.GUIDED)
        assertThat(reminderCenter.morningCheckInEnabled).isTrue()
        assertThat(reminderCenter.eveningCheckInEnabled).isTrue()
    }

    @Test
    fun selectedRegionUpdatesSettingsAndLaunchSnapshotTogether() {
        val repository = repository()

        repository.setSelectedRegion(RegionProfile.CANADA)

        assertThat(repository.dashboardState.value.settings.regionProfile).isEqualTo(RegionProfile.CANADA)
        assertThat(repository.dashboardState.value.launchFunnelSnapshot.selectedRegion).isEqualTo(RegionProfile.CANADA)
        assertThat(repository.dashboardState.value.launchFunnelSnapshot.regionSelected).isTrue()
    }

    @Test
    fun dailyQuoteReminderCanBeEnabledWithoutChangingTier() {
        val repository = repository()

        repository.setDailyQuoteReminderEnabled(true)

        assertThat(repository.dashboardState.value.launchFunnelSnapshot.dailyQuoteReminderEnabled).isTrue()
        assertThat(repository.dashboardState.value.launchFunnelSnapshot.selectedReminderTier).isEqualTo(ReminderTier.BALANCED)
        assertThat(repository.dashboardState.value.launchFunnelSnapshot.reminderTierSelected).isFalse()
    }

    @Test
    fun reminderTierSelectionMarksReminderRhythmComplete() {
        val repository = repository()

        repository.setReminderTier(ReminderTier.GUIDED)

        assertThat(repository.dashboardState.value.launchFunnelSnapshot.selectedReminderTier).isEqualTo(ReminderTier.GUIDED)
        assertThat(repository.dashboardState.value.launchFunnelSnapshot.reminderTierSelected).isTrue()
    }

    @Test
    fun dailyQuoteReminderTimeIsClampedToClockBounds() {
        val repository = repository()

        repository.setDailyQuoteReminderTime(hour = 99, minute = -4)

        val snapshot = repository.dashboardState.value.launchFunnelSnapshot
        assertThat(snapshot.dailyQuoteReminderHour).isEqualTo(23)
        assertThat(snapshot.dailyQuoteReminderMinute).isEqualTo(0)
    }

    @Test
    fun completeOnboardingWritesCompletionAndFirstActionOnce() {
        val repository = repository()
        val first = Instant.parse("2026-03-13T08:00:00Z")
        val second = Instant.parse("2026-03-14T08:00:00Z")

        repository.completeOnboarding(first)
        repository.completeOnboarding(second)

        val snapshot = repository.dashboardState.value.launchFunnelSnapshot
        assertThat(snapshot.completedOnboardingAtIso).isEqualTo(second.toString())
        assertThat(snapshot.firstActionCompletedAtIso).isEqualTo(first.toString())
    }

    @Test
    fun completedStatusIsStoredByObservanceId() {
        val repository = repository()
        val observanceId =
            repository.dashboardState.value.observances
                .first()
                .id

        repository.setStatus(observanceId, CompletionStatus.COMPLETED)

        assertThat(repository.dashboardState.value.statusesById[observanceId]).isEqualTo(CompletionStatus.COMPLETED)
    }

    @Test
    fun notStartedStatusRemovesStoredProgress() {
        val repository = repository()
        val observanceId =
            repository.dashboardState.value.observances
                .first()
                .id

        repository.setStatus(observanceId, CompletionStatus.COMPLETED)
        repository.setStatus(observanceId, CompletionStatus.NOT_STARTED)

        assertThat(repository.dashboardState.value.statusesById).doesNotContainKey(observanceId)
    }

    @Test
    fun fridayNoteIsTrimmedBeforeStorage() {
        val repository = repository()
        val fridayId =
            repository.dashboardState.value.observances
                .first { it.title.contains("Friday") }
                .id

        repository.setFridayNote(fridayId, "  prayer and almsgiving  ")

        assertThat(repository.dashboardState.value.fridayNotesById[fridayId]).isEqualTo("prayer and almsgiving")
    }

    @Test
    fun blankFridayNoteRemovesStoredNote() {
        val repository = repository()
        val fridayId =
            repository.dashboardState.value.observances
                .first { it.title.contains("Friday") }
                .id

        repository.setFridayNote(fridayId, "almsgiving")
        repository.setFridayNote(fridayId, "   ")

        assertThat(repository.dashboardState.value.fridayNotesById).doesNotContainKey(fridayId)
    }

    @Test
    fun presetHoursLowerBoundIsTwelve() {
        val repository = repository()

        repository.setIntermittentPresetHours(2)

        assertThat(repository.dashboardState.value.intermittentPresetHours).isEqualTo(12)
    }

    @Test
    fun presetHoursUpperBoundIsThreeHundredThirtySix() {
        val repository = repository()

        repository.setIntermittentPresetHours(999)

        assertThat(repository.dashboardState.value.intermittentPresetHours).isEqualTo(336)
    }

    @Test
    fun saveScheduleAddsPlanAndMarksItActive() {
        val repository = repository()

        val result = repository.saveIntermittentSchedule(null, "  Friday plan  ", 19, setOf(5))

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().name).isEqualTo("Friday plan")
        assertThat(repository.dashboardState.value.activeIntermittentScheduleId).isEqualTo(result.getOrThrow().id)
    }

    @Test
    fun saveScheduleRejectsEmptyWeekdays() {
        val repository = repository()

        val result = repository.saveIntermittentSchedule(null, "No days", 19, emptySet())

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun saveScheduleNormalizesInvalidWeekdaysAndStartHour() {
        val repository = repository()

        val plan = repository.saveIntermittentSchedule(null, "", 99, setOf(-1, 1, 8)).getOrThrow()

        assertThat(plan.name).isEqualTo("Plan 2")
        assertThat(plan.startHour).isEqualTo(23)
        assertThat(plan.weekdays).containsExactly(1)
    }

    @Test
    fun updateExistingScheduleKeepsIdAndUsesCurrentPreset() {
        val repository = repository()
        val plan = repository.saveIntermittentSchedule(null, "First", 20, setOf(2)).getOrThrow()

        repository.setIntermittentPresetHours(18)
        val updated = repository.saveIntermittentSchedule(plan.id, "Updated", 21, setOf(3, 5)).getOrThrow()

        assertThat(updated.id).isEqualTo(plan.id)
        assertThat(updated.targetHours).isEqualTo(18)
        assertThat(updated.weekdays).containsExactly(3, 5).inOrder()
    }

    @Test
    fun applyScheduleUpdatesPresetAndActiveId() {
        val repository = repository()
        repository.setIntermittentPresetHours(20)
        val plan = repository.saveIntermittentSchedule(null, "Twenty", 20, setOf(2)).getOrThrow()
        repository.setIntermittentPresetHours(14)

        val applied = repository.applyIntermittentSchedule(plan.id).getOrThrow()

        assertThat(applied.id).isEqualTo(plan.id)
        assertThat(repository.dashboardState.value.intermittentPresetHours).isEqualTo(20)
        assertThat(repository.dashboardState.value.activeIntermittentScheduleId).isEqualTo(plan.id)
    }

    @Test
    fun deleteScheduleFallsBackToRemainingPlan() {
        val repository = repository()
        val plan = repository.saveIntermittentSchedule(null, "Delete me", 20, setOf(2)).getOrThrow()

        val result = repository.deleteIntermittentSchedule(plan.id)

        assertThat(result.isSuccess).isTrue()
        assertThat(
            repository.dashboardState.value.schedules
                .map { it.id },
        ).doesNotContain(plan.id)
        assertThat(repository.dashboardState.value.activeIntermittentScheduleId).isNotNull()
    }

    @Test
    fun startFastIsIgnoredWhenFastAlreadyActive() {
        val repository = repository()

        repository.startIntermittentFast(Instant.parse("2026-03-13T08:00:00Z"))
        repository.startIntermittentFast(Instant.parse("2026-03-13T09:00:00Z"))

        assertThat(
            repository.dashboardState.value.activeIntermittentFast
                ?.startIso,
        ).isEqualTo("2026-03-13T08:00:00Z")
    }

    @Test
    fun endFastStoresCompletedSessionAndClearsActiveFast() {
        val repository = repository()

        repository.startIntermittentFastWithIntention(
            intentionId = IntermittentFastIntention.MERCY.name,
            now = Instant.parse("2026-03-13T08:00:00Z"),
        )
        repository.endIntermittentFastWithReview(
            reviewNote = "Shared the evening meal calmly.",
            now = Instant.parse("2026-03-14T01:00:00Z"),
        )

        assertThat(repository.dashboardState.value.activeIntermittentFast).isNull()
        assertThat(repository.dashboardState.value.intermittentSessions).hasSize(1)
        val session =
            repository.dashboardState.value.intermittentSessions
                .first()
        assertThat(session.completedTarget).isTrue()
        assertThat(session.intentionId).isEqualTo(IntermittentFastIntention.MERCY.name)
        assertThat(session.reviewNote).isEqualTo("Shared the evening meal calmly.")
    }

    @Test
    fun cancelFastClearsActiveFastWithoutWritingSession() {
        val repository = repository()

        repository.startIntermittentFast(Instant.parse("2026-03-13T08:00:00Z"))
        repository.cancelIntermittentFast()

        assertThat(repository.dashboardState.value.activeIntermittentFast).isNull()
        assertThat(repository.dashboardState.value.intermittentSessions).isEmpty()
    }

    @Test
    fun notificationEndFastUsesFallbackPayloadWhenLiveStateIsEmpty() {
        val repository = repository()

        val ended =
            repository.endIntermittentFastFromAction(
                startIso = "2026-03-13T08:00:00Z",
                targetHours = 16,
                now = Instant.parse("2026-03-14T01:00:00Z"),
            )

        assertThat(ended).isTrue()
        assertThat(repository.dashboardState.value.intermittentSessions).hasSize(1)
        val session =
            repository.dashboardState.value.intermittentSessions
                .first()
        assertThat(session.intentionId).isNull()
        assertThat(session.reviewNote).isNull()
    }

    @Test
    fun notificationEndFastReturnsFalseForMissingPayloadAndNoActiveFast() {
        val repository = repository()

        val ended = repository.endIntermittentFastFromAction(startIso = null, targetHours = 16)

        assertThat(ended).isFalse()
        assertThat(repository.dashboardState.value.intermittentSessions).isEmpty()
    }

    @Test
    fun reflectionRequiresTitleOrBody() {
        val repository = repository()

        val result = repository.addReflectionEntry(" ", " ")

        assertThat(result.isFailure).isTrue()
        assertThat(repository.dashboardState.value.reflections).isEmpty()
    }

    @Test
    fun reflectionDefaultsTitleWhenOnlyBodyIsProvided() {
        val repository = repository()

        val result = repository.addReflectionEntry(" ", "  Body text  ", Instant.parse("2026-03-13T08:00:00Z"))

        assertThat(result.isSuccess).isTrue()
        assertThat(
            repository.dashboardState.value.reflections
                .first()
                .title,
        ).isEqualTo("Reflection")
        assertThat(
            repository.dashboardState.value.reflections
                .first()
                .body,
        ).isEqualTo("Body text")
    }

    @Test
    fun storageDiagnosticsReflectCompletedNotesSessionsAndReflections() {
        val repository = repository()
        val observance =
            repository.dashboardState.value.observances
                .first()
        val friday =
            repository.dashboardState.value.observances
                .first { it.title.contains("Friday") }

        repository.setStatus(observance.id, CompletionStatus.COMPLETED)
        repository.setFridayNote(friday.id, "Note")
        repository.startIntermittentFast(Instant.parse("2026-03-13T08:00:00Z"))
        repository.endIntermittentFast(Instant.parse("2026-03-14T01:00:00Z"))
        repository.addReflectionEntry("Title", "Body")

        val diagnostics = buildStorageDiagnosticsState(repository.dashboardState.value)
        assertThat(diagnostics.completedObservancesCount).isEqualTo(1)
        assertThat(diagnostics.fridayNotesCount).isEqualTo(1)
        assertThat(diagnostics.intermittentSessionsCount).isEqualTo(1)
        assertThat(diagnostics.reflectionsCount).isEqualTo(1)
    }

    @Test
    fun repositoryMigratesStorageBeforeReadingSnapshot() {
        val storage = InMemoryStorage(AppStorageSnapshot(year = 2029))

        val repository = AppRepository(storage)

        assertThat(repository.dashboardState.value.year).isEqualTo(2029)
        assertThat(storage.migrated).isTrue()
    }

    @Test
    fun repositoryPersistsLatestStateToStorage() {
        val storage = InMemoryStorage()
        val repository = AppRepository(storage)

        repository.setSelectedRegion(RegionProfile.CANADA)
        storage.waitForWrite { it.settings.regionProfile == RegionProfile.CANADA }

        assertThat(runBlocking { storage.readSnapshot().settings.regionProfile }).isEqualTo(RegionProfile.CANADA)
    }

    private fun repository(snapshot: AppStorageSnapshot = AppStorageSnapshot(year = 2026)): AppRepository =
        AppRepository(InMemoryStorage(snapshot))

    private class InMemoryStorage(
        initialSnapshot: AppStorageSnapshot = AppStorageSnapshot(),
    ) : AppStorageGateway {
        @Volatile
        private var snapshot = initialSnapshot

        @Volatile
        var migrated = false

        override suspend fun writeSnapshot(snapshot: AppStorageSnapshot) {
            this.snapshot = snapshot
        }

        override suspend fun readSnapshot(): AppStorageSnapshot = snapshot

        override suspend fun migrateIfNeeded() {
            migrated = true
        }

        override suspend fun clear() {
            snapshot = AppStorageSnapshot()
        }

        fun waitForWrite(condition: (AppStorageSnapshot) -> Boolean) {
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < 3_000) {
                if (condition(snapshot)) {
                    return
                }
                Thread.sleep(20)
            }
            throw AssertionError("Timed out waiting for storage write.")
        }
    }
}
