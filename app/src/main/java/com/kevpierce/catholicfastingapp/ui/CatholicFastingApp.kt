@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfastingapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.core.content.ContextCompat
import com.kevpierce.catholicfasting.core.billing.BillingContainer
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.data.buildOnboardingState
import com.kevpierce.catholicfasting.core.data.buildReminderCenterState
import com.kevpierce.catholicfasting.core.data.buildSeasonalHeroState
import com.kevpierce.catholicfasting.core.data.buildSetupProgressState
import com.kevpierce.catholicfasting.core.data.buildStorageDiagnosticsState
import com.kevpierce.catholicfasting.core.data.buildSyncSnapshot
import com.kevpierce.catholicfasting.core.data.buildWidgetSnapshot
import com.kevpierce.catholicfasting.core.model.CatholicFastingQuote
import com.kevpierce.catholicfasting.core.model.ContentLocale
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.OnboardingState
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderCenterState
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.model.RuleBundleAudit
import com.kevpierce.catholicfasting.core.model.SacredImageryItem
import com.kevpierce.catholicfasting.core.model.SeasonalContentPack
import com.kevpierce.catholicfasting.core.model.SeasonalHeroState
import com.kevpierce.catholicfasting.core.model.SetupProgressState
import com.kevpierce.catholicfasting.core.model.StorageDiagnosticsState
import com.kevpierce.catholicfasting.core.model.SyncSnapshot
import com.kevpierce.catholicfasting.core.rules.ObservanceCalculator
import com.kevpierce.catholicfasting.core.rules.PremiumFastPrepGuidanceEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSeasonProgramEngine
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshotEngine
import com.kevpierce.catholicfasting.core.rules.SacredImageryCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentPackCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentSupport
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.SeasonTone
import com.kevpierce.catholicfasting.core.ui.catholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.rememberSeasonTone
import com.kevpierce.catholicfasting.core.widget.WidgetSnapshotStore
import com.kevpierce.catholicfasting.feature.calendar.calendarScreen
import com.kevpierce.catholicfasting.feature.guidance.guidanceScreen
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceActions
import com.kevpierce.catholicfasting.feature.premium.PremiumWorkspaceUiState
import com.kevpierce.catholicfasting.feature.premium.premiumScreen
import com.kevpierce.catholicfasting.feature.settings.settingsScreen
import com.kevpierce.catholicfasting.feature.today.TodayUiState
import com.kevpierce.catholicfasting.feature.today.todayScreen
import com.kevpierce.catholicfasting.feature.tracker.TrackerActions
import com.kevpierce.catholicfasting.feature.tracker.TrackerUiState
import com.kevpierce.catholicfasting.feature.tracker.trackerScreen
import com.kevpierce.catholicfastingapp.R
import com.kevpierce.catholicfastingapp.notifications.IntermittentFastNotificationManager
import com.kevpierce.catholicfastingapp.notifications.ReminderScheduler
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

private data class AppSupportState(
    val premiumSnapshot: PremiumSnapshot,
    val seasonProgramActions: List<String>,
    val fastPrepGuidance: List<String>,
    val syncSnapshot: SyncSnapshot,
    val onboardingState: OnboardingState,
    val setupProgressState: SetupProgressState,
    val reminderCenterState: ReminderCenterState,
    val storageDiagnosticsState: StorageDiagnosticsState,
    val seasonalHeroState: SeasonalHeroState,
    val ruleBundleAudit: RuleBundleAudit,
    val seasonalContentPack: SeasonalContentPack,
    val dailyFormationLine: String,
    val dailyQuote: CatholicFastingQuote,
    val devotionalGallery: List<SacredImageryItem>,
    val setupProgressSummary: String,
    val yearPlanSummary: String,
    val weeklyRecap: String,
    val streakMessage: String,
)

private data class BillingActions(
    val onRefresh: () -> Unit,
    val onManageSubscription: () -> Unit,
    val onPurchase: (String) -> Unit,
)

private data class NotificationPermissionActions(
    val granted: Boolean,
    val requestPermission: () -> Unit,
)

private data class SetupReminderActions(
    val onReminderTierChange: (ReminderTier) -> Unit,
    val onDailyQuoteReminderEnabledChange: (Boolean) -> Unit,
    val onDailyQuoteReminderTimeChange: (Int, Int) -> Unit,
    val onNoticeAcknowledgedChange: (Boolean) -> Unit,
    val onCompleteOnboarding: () -> Unit,
)

