@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfasting.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.FastingPlanningData
import com.kevpierce.catholicfasting.core.model.HouseholdProfile
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.IntermittentSchedulePlan
import com.kevpierce.catholicfasting.core.model.LaunchFunnelSnapshot
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.OnboardingState
import com.kevpierce.catholicfasting.core.model.PremiumChecklistItem
import com.kevpierce.catholicfasting.core.model.PremiumCompanionState
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderCenterState
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.model.RuleSettings
import com.kevpierce.catholicfasting.core.model.SeasonalHeroState
import com.kevpierce.catholicfasting.core.model.SetupProgressState
import com.kevpierce.catholicfasting.core.model.StorageDiagnosticsState
import com.kevpierce.catholicfasting.core.model.SubscriptionOfferCatalog
import com.kevpierce.catholicfasting.core.model.SyncSnapshot
import com.kevpierce.catholicfasting.core.model.WidgetSnapshot
import com.kevpierce.catholicfasting.core.rules.LiturgicalSeasonThemeEngine
import com.kevpierce.catholicfasting.core.rules.ObservanceCalculator
import com.kevpierce.catholicfasting.core.rules.SacredImageryCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentPackCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentSupport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

private val Context.catholicFastingDataStore by preferencesDataStore(name = "catholic_fasting_store")

data class DashboardState(
    val settings: RuleSettings = RuleSettings(),
    val year: Int = LocalDate.now().year,
    val observances: List<Observance> = emptyList(),
    val statusesById: Map<String, CompletionStatus> = emptyMap(),
    val fridayNotesById: Map<String, String> = emptyMap(),
    val planningData: FastingPlanningData = FastingPlanningData.default,
    val schedules: List<IntermittentSchedulePlan> = defaultSchedules,
    val activeIntermittentScheduleId: String? = defaultSchedules.firstOrNull()?.id,
    val intermittentSessions: List<IntermittentFastSession> = emptyList(),
    val activeIntermittentFast: ActiveIntermittentFast? = null,
    val intermittentPresetHours: Int = DEFAULT_INTERMITTENT_PRESET_HOURS,
    val profiles: List<HouseholdProfile> = defaultProfiles,
    val reflections: List<ReflectionJournalEntry> = emptyList(),
    val checklist: List<PremiumChecklistItem> = defaultChecklist,
    val lastSyncDateIso: String? = null,
    val premiumCompanionState: PremiumCompanionState =
        PremiumCompanionState(
            seasonProgramStartIso = Instant.now().toString(),
        ),
    val launchFunnelSnapshot: LaunchFunnelSnapshot =
        LaunchFunnelSnapshot(startedAtIso = Instant.now().toString()),
)

@Serializable
internal data class AppStorageSnapshot(
    val settings: RuleSettings = RuleSettings(),
    val year: Int = LocalDate.now().year,
    val statusesById: Map<String, CompletionStatus> = emptyMap(),
    val fridayNotesById: Map<String, String> = emptyMap(),
    val planningData: FastingPlanningData = FastingPlanningData.default,
    val schedules: List<IntermittentSchedulePlan> = defaultSchedules,
    val activeIntermittentScheduleId: String? = defaultSchedules.firstOrNull()?.id,
    val intermittentSessions: List<IntermittentFastSession> = emptyList(),
    val activeIntermittentFast: ActiveIntermittentFast? = null,
    val intermittentPresetHours: Int = DEFAULT_INTERMITTENT_PRESET_HOURS,
    val profiles: List<HouseholdProfile> = defaultProfiles,
    val reflections: List<ReflectionJournalEntry> = emptyList(),
    val checklist: List<PremiumChecklistItem> = defaultChecklist,
    val lastSyncDateIso: String? = null,
    val premiumCompanionState: PremiumCompanionState = PremiumCompanionState(seasonProgramStartIso = DEFAULT_TIMESTAMP),
    val launchFunnelSnapshot: LaunchFunnelSnapshot = LaunchFunnelSnapshot(startedAtIso = DEFAULT_TIMESTAMP),
)

private const val STORAGE_SCHEMA_VERSION = 5
private const val DEFAULT_TIMESTAMP = "2026-03-13T00:00:00Z"
private const val DEFAULT_INTERMITTENT_PRESET_HOURS = 16

private val JsonCodec =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

private val snapshotKey = stringPreferencesKey("app_snapshot_v1")
private val schemaVersionKey = intPreferencesKey("storage_schema_version")

private val defaultSchedules =
    listOf(
        IntermittentSchedulePlan(
            id = "mon-wed-fri",
            name = "Mon/Wed/Fri 16h",
            targetHours = 16,
            startHour = 20,
            weekdays = listOf(2, 4, 6),
        ),
    )

private val defaultProfiles =
    listOf(
        HouseholdProfile(
            id = "my-profile",
            name = "My Profile",
            isAge14OrOlderForAbstinence = true,
            isAge18OrOlderForFasting = true,
            medicalDispensation = false,
        ),
    )

private val defaultChecklist =
    listOf(
        PremiumChecklistItem(
            id = "friday-plan",
            title = "Plan Friday penance for this week",
            isDone = false,
        ),
        PremiumChecklistItem(
            id = "season-reminders",
            title = "Set reminder cadence for next liturgical season",
            isDone = false,
        ),
    )

@Suppress("TooManyFunctions")
class AppRepository internal constructor(
    private val storage: AppStorageGateway,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeMutex = Mutex()
    private val writeVersion = AtomicLong(0L)
    private val state = MutableStateFlow(loadDefaultState())
    val dashboardState: StateFlow<DashboardState> = state.asStateFlow()

    init {
        runBlocking(Dispatchers.IO) {
            storage.migrateIfNeeded()
            val snapshot = storage.readSnapshot()
            state.value = snapshot.toDashboardState()
        }
    }

    fun updateYear(year: Int) {
        val current = state.value
        persist(current.copy(year = year, observances = observancesFor(year, current.settings)))
    }

    fun updateSettings(settings: RuleSettings) {
        val current = state.value
        persist(
            current.copy(
                settings = settings,
                observances = observancesFor(current.year, settings),
                launchFunnelSnapshot =
                    current.launchFunnelSnapshot.copy(
                        selectedRegion = settings.regionProfile,
                        regionSelected = true,
                    ),
            ),
        )
    }

    fun setIndependentAppNoticeAcknowledged(acknowledged: Boolean) {
        persist(
            state.value.copy(
                launchFunnelSnapshot =
                    state.value.launchFunnelSnapshot.copy(
                        independentAppNoticeAcknowledged = acknowledged,
                    ),
            ),
        )
    }

    fun setReminderTier(reminderTier: ReminderTier) {
        persist(
            state.value.copy(
                launchFunnelSnapshot =
                    state.value.launchFunnelSnapshot.copy(
                        selectedReminderTier = reminderTier,
                        reminderTierSelected = true,
                    ),
            ),
        )
    }

    fun setSelectedRegion(regionProfile: RegionProfile) {
        val current = state.value
        persist(
            current.copy(
                settings = current.settings.copy(regionProfile = regionProfile),
                observances = observancesFor(current.year, current.settings.copy(regionProfile = regionProfile)),
                launchFunnelSnapshot =
                    current.launchFunnelSnapshot.copy(
                        selectedRegion = regionProfile,
                        regionSelected = true,
                    ),
            ),
        )
    }

    fun setDailyQuoteReminderEnabled(enabled: Boolean) {
        persist(
            state.value.copy(
                launchFunnelSnapshot =
                    state.value.launchFunnelSnapshot.copy(
                        dailyQuoteReminderEnabled = enabled,
                    ),
            ),
        )
    }

    fun setDailyQuoteReminderTime(
        hour: Int,
        minute: Int,
    ) {
        persist(
            state.value.copy(
                launchFunnelSnapshot =
                    state.value.launchFunnelSnapshot.copy(
                        dailyQuoteReminderHour = hour.coerceIn(0, 23),
                        dailyQuoteReminderMinute = minute.coerceIn(0, 59),
                    ),
            ),
        )
    }

    fun setIntermittentIntention(intentionId: String) {
        val normalized = normalizedIntentionId(intentionId)
        persist(
            state.value.copy(
                activeIntermittentFast =
                    state.value.activeIntermittentFast?.copy(
                        intentionId = normalized,
                    ),
                launchFunnelSnapshot =
                    state.value.launchFunnelSnapshot.copy(
                        selectedIntermittentIntentionId = normalized,
                        intermittentIntentionSelected = true,
                    ),
            ),
        )
    }

    fun completeOnboarding(now: Instant = Instant.now()) {
        persist(
            state.value.copy(
                launchFunnelSnapshot =
                    state.value.launchFunnelSnapshot.copy(
                        completedOnboardingAtIso = now.toString(),
                        firstActionCompletedAtIso =
                            state.value.launchFunnelSnapshot.firstActionCompletedAtIso ?: now.toString(),
                    ),
            ),
        )
    }

    fun setStatus(
        observanceId: String,
        status: CompletionStatus,
    ) {
        val updated = state.value.statusesById.toMutableMap()
        if (status == CompletionStatus.NOT_STARTED) {
            updated.remove(observanceId)
        } else {
            updated[observanceId] = status
        }
        persist(state.value.copy(statusesById = updated))
    }

    fun setFridayNote(
        observanceId: String,
        note: String,
    ) {
        val updated = state.value.fridayNotesById.toMutableMap()
        val trimmed = note.trim()
        if (trimmed.isEmpty()) {
            updated.remove(observanceId)
        } else {
            updated[observanceId] = trimmed
        }
        persist(state.value.copy(fridayNotesById = updated))
    }

    fun setIntermittentPresetHours(hours: Int) {
        persist(state.value.copy(intermittentPresetHours = boundedPresetHours(hours)))
    }

    fun saveIntermittentSchedule(
        scheduleId: String?,
        name: String,
        startHour: Int,
        weekdays: Set<Int>,
    ): Result<IntermittentSchedulePlan> =
        runCatching {
            val updatedState =
                state.value.saveIntermittentSchedule(
                    scheduleId = scheduleId,
                    name = name,
                    startHour = startHour,
                    weekdays = weekdays,
                )
            val savedPlan =
                updatedState.schedules.firstOrNull { it.id == updatedState.activeIntermittentScheduleId }
                    ?: error("Saved schedule could not be found.")
            persist(updatedState)
            savedPlan
        }

    fun deleteIntermittentSchedule(scheduleId: String): Result<Unit> =
        runCatching {
            persist(state.value.deleteIntermittentSchedule(scheduleId))
        }

    fun applyIntermittentSchedule(scheduleId: String): Result<IntermittentSchedulePlan> =
        runCatching {
            val updatedState = state.value.applyIntermittentSchedule(scheduleId)
            val appliedPlan =
                updatedState.schedules.firstOrNull { it.id == scheduleId }
                    ?: error("Applied schedule could not be found.")
            persist(updatedState)
            appliedPlan
        }

    fun startIntermittentFast(now: Instant = Instant.now()) {
        startIntermittentFastWithIntention(intentionId = null, now = now)
    }

    fun startIntermittentFastWithIntention(
        intentionId: String? = null,
        now: Instant = Instant.now(),
    ) {
        val current = state.value
        if (current.activeIntermittentFast != null) {
            return
        }
        val normalizedIntentionId =
            normalizedIntentionId(
                intentionId ?: current.launchFunnelSnapshot.selectedIntermittentIntentionId,
            )

        persist(
            current.copy(
                activeIntermittentFast =
                    ActiveIntermittentFast(
                        startIso = now.toString(),
                        targetHours = current.intermittentPresetHours,
                        intentionId = normalizedIntentionId,
                    ),
                launchFunnelSnapshot =
                    current.launchFunnelSnapshot.copy(
                        selectedIntermittentIntentionId = normalizedIntentionId,
                        intermittentIntentionSelected = true,
                    ),
            ),
        )
    }

    fun endIntermittentFast(now: Instant = Instant.now()) {
        endIntermittentFastWithReview(reviewNote = null, now = now)
    }

    fun endIntermittentFastWithReview(
        reviewNote: String? = null,
        now: Instant = Instant.now(),
    ) {
        val current = state.value
        val endedState = current.endIntermittentFast(now, reviewNote)
        if (endedState != null) {
            persist(endedState)
        }
    }

    fun endIntermittentFastFromAction(
        startIso: String?,
        targetHours: Int,
        now: Instant = Instant.now(),
    ): Boolean {
        val storedState =
            runBlocking {
                storage.readSnapshot().toDashboardState()
            }
        val fallbackActiveFast =
            startIso?.let {
                ActiveIntermittentFast(
                    startIso = it,
                    targetHours = boundedPresetHours(targetHours),
                    intentionId = storedState.activeIntermittentFast?.intentionId,
                )
            }
        val endedState =
            resolveEndedFastState(
                liveState = state.value,
                storedState = storedState,
                fallbackActiveFast = fallbackActiveFast,
                now = now,
            ) ?: return false
        persistBlocking(endedState)
        return true
    }

    fun cancelIntermittentFast() {
        if (state.value.activeIntermittentFast == null) {
            return
        }
        persist(state.value.copy(activeIntermittentFast = null))
    }

    fun addReflectionEntry(
        title: String,
        body: String,
        now: Instant = Instant.now(),
    ): Result<Unit> {
        val trimmedTitle = title.trim()
        val trimmedBody = body.trim()
        if (trimmedTitle.isEmpty() && trimmedBody.isEmpty()) {
            return Result.failure(IllegalArgumentException("Add a reflection title or body first."))
        }

        val entry =
            ReflectionJournalEntry(
                id = UUID.randomUUID().toString(),
                createdAtIso = now.toString(),
                title = trimmedTitle.ifEmpty { "Reflection" },
                body = trimmedBody,
            )
        persist(state.value.copy(reflections = listOf(entry) + state.value.reflections))
        return Result.success(Unit)
    }

    fun flushForTesting() {
        val snapshot = state.value.toStorageSnapshot()
        val version = writeVersion.incrementAndGet()
        runBlocking(Dispatchers.IO) {
            writeSnapshotIfLatest(version, snapshot)
        }
    }

    private fun persist(nextState: DashboardState) {
        val syncedState = nextState.copy(lastSyncDateIso = Instant.now().toString())
        val snapshot = syncedState.toStorageSnapshot()
        val version = writeVersion.incrementAndGet()
        state.value = syncedState
        scope.launch {
            writeSnapshotIfLatest(version, snapshot)
        }
    }

    private fun persistBlocking(nextState: DashboardState) {
        val syncedState = nextState.copy(lastSyncDateIso = Instant.now().toString())
        val snapshot = syncedState.toStorageSnapshot()
        val version = writeVersion.incrementAndGet()
        state.value = syncedState
        runBlocking(Dispatchers.IO) {
            writeSnapshotIfLatest(version, snapshot)
        }
    }

    private suspend fun writeSnapshotIfLatest(
        version: Long,
        snapshot: AppStorageSnapshot,
    ) {
        writeMutex.withLock {
            if (version == writeVersion.get()) {
                storage.writeSnapshot(snapshot)
            }
        }
    }
}