@Composable
fun catholicFastingApp(initialDeepLink: String? = null) {
    val repository = AppContainer.repository
    val billingRepository = BillingContainer.repository
    val state by repository.dashboardState.collectAsState()
    val billingState by billingRepository.billingState.collectAsState()
    val launchDestination = AppRouteResolver.resolve(initialDeepLink)
    val initialDestination = launchDestination.topLevelDestination
    val initialMoreSection = launchDestination.moreSection
    var destination by rememberSaveable(initialDeepLink) { mutableStateOf(initialDestination) }
    LaunchedEffect(initialDeepLink, initialDestination) {
        destination = initialDestination
    }
    val context = LocalContext.current
    val notificationPermissionActions =
        rememberNotificationPermissionActions(
            context = context,
            refreshKey =
                listOf(
                    destination,
                    state.launchFunnelSnapshot.selectedReminderTier,
                    state.launchFunnelSnapshot.completedOnboardingAtIso,
                ),
        )
    appRuntimeEffects(context = context, state = state)

    if (state.launchFunnelSnapshot.completedOnboardingAtIso == null) {
        onboardingRoute(
            state = state,
            repository = repository,
            notificationPermissionActions = notificationPermissionActions,
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    Scaffold(
        bottomBar = { bottomNavigation(destination = destination, onDestinationChange = { destination = it }) },
    ) { padding ->
        appContent(
            destination = destination,
            initialMoreSection = initialMoreSection,
            state = state,
            repository = repository,
            billingState = billingState,
            billingActions =
                BillingActions(
                    onRefresh = billingRepository::refresh,
                    onManageSubscription = billingRepository::openManageSubscription,
                    onPurchase = { productId ->
                        (context as? ComponentActivity)?.let { activity ->
                            billingRepository.launchPurchase(activity, productId)
                        }
                    },
                ),
            notificationPermissionActions = notificationPermissionActions,
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}

@Composable
private fun appRuntimeEffects(
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

@Composable
private fun bottomNavigation(
    destination: TopLevelDestination,
    onDestinationChange: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        listOf(
            TopLevelDestination.TODAY to "T",
            TopLevelDestination.FASTING_DAYS to "F",
            TopLevelDestination.TRACK_FAST to "TF",
            TopLevelDestination.MORE to "M",
        ).forEach { (item, shortLabel) ->
            val itemLabel = stringResource(item.labelRes())
            NavigationBarItem(
                selected = item == destination,
                onClick = { onDestinationChange(item) },
                icon = { Text(shortLabel, modifier = Modifier.clearAndSetSemantics { }) },
                label = { Text(itemLabel) },
                modifier =
                    Modifier.semantics {
                        contentDescription = itemLabel
                    },
            )
        }
    }
}

@Composable
private fun appContent(
    destination: TopLevelDestination,
    initialMoreSection: MoreSection,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    notificationPermissionActions: NotificationPermissionActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val supportState = buildAppSupportState(context, state)
    val spacing = CatholicFastingThemeValues.spacing

    when (destination) {
        TopLevelDestination.TODAY ->
            todayScreen(
                uiState =
                    TodayUiState(
                        todayObservance = state.observances.firstOrNull { it.date == LocalDate.now().toString() },
                        completionSummary = completionSummary(context, state),
                        premiumSnapshot = supportState.premiumSnapshot,
                        seasonalContentPack = supportState.seasonalContentPack,
                        dailyFormationLine = supportState.dailyFormationLine,
                        dailyQuote = supportState.dailyQuote,
                        devotionalGallery = supportState.devotionalGallery,
                        setupProgressSummary = supportState.setupProgressSummary,
                        yearPlanSummary = supportState.yearPlanSummary,
                        weeklyRecap = supportState.weeklyRecap,
                        streakMessage = supportState.streakMessage,
                        noticeSummary = stringResource(R.string.notice_independent_app_summary),
                    ),
                modifier = modifier,
            )
        TopLevelDestination.FASTING_DAYS ->
            calendarScreen(
                observances = state.observances,
                statusesById = state.statusesById,
                fridayNotesById = state.fridayNotesById,
                premiumSnapshot = supportState.premiumSnapshot,
                onStatusChange = repository::setStatus,
                onFridayNoteChange = repository::setFridayNote,
                modifier = modifier,
            )
        TopLevelDestination.TRACK_FAST ->
            trackFastDestination(
                state = state,
                repository = repository,
                supportState = supportState,
                modifier = modifier,
            )
        TopLevelDestination.MORE ->
            moreDestination(
                initialSection = initialMoreSection,
                state = state,
                repository = repository,
                onSettingsChange = repository::updateSettings,
                billingState = billingState,
                billingActions = billingActions,
                notificationPermissionActions = notificationPermissionActions,
                supportState = supportState,
                modifier = modifier,
            )
    }
}

@Composable
private fun onboardingRoute(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    notificationPermissionActions: NotificationPermissionActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val supportState = buildAppSupportState(context, state)
    val onboardingState = supportState.onboardingState
    val completionState =
        SetupCompletionState.from(
            setupProgressState = supportState.setupProgressState,
            notificationPermissionGranted = notificationPermissionActions.granted,
            notificationPermissionSupported = notificationPermissionSupported(),
        )

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(CatholicFastingThemeValues.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.small),
    ) {
        onboardingHeaderCard(
            onboardingState = onboardingState,
            setupProgressSummary = supportState.setupProgressSummary,
        )
        onboardingNoticeCard(
            noticeAcknowledged = onboardingState.noticeAcknowledged,
            onNoticeAcknowledgedChange = repository::setIndependentAppNoticeAcknowledged,
        )
        onboardingProfileCard(
            state = state,
            onRegionSelected = repository::setSelectedRegion,
            onFridayModeSelected = { mode ->
                repository.updateSettings(
                    state.settings.copy(
                        fridayOutsideLentMode = mode,
                    ),
                )
            },
        )
        onboardingReminderCard(
            onboardingState = onboardingState,
            reminderCenterState = supportState.reminderCenterState,
            notificationPermissionGranted = notificationPermissionActions.granted,
            onRequestNotificationPermission = notificationPermissionActions.requestPermission,
            onReminderTierChange = repository::setReminderTier,
            onDailyQuoteReminderEnabledChange = repository::setDailyQuoteReminderEnabled,
            onDailyQuoteReminderTimeChange = repository::setDailyQuoteReminderTime,
        )
        onboardingPremiumCard(
            seasonalHeroState = supportState.seasonalHeroState,
            premiumSnapshot = supportState.premiumSnapshot,
        )
        outlinedActionButton(
            label = stringResource(R.string.onboarding_finish),
            onClick = repository::completeOnboarding,
            enabled = completionState.canCompleteOnboarding,
        )
        if (!completionState.canCompleteOnboarding) {
            Text(stringResource(R.string.onboarding_finish_blocked))
        }
    }
}

@Composable
private fun onboardingHeaderCard(
    onboardingState: OnboardingState,
    setupProgressSummary: String,
) {
    sectionCard(
        title = stringResource(R.string.onboarding_title),
        heroTitle = true,
    ) {
        Text(stringResource(R.string.onboarding_subtitle), style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(
                R.string.onboarding_step_value,
                onboardingState.currentStep,
                onboardingState.totalSteps,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(setupProgressSummary, style = CatholicFastingThemeValues.typography.supporting)
    }
}

@Composable
private fun onboardingNoticeCard(
    noticeAcknowledged: Boolean,
    onNoticeAcknowledgedChange: (Boolean) -> Unit,
) {
    sectionCard(title = stringResource(R.string.onboarding_notice_title)) {
        Text(
            stringResource(R.string.notice_independent_app_summary),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            if (noticeAcknowledged) {
                stringResource(R.string.more_notice_acknowledged)
            } else {
                stringResource(R.string.more_notice_pending)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        booleanChoiceRow(
            selected = noticeAcknowledged,
            onSelectionChange = onNoticeAcknowledgedChange,
            trueLabel = stringResource(R.string.onboarding_notice_accept),
            falseLabel = stringResource(R.string.onboarding_notice_review),
        )
    }
}

@Composable
private fun onboardingProfileCard(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    onRegionSelected: (RegionProfile) -> Unit,
    onFridayModeSelected: (FridayOutsideLentMode) -> Unit,
) {
    sectionCard(title = stringResource(R.string.onboarding_profile_title)) {
        Text(
            stringResource(R.string.onboarding_profile_body),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            if (state.settings.hasFullBirthDate) {
                stringResource(R.string.more_birth_profile_complete)
            } else {
                stringResource(R.string.onboarding_profile_follow_up)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(R.string.onboarding_region_title),
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        rowWithScroll {
            RegionProfile.entries.forEach { region ->
                val regionLabel = region.localizedLabel()
                val regionStateDescription = selectedStateDescription(state.settings.regionProfile == region)
                FilterChip(
                    selected = state.settings.regionProfile == region,
                    onClick = { onRegionSelected(region) },
                    label = { Text(regionLabel) },
                    modifier =
                        Modifier.semantics {
                            contentDescription = regionLabel
                            stateDescription = regionStateDescription
                        },
                )
            }
        }
        Text(
            stringResource(R.string.onboarding_friday_title),
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        rowWithScroll {
            FridayOutsideLentMode.entries.forEach { mode ->
                val modeLabel = mode.localizedLabel()
                val modeStateDescription =
                    selectedStateDescription(state.settings.fridayOutsideLentMode == mode)
                FilterChip(
                    selected = state.settings.fridayOutsideLentMode == mode,
                    onClick = { onFridayModeSelected(mode) },
                    label = { Text(modeLabel) },
                    modifier =
                        Modifier.semantics {
                            contentDescription = modeLabel
                            stateDescription = modeStateDescription
                        },
                )
            }
        }
    }
}

@Composable
private fun onboardingReminderCard(
    onboardingState: OnboardingState,
    reminderCenterState: ReminderCenterState,
    notificationPermissionGranted: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onReminderTierChange: (ReminderTier) -> Unit,
    onDailyQuoteReminderEnabledChange: (Boolean) -> Unit,
    onDailyQuoteReminderTimeChange: (Int, Int) -> Unit,
) {
    sectionCard(title = stringResource(R.string.onboarding_reminders_title)) {
        Text(stringResource(R.string.onboarding_reminders_body), style = CatholicFastingThemeValues.typography.body)
        reminderTierChipRow(
            selectedTier = onboardingState.selectedReminderTier,
            onReminderTierChange = onReminderTierChange,
        )
        quoteReminderControls(
            reminderCenterState = reminderCenterState,
            labelRes = R.string.onboarding_quote_reminder_value,
            selectedEnabled = onboardingState.dailyQuoteReminderEnabled,
            onDailyQuoteReminderEnabledChange = onDailyQuoteReminderEnabledChange,
            onDailyQuoteReminderTimeChange = onDailyQuoteReminderTimeChange,
        )
        Text(
            if (notificationPermissionGranted || !notificationPermissionSupported()) {
                stringResource(R.string.more_notification_permission_granted)
            } else {
                stringResource(R.string.onboarding_notification_permission_needed)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        if (notificationPermissionSupported() && !notificationPermissionGranted) {
            outlinedActionButton(
                label = stringResource(R.string.onboarding_request_notification_permission),
                onClick = onRequestNotificationPermission,
            )
        }
    }
}

@Composable
private fun onboardingPremiumCard(
    seasonalHeroState: SeasonalHeroState,
    premiumSnapshot: PremiumSnapshot,
) {
    val tone = rememberSeasonTone(premiumSnapshot.season)

    sectionCard(
        title = stringResource(R.string.onboarding_premium_title),
        tone = tone,
    ) {
        Text(stringResource(R.string.onboarding_premium_body), style = CatholicFastingThemeValues.typography.body)
        Text(seasonalHeroState.campaignTitle, style = CatholicFastingThemeValues.typography.heroTitle)
        Text(seasonalHeroState.campaignSubtitle, style = CatholicFastingThemeValues.typography.sectionTitle)
        Text(seasonalHeroState.formationLine, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(
                R.string.onboarding_quote_card_value,
                seasonalHeroState.quote.text,
                seasonalHeroState.quote.author,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(premiumSnapshot.recoveryCoachPlan.summary, style = CatholicFastingThemeValues.typography.supporting)
    }
}

private fun completionSummary(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
): String {
    val completedCount = state.statusesById.count { it.value.countsTowardProgress }
    return context.getString(R.string.summary_completion_value, completedCount)
}

@Composable
@Suppress("LongMethod")
private fun trackFastDestination(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    supportState: AppSupportState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    trackerScreen(
        uiState =
            TrackerUiState(
                schedules = state.schedules,
                activeScheduleId = state.activeIntermittentScheduleId,
                sessions = state.intermittentSessions,
                activeFast = state.activeIntermittentFast,
                presetHours = state.intermittentPresetHours,
                premiumSnapshot = supportState.premiumSnapshot,
                prepGuidance = supportState.fastPrepGuidance,
                seasonProgramActions = supportState.seasonProgramActions,
            ),
        actions =
            TrackerActions(
                onPresetHoursChange = repository::setIntermittentPresetHours,
                onStartFast = repository::startIntermittentFast,
                onEndFast = repository::endIntermittentFast,
                onCancelFast = repository::cancelIntermittentFast,
                onSaveSchedule = { scheduleId, name, startHour, weekdays ->
                    repository
                        .saveIntermittentSchedule(
                            scheduleId = scheduleId,
                            name = name,
                            startHour = startHour,
                            weekdays = weekdays,
                        ).fold(
                            onSuccess = { plan ->
                                context.getString(R.string.status_schedule_saved, plan.name)
                            },
                            onFailure = {
                                it.message ?: context.getString(R.string.status_schedule_save_failed)
                            },
                        )
                },
                onDeleteSchedule = { scheduleId ->
                    repository.deleteIntermittentSchedule(scheduleId)
                        .fold(
                            onSuccess = { context.getString(R.string.status_schedule_deleted) },
                            onFailure = {
                                it.message ?: context.getString(R.string.status_schedule_delete_failed)
                            },
                        )
                },
                onApplySchedule = { scheduleId ->
                    repository.applyIntermittentSchedule(scheduleId)
                        .fold(
                            onSuccess = { plan ->
                                context.getString(R.string.status_schedule_applied, plan.name)
                            },
                            onFailure = {
                                it.message ?: context.getString(R.string.status_schedule_apply_failed)
                            },
                        )
                },
            ),
        modifier = modifier,
    )
}

@Composable
private fun moreDestination(
    initialSection: MoreSection,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    onSettingsChange: (com.kevpierce.catholicfasting.core.model.RuleSettings) -> Unit,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    notificationPermissionActions: NotificationPermissionActions,
    supportState: AppSupportState,
    modifier: Modifier = Modifier,
) {
    var section by rememberSaveable(initialSection) { mutableStateOf(initialSection) }
    LaunchedEffect(initialSection) {
        section = initialSection
    }

    Column(modifier = modifier) {
        moreSectionTabs(
            selected = section,
            onSelected = { section = it },
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            moreSectionContent(
                section = section,
                state = state,
                repository = repository,
                onSettingsChange = onSettingsChange,
                billingState = billingState,
                billingActions = billingActions,
                notificationPermissionActions = notificationPermissionActions,
                supportState = supportState,
            )
        }
    }
}

@Composable
private fun moreSectionTabs(
    selected: MoreSection,
    onSelected: (MoreSection) -> Unit,
) {
    val context = LocalContext.current
    val spacing = CatholicFastingThemeValues.spacing
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.medium, vertical = spacing.small),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        Text(
            stringResource(R.string.more_title),
            style = CatholicFastingThemeValues.typography.screenTitle,
            modifier = Modifier.padding(horizontal = spacing.xxSmall),
        )
        rowWithScroll {
            MoreSection.entries.forEach { section ->
                FilterChip(
                    selected = selected == section,
                    onClick = { onSelected(section) },
                    label = { Text(stringResource(section.labelRes())) },
                )
            }
        }
    }
}

@Composable
private fun setupAndRemindersSection(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    actions: SetupReminderActions,
    notificationPermissionActions: NotificationPermissionActions,
    supportState: AppSupportState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(CatholicFastingThemeValues.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.small),
    ) {
        quickSetupCard(
            state = state,
            setupProgressSummary = supportState.setupProgressSummary,
            onNoticeAcknowledgedChange = actions.onNoticeAcknowledgedChange,
            onCompleteOnboarding = actions.onCompleteOnboarding,
        )
        reminderCenterCard(
            reminderCenterState = supportState.reminderCenterState,
            summaryLine = supportState.premiumSnapshot.reminderRecommendation.summaryLine,
            notificationPermissionGranted = notificationPermissionActions.granted,
            onRequestNotificationPermission = notificationPermissionActions.requestPermission,
            onReminderTierChange = actions.onReminderTierChange,
            onDailyQuoteReminderEnabledChange = actions.onDailyQuoteReminderEnabledChange,
            onDailyQuoteReminderTimeChange = actions.onDailyQuoteReminderTimeChange,
        )
        sectionCard(title = stringResource(R.string.more_setup_progress_title)) {
            Text(supportState.setupProgressSummary)
            Text(stringResource(R.string.more_region_value, state.settings.regionProfile.localizedLabel()))
            Text(stringResource(R.string.more_calendar_value, state.settings.calendarMode.localizedLabel()))
            Text(
                stringResource(
                    R.string.more_reminder_strategy_value,
                    state.launchFunnelSnapshot.selectedReminderTier.localizedLabel(),
                ),
            )
            Text(
                if (state.settings.hasFullBirthDate) {
                    stringResource(R.string.more_birth_profile_complete)
                } else {
                    stringResource(R.string.more_birth_profile_partial)
                },
            )
            Text(stringResource(R.string.more_profiles_stored, state.profiles.size))
        }
    }
}

@Composable
private fun quickSetupCard(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    setupProgressSummary: String,
    onNoticeAcknowledgedChange: (Boolean) -> Unit,
    onCompleteOnboarding: () -> Unit,
) {
    sectionCard(title = stringResource(R.string.more_quick_setup_title)) {
        Text(stringResource(R.string.more_quick_setup_body), style = CatholicFastingThemeValues.typography.body)
        Text(
            if (state.launchFunnelSnapshot.independentAppNoticeAcknowledged) {
                stringResource(R.string.more_notice_acknowledged)
            } else {
                stringResource(R.string.more_notice_pending)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        rowWithScroll {
            listOf(true, false).forEach { acknowledged ->
                FilterChip(
                    selected =
                        state.launchFunnelSnapshot.independentAppNoticeAcknowledged == acknowledged,
                    onClick = { onNoticeAcknowledgedChange(acknowledged) },
                    label = {
                        Text(
                            if (acknowledged) {
                                stringResource(R.string.more_notice_acknowledged_chip)
                            } else {
                                stringResource(R.string.more_notice_pending_chip)
                            },
                        )
                    },
                )
            }
        }
        Text(setupProgressSummary, style = CatholicFastingThemeValues.typography.supporting)
        outlinedActionButton(
            label =
                if (state.launchFunnelSnapshot.completedOnboardingAtIso == null) {
                    stringResource(R.string.more_mark_setup_complete)
                } else {
                    stringResource(R.string.more_refresh_setup_complete)
                },
            onClick = onCompleteOnboarding,
        )
    }
}

@Composable
private fun reminderCenterCard(
    reminderCenterState: ReminderCenterState,
    summaryLine: String,
    notificationPermissionGranted: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onReminderTierChange: (ReminderTier) -> Unit,
    onDailyQuoteReminderEnabledChange: (Boolean) -> Unit,
    onDailyQuoteReminderTimeChange: (Int, Int) -> Unit,
) {
    sectionCard(title = stringResource(R.string.more_reminder_center_title)) {
        Text(summaryLine, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(
                R.string.more_reminder_tier_value,
                reminderCenterState.selectedTier.localizedLabel(),
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            reminderCenterState.selectedTier.localizedSummary(),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        reminderTierChipRow(
            selectedTier = reminderCenterState.selectedTier,
            onReminderTierChange = onReminderTierChange,
        )
        quoteReminderControls(
            reminderCenterState = reminderCenterState,
            labelRes = R.string.more_quote_reminder_time_value,
            selectedEnabled = reminderCenterState.dailyQuoteReminderEnabled,
            onDailyQuoteReminderEnabledChange = onDailyQuoteReminderEnabledChange,
            onDailyQuoteReminderTimeChange = onDailyQuoteReminderTimeChange,
        )
        Text(
            stringResource(R.string.more_reminder_strategy_body),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            if (notificationPermissionGranted) {
                stringResource(R.string.more_notification_permission_granted)
            } else {
                stringResource(R.string.more_notification_permission_needed)
            },
            style = CatholicFastingThemeValues.typography.supporting,
        )
        if (notificationPermissionSupported() && !notificationPermissionGranted) {
            outlinedActionButton(
                label = stringResource(R.string.more_request_notification_permission),
                onClick = onRequestNotificationPermission,
            )
        }
        Text(stringResource(R.string.more_required_day_local), style = CatholicFastingThemeValues.typography.utility)
        Text(stringResource(R.string.more_active_fast_sync), style = CatholicFastingThemeValues.typography.utility)
    }
}

@Composable
private fun reminderTierChipRow(
    selectedTier: ReminderTier,
    onReminderTierChange: (ReminderTier) -> Unit,
) {
    rowWithScroll {
        ReminderTier.entries.forEach { tier ->
            val tierLabel = tier.localizedLabel()
            val tierStateDescription = selectedStateDescription(selectedTier == tier)
            FilterChip(
                selected = selectedTier == tier,
                onClick = { onReminderTierChange(tier) },
                label = { Text(tierLabel) },
                modifier =
                    Modifier.semantics {
                        contentDescription = tierLabel
                        stateDescription = tierStateDescription
                    },
            )
        }
    }
}

@Composable
private fun quoteReminderControls(
    reminderCenterState: ReminderCenterState,
    labelRes: Int,
    selectedEnabled: Boolean,
    onDailyQuoteReminderEnabledChange: (Boolean) -> Unit,
    onDailyQuoteReminderTimeChange: (Int, Int) -> Unit,
) {
    Text(
        stringResource(
            labelRes,
            reminderCenterState.dailyQuoteTimeLabel,
        ),
        style = CatholicFastingThemeValues.typography.supporting,
    )
    booleanChoiceRow(
        selected = selectedEnabled,
        onSelectionChange = onDailyQuoteReminderEnabledChange,
        trueLabel = stringResource(R.string.onboarding_quote_reminder_on),
        falseLabel = stringResource(R.string.onboarding_quote_reminder_off),
    )
    if (reminderCenterState.dailyQuoteReminderEnabled) {
        rowWithScroll {
            listOf(6, 7, 8, 9).forEach { hour ->
                val hourLabel = stringResource(R.string.onboarding_hour_value, hour)
                val hourStateDescription =
                    selectedStateDescription(reminderCenterState.dailyQuoteReminderHour == hour)
                FilterChip(
                    selected = reminderCenterState.dailyQuoteReminderHour == hour,
                    onClick = {
                        onDailyQuoteReminderTimeChange(
                            hour,
                            reminderCenterState.dailyQuoteReminderMinute,
                        )
                    },
                    label = { Text(hourLabel) },
                    modifier =
                        Modifier.semantics {
                            contentDescription = hourLabel
                            stateDescription = hourStateDescription
                        },
                )
            }
        }
    }
}

@Composable
private fun booleanChoiceRow(
    selected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    trueLabel: String,
    falseLabel: String,
) {
    rowWithScroll {
        listOf(true to trueLabel, false to falseLabel).forEach { (value, label) ->
            val optionStateDescription = selectedStateDescription(selected == value)
            FilterChip(
                selected = selected == value,
                onClick = { onSelectionChange(value) },
                label = { Text(label) },
                modifier =
                    Modifier.semantics {
                        contentDescription = label
                        stateDescription = optionStateDescription
                    },
            )
        }
    }
}

@Composable
private fun privacyAndDataSection(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    supportState: AppSupportState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(CatholicFastingThemeValues.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.small),
    ) {
        privacySummaryCard(state = state, supportState = supportState)
        dataStoredCard()
        currentLocalStateCard(state = state, supportState = supportState)
    }
}

@Composable
private fun privacySummaryCard(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    supportState: AppSupportState,
) {
    sectionCard(title = stringResource(R.string.more_privacy_title)) {
        Text(
            stringResource(R.string.more_privacy_local_first),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.more_privacy_backup_tools),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_privacy_last_sync,
                supportState.storageDiagnosticsState.lastLocalWriteIso ?: stringResource(R.string.more_not_yet_saved),
            ),
            style = CatholicFastingThemeValues.typography.utility,
        )
        Text(
            if (state.launchFunnelSnapshot.independentAppNoticeAcknowledged) {
                stringResource(R.string.more_notice_acknowledged)
            } else {
                stringResource(R.string.more_notice_not_acknowledged)
            },
            style = CatholicFastingThemeValues.typography.utility,
        )
        Text(
            stringResource(R.string.more_stored_reflections, state.reflections.size),
            style = CatholicFastingThemeValues.typography.utility,
        )
        Text(
            stringResource(R.string.more_household_profiles, state.profiles.size),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}

@Composable
private fun dataStoredCard() {
    sectionCard(title = stringResource(R.string.more_data_stored_title)) {
        Text(
            stringResource(R.string.more_data_profile_settings),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.more_data_observances),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.more_data_funnel),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(R.string.more_data_no_tracking),
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@Composable
private fun currentLocalStateCard(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    supportState: AppSupportState,
) {
    sectionCard(title = stringResource(R.string.more_current_local_state_title)) {
        Text(
            stringResource(R.string.more_observances_this_year, state.observances.size),
            style = CatholicFastingThemeValues.typography.body,
        )
        Text(
            stringResource(
                R.string.more_completed_count,
                supportState.storageDiagnosticsState.completedObservancesCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_friday_notes_saved,
                supportState.storageDiagnosticsState.fridayNotesCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_intermittent_sessions_saved,
                supportState.storageDiagnosticsState.intermittentSessionsCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_stored_reflections,
                supportState.storageDiagnosticsState.reflectionsCount,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.more_selected_reminder_tier,
                supportState.reminderCenterState.selectedTier.localizedLabel(),
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        if (supportState.storageDiagnosticsState.warnings.isNotEmpty()) {
            supportState.storageDiagnosticsState.warnings.forEach { warning ->
                Text(
                    stringResource(R.string.more_warning_value, warning),
                    style = CatholicFastingThemeValues.typography.utility,
                )
            }
        }
    }
}

@Composable
private fun sectionCard(
    title: String,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    catholicFastingSectionCard(
        title = title,
        tone = tone,
        heroTitle = heroTitle,
        content = content,
    )
}

@Composable
private fun rowWithScroll(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.xSmall),
    ) {
        content()
    }
}

@Composable
private fun outlinedActionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    androidx.compose.material3.OutlinedButton(onClick = onClick, enabled = enabled) {
        Text(label)
    }
}

private fun TopLevelDestination.labelRes(): Int =
    when (this) {
        TopLevelDestination.TODAY -> R.string.nav_today
        TopLevelDestination.FASTING_DAYS -> R.string.nav_fasting_days
        TopLevelDestination.TRACK_FAST -> R.string.nav_track_fast
        TopLevelDestination.MORE -> R.string.nav_more
    }

private fun MoreSection.labelRes(): Int =
    when (this) {
        MoreSection.SUPPORT_PREMIUM -> R.string.more_support_premium
        MoreSection.SETUP_REMINDERS -> R.string.more_setup_reminders
        MoreSection.PROFILE_NORMS -> R.string.more_profile_norms
        MoreSection.GUIDANCE_RULES -> R.string.more_guidance_rules
        MoreSection.PRIVACY_DATA -> R.string.more_privacy_data
    }

private fun notificationsEnabled(context: Context): Boolean =
    !notificationPermissionSupported() ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

private fun notificationPermissionSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@Composable
private fun rememberNotificationPermissionActions(
    context: Context,
    refreshKey: List<Any?>,
): NotificationPermissionActions {
    var notificationPermissionGranted by rememberSaveable {
        mutableStateOf(notificationsEnabled(context))
    }
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            notificationPermissionGranted = granted
        }

    LaunchedEffect(refreshKey) {
        notificationPermissionGranted = notificationsEnabled(context)
    }

    return NotificationPermissionActions(
        granted = notificationPermissionGranted,
        requestPermission = {
            if (notificationPermissionSupported() && !notificationPermissionGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
    )
}

@Composable
private fun moreSectionContent(
    section: MoreSection,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    onSettingsChange: (com.kevpierce.catholicfasting.core.model.RuleSettings) -> Unit,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    notificationPermissionActions: NotificationPermissionActions,
    supportState: AppSupportState,
) {
    when (section) {
        MoreSection.SUPPORT_PREMIUM ->
            supportAndPremiumSection(
                state = state,
                repository = repository,
                billingState = billingState,
                billingActions = billingActions,
                supportState = supportState,
            )
        MoreSection.SETUP_REMINDERS ->
            setupAndRemindersSection(
                state = state,
                actions =
                    SetupReminderActions(
                        onReminderTierChange = repository::setReminderTier,
                        onDailyQuoteReminderEnabledChange = repository::setDailyQuoteReminderEnabled,
                        onDailyQuoteReminderTimeChange = repository::setDailyQuoteReminderTime,
                        onNoticeAcknowledgedChange = repository::setIndependentAppNoticeAcknowledged,
                        onCompleteOnboarding = repository::completeOnboarding,
                    ),
                notificationPermissionActions = notificationPermissionActions,
                supportState = supportState,
                modifier = Modifier.fillMaxSize(),
            )
        MoreSection.PROFILE_NORMS ->
            settingsScreen(
                settings = state.settings,
                onSettingsChange = onSettingsChange,
                modifier = Modifier.fillMaxSize(),
            )
        MoreSection.GUIDANCE_RULES ->
            guidanceScreen(
                settings = state.settings,
                ruleBundleAudit = supportState.ruleBundleAudit,
                modifier = Modifier.fillMaxSize(),
            )
        MoreSection.PRIVACY_DATA ->
            privacyAndDataSection(
                state = state,
                supportState = supportState,
                modifier = Modifier.fillMaxSize(),
            )
    }
}

@Composable
private fun supportAndPremiumSection(
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    supportState: AppSupportState,
) {
    val context = LocalContext.current
    premiumScreen(
        billingState = billingState,
        workspaceState =
            PremiumWorkspaceUiState(
                planningData = state.planningData,
                reflections = state.reflections,
                premiumSnapshot = supportState.premiumSnapshot,
                seasonProgramActions = supportState.seasonProgramActions,
                fastPrepGuidance = supportState.fastPrepGuidance,
            ),
        actions =
            PremiumWorkspaceActions(
                onRefresh = billingActions.onRefresh,
                onManageSubscription = billingActions.onManageSubscription,
                onPurchase = billingActions.onPurchase,
                onSaveReflection = { title, body ->
                    repository.addReflectionEntry(title, body)
                        .fold(
                            onSuccess = { context.getString(R.string.status_reflection_saved) },
                            onFailure = {
                                it.message ?: context.getString(R.string.status_reflection_save_failed)
                            },
                        )
                },
            ),
        modifier = Modifier.fillMaxSize(),
    )
}

private fun buildAppSupportState(
    context: Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
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
    if (Locale.getDefault().language.startsWith("es")) {
        ContentLocale.SPANISH
    } else {
        ContentLocale.ENGLISH
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
        context.getString(R.string.summary_weekly_recap_value, completed, weeklyActionable.size)
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