internal interface AppStorageGateway {
    suspend fun writeSnapshot(snapshot: AppStorageSnapshot)

    suspend fun readSnapshot(): AppStorageSnapshot

    suspend fun migrateIfNeeded()

    suspend fun clear()
}

internal class AppStorage(
    private val dataStore: DataStore<Preferences>,
) : AppStorageGateway {
    override suspend fun writeSnapshot(snapshot: AppStorageSnapshot) {
        dataStore.edit { preferences ->
            preferences[schemaVersionKey] = STORAGE_SCHEMA_VERSION
            preferences[snapshotKey] =
                JsonCodec.encodeToString(
                    AppStorageSnapshot.serializer(),
                    snapshot,
                )
        }
    }

    override suspend fun readSnapshot(): AppStorageSnapshot {
        val preferences = dataStore.data.first()
        val encoded = preferences[snapshotKey] ?: return AppStorageSnapshot()
        return runCatching {
            JsonCodec.decodeFromString(AppStorageSnapshot.serializer(), encoded)
        }.getOrElse {
            AppStorageSnapshot()
        }
    }

    override suspend fun migrateIfNeeded() {
        dataStore.edit { preferences ->
            val version = preferences[schemaVersionKey] ?: 0
            if (version < STORAGE_SCHEMA_VERSION) {
                preferences[schemaVersionKey] = STORAGE_SCHEMA_VERSION
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

internal fun observancesFor(
    year: Int,
    settings: RuleSettings,
): List<Observance> = ObservanceCalculator.makeCalendar(year, settings)

private fun loadDefaultState(): DashboardState {
    val settings = RuleSettings()
    val year = LocalDate.now().year
    return DashboardState(
        settings = settings,
        year = year,
        observances = observancesFor(year, settings),
    )
}

private fun AppStorageSnapshot.toDashboardState(): DashboardState =
    DashboardState(
        settings = settings,
        year = year,
        observances = ObservanceCalculator.makeCalendar(year, settings),
        statusesById = statusesById,
        fridayNotesById = fridayNotesById,
        planningData = planningData,
        schedules = schedules,
        activeIntermittentScheduleId = activeIntermittentScheduleId,
        intermittentSessions = intermittentSessions,
        activeIntermittentFast = activeIntermittentFast,
        intermittentPresetHours = boundedPresetHours(intermittentPresetHours),
        profiles = profiles,
        reflections = reflections,
        checklist = checklist,
        lastSyncDateIso = lastSyncDateIso,
        premiumCompanionState = premiumCompanionState,
        launchFunnelSnapshot = launchFunnelSnapshot,
    )

private fun DashboardState.toStorageSnapshot(): AppStorageSnapshot =
    AppStorageSnapshot(
        settings = settings,
        year = year,
        statusesById = statusesById,
        fridayNotesById = fridayNotesById,
        planningData = planningData,
        schedules = schedules,
        activeIntermittentScheduleId = activeIntermittentScheduleId,
        intermittentSessions = intermittentSessions,
        activeIntermittentFast = activeIntermittentFast,
        intermittentPresetHours = intermittentPresetHours,
        profiles = profiles,
        reflections = reflections,
        checklist = checklist,
        lastSyncDateIso = lastSyncDateIso,
        premiumCompanionState = premiumCompanionState,
        launchFunnelSnapshot = launchFunnelSnapshot,
    )

fun buildSyncSnapshot(state: DashboardState): SyncSnapshot {
    val completedCount = state.statusesById.values.count(CompletionStatus::countsTowardProgress)
    val fridayNotesCount =
        state.fridayNotesById.values.count { value ->
            value.trim().isNotEmpty()
        }
    val warnings =
        buildList {
            if (completedCount == 0 && fridayNotesCount > 0) {
                add("You have notes but no completed observances.")
            }
            if (completedCount > 200) {
                add("High completion count detected; review exports for duplicates.")
            }
            if (ObservanceCalculator.ruleBundleAudit().warnings.isNotEmpty()) {
                addAll(ObservanceCalculator.ruleBundleAudit().warnings)
            }
        }

    return SyncSnapshot(
        lastSyncDateIso = state.lastSyncDateIso,
        completedObservancesCount = completedCount,
        fridayNotesCount = fridayNotesCount,
        warnings = warnings,
    )
}

fun buildOnboardingState(state: DashboardState): OnboardingState =
    OnboardingState(
        isCompleted = state.launchFunnelSnapshot.completedOnboardingAtIso != null,
        currentStep =
            when {
                state.launchFunnelSnapshot.completedOnboardingAtIso != null -> 5
                !state.launchFunnelSnapshot.independentAppNoticeAcknowledged -> 1
                !state.launchFunnelSnapshot.regionSelected ||
                    state.launchFunnelSnapshot.selectedRegion != state.settings.regionProfile -> 2
                !state.launchFunnelSnapshot.reminderTierSelected -> 3
                !state.launchFunnelSnapshot.intermittentIntentionSelected -> 4
                else -> 5
            },
        totalSteps = 5,
        noticeAcknowledged = state.launchFunnelSnapshot.independentAppNoticeAcknowledged,
        selectedRegion = state.launchFunnelSnapshot.selectedRegion,
        regionSelected =
            state.launchFunnelSnapshot.regionSelected ||
                state.launchFunnelSnapshot.completedOnboardingAtIso != null,
        selectedReminderTier = state.launchFunnelSnapshot.selectedReminderTier,
        reminderTierSelected =
            state.launchFunnelSnapshot.reminderTierSelected ||
                state.launchFunnelSnapshot.completedOnboardingAtIso != null,
        dailyQuoteReminderEnabled = state.launchFunnelSnapshot.dailyQuoteReminderEnabled,
        dailyQuoteReminderHour = state.launchFunnelSnapshot.dailyQuoteReminderHour,
        dailyQuoteReminderMinute = state.launchFunnelSnapshot.dailyQuoteReminderMinute,
        selectedIntermittentIntentionId = state.launchFunnelSnapshot.selectedIntermittentIntentionId,
        intermittentIntentionSelected = state.launchFunnelSnapshot.intermittentIntentionSelected,
        hasFullBirthDate = state.settings.hasFullBirthDate,
    )

fun buildSetupProgressState(state: DashboardState): SetupProgressState {
    val birthProfileComplete = state.settings.hasFullBirthDate
    val independentNoticeAcknowledged = state.launchFunnelSnapshot.independentAppNoticeAcknowledged
    val onboardingCompleted = state.launchFunnelSnapshot.completedOnboardingAtIso != null
    val regionSelected =
        (
            state.launchFunnelSnapshot.regionSelected &&
                state.launchFunnelSnapshot.selectedRegion == state.settings.regionProfile
        ) ||
            onboardingCompleted
    val reminderTierSelected =
        state.launchFunnelSnapshot.reminderTierSelected || onboardingCompleted
    val intermittentIntentionSelected =
        state.launchFunnelSnapshot.intermittentIntentionSelected || onboardingCompleted
    val completedSteps =
        listOf(
            independentNoticeAcknowledged,
            regionSelected,
            reminderTierSelected,
            intermittentIntentionSelected,
            onboardingCompleted,
        ).count { it }
    return SetupProgressState(
        completedSteps = completedSteps,
        totalSteps = 5,
        birthProfileComplete = birthProfileComplete,
        independentNoticeAcknowledged = independentNoticeAcknowledged,
        regionSelected = regionSelected,
        reminderTierSelected = reminderTierSelected,
        intermittentIntentionSelected = intermittentIntentionSelected,
        onboardingCompleted = onboardingCompleted,
    )
}

fun buildReminderCenterState(state: DashboardState): ReminderCenterState =
    ReminderCenterState(
        selectedTier = state.launchFunnelSnapshot.selectedReminderTier,
        supportRemindersEnabled = state.launchFunnelSnapshot.selectedReminderTier.supportEnabled,
        morningCheckInEnabled = state.launchFunnelSnapshot.selectedReminderTier.morningEnabled,
        eveningCheckInEnabled = state.launchFunnelSnapshot.selectedReminderTier.eveningEnabled,
        dailyQuoteReminderEnabled = state.launchFunnelSnapshot.dailyQuoteReminderEnabled,
        dailyQuoteReminderHour = state.launchFunnelSnapshot.dailyQuoteReminderHour,
        dailyQuoteReminderMinute = state.launchFunnelSnapshot.dailyQuoteReminderMinute,
    )

fun buildStorageDiagnosticsState(state: DashboardState): StorageDiagnosticsState {
    val syncSnapshot = buildSyncSnapshot(state)
    return StorageDiagnosticsState(
        lastLocalWriteIso = state.lastSyncDateIso,
        completedObservancesCount = syncSnapshot.completedObservancesCount,
        fridayNotesCount = syncSnapshot.fridayNotesCount,
        intermittentSessionsCount = state.intermittentSessions.size,
        reflectionsCount = state.reflections.size,
        warnings = syncSnapshot.warnings,
    )
}

fun buildSeasonalHeroState(): SeasonalHeroState =
    buildSeasonalHeroState(
        locale = Locale.getDefault(),
        today = LocalDate.now(),
    )

internal fun buildSeasonalHeroState(
    locale: Locale,
    today: LocalDate,
): SeasonalHeroState {
    val contentLocale =
        when {
            locale.language.startsWith("es") ->
                com.kevpierce.catholicfasting.core.model.ContentLocale.SPANISH
            locale.language.startsWith("fr") ->
                com.kevpierce.catholicfasting.core.model.ContentLocale.FRENCH_CANADIAN
            else ->
                com.kevpierce.catholicfasting.core.model.ContentLocale.ENGLISH
        }
    val season = LiturgicalSeasonThemeEngine.seasonFor(today)
    val pack = SeasonalContentPackCatalog.pack(season = season, locale = contentLocale)
    return SeasonalHeroState(
        campaignTitle = pack.campaignTitle,
        campaignSubtitle = pack.campaignSubtitle,
        formationLine = SeasonalContentSupport.dailyFormationLine(pack, today),
        quote = SeasonalContentSupport.dailyQuote(season, pack, today),
        imagery = SacredImageryCatalog.fastingGallery.take(3),
    )
}

object AppContainer {
    @Volatile
    private var repositoryInstance: AppRepository? = null

    fun initialize(context: Context) {
        if (repositoryInstance == null) {
            synchronized(this) {
                if (repositoryInstance == null) {
                    repositoryInstance =
                        AppRepository(
                            storage = AppStorage(context.applicationContext.catholicFastingDataStore),
                        )
                }
            }
        }
    }

    val repository: AppRepository
        get() =
            checkNotNull(repositoryInstance) {
                "AppContainer.initialize(context) must be called before use."
            }

    fun resetForTesting(context: Context) {
        synchronized(this) {
            val storage = AppStorage(context.applicationContext.catholicFastingDataStore)
            runBlocking(Dispatchers.IO) {
                storage.clear()
            }
            repositoryInstance =
                AppRepository(
                    storage = storage,
                )
        }
    }

    fun reloadForTesting(context: Context) {
        synchronized(this) {
            repositoryInstance =
                AppRepository(
                    storage = AppStorage(context.applicationContext.catholicFastingDataStore),
                )
        }
    }
}

fun DashboardState.buildWidgetSnapshot(now: Instant = Instant.now()): WidgetSnapshot {
    val today = now.atZone(ZoneId.systemDefault()).toLocalDate()
    val todayKey = today.toString()
    val todayObservance = observances.firstOrNull { it.date == todayKey }
    val nextRequired =
        observances.firstOrNull {
            it.obligation == ObservanceObligation.MANDATORY && LocalDate.parse(it.date) >= today
        }
    val actionable = observances.filter { it.obligation != ObservanceObligation.NOT_APPLICABLE }
    val completed = actionable.count { statusesById[it.id]?.countsTowardProgress == true }
    val completionRate = if (actionable.isEmpty()) 0.0 else completed.toDouble() / actionable.size.toDouble()

    return WidgetSnapshot(
        generatedAtIso = now.toString(),
        todayTitle = todayObservance?.title ?: "No observance today",
        todayObligation = todayObservance?.obligation?.label ?: "No obligation",
        nextRequiredTitle = nextRequired?.title ?: "No upcoming required observance",
        nextRequiredDateIso = nextRequired?.date,
        completionRate = completionRate,
        hasActiveIntermittentFast = activeIntermittentFast != null,
        activeIntermittentFastStartIso = activeIntermittentFast?.startIso,
        activeIntermittentTargetHours =
            activeIntermittentFast?.targetHours ?: intermittentPresetHours,
    )
}

fun defaultReminderTier(): ReminderTier =
    ReminderTier.infer(
        supportEnabled = true,
        morningEnabled = true,
        eveningEnabled = false,
    )

fun defaultPremiumCatalog(): SubscriptionOfferCatalog = SubscriptionOfferCatalog.catholicFasting
