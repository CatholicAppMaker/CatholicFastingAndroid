package com.kevpierce.catholicfastingapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.billing.BillingContainer
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.model.CompanionActionDestination
import com.kevpierce.catholicfasting.core.model.CompanionNextAction
import com.kevpierce.catholicfasting.feature.calendar.CalendarScreen
import com.kevpierce.catholicfasting.feature.today.TodayScreen
import com.kevpierce.catholicfasting.feature.today.TodayUiState
import com.kevpierce.catholicfastingapp.R
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

@Composable
fun CatholicFastingApp(
    initialDeepLink: String? = null,
    clock: Clock = Clock.systemDefaultZone(),
) {
    val repository = AppContainer.repository
    val billingRepository = BillingContainer.repository
    val state by repository.dashboardState.collectAsState()
    val billingState by billingRepository.billingState.collectAsState()
    val now = rememberMinuteNow(clock)
    val today = localDateAt(now, clock.zone)
    val launchDestination = AppRouteResolver.resolve(initialDeepLink)
    val initialDestination = launchDestination.topLevelDestination
    val initialMoreSection = launchDestination.moreSection
    var destination by rememberSaveable(initialDeepLink) { mutableStateOf(initialDestination) }
    var moreSection by rememberSaveable(initialDeepLink) { mutableStateOf(initialMoreSection) }
    LaunchedEffect(initialDeepLink, initialDestination, initialMoreSection) {
        destination = initialDestination
        moreSection = initialMoreSection
    }
    val context = LocalContext.current
    val notificationPermissionActions =
        rememberAppNotificationPermissionActions(
            context = context,
            destination = destination,
            state = state,
        )
    AppRuntimeEffects(context = context, state = state)

    if (state.launchFunnelSnapshot.completedOnboardingAtIso == null) {
        OnboardingRoute(
            state = state,
            repository = repository,
            billingState = billingState,
            notificationPermissionActions = notificationPermissionActions,
            now = now,
            today = today,
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    AppScaffold(
        destination = destination,
        initialMoreSection = moreSection,
        state = state,
        repository = repository,
        billingState = billingState,
        billingActions = appBillingActions(billingRepository, context),
        notificationPermissionActions = notificationPermissionActions,
        now = now,
        today = today,
        onDestinationChange = { destination = it },
        onCompanionAction = { action ->
            val route = appRouteFor(action.destination)
            destination = route.topLevelDestination
            moreSection = route.moreSection
        },
    )
}

@Composable
private fun AppScaffold(
    destination: TopLevelDestination,
    initialMoreSection: MoreSection,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    notificationPermissionActions: NotificationPermissionActions,
    now: Instant,
    today: LocalDate,
    onDestinationChange: (TopLevelDestination) -> Unit,
    onCompanionAction: (CompanionNextAction) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigation(destination = destination, onDestinationChange = onDestinationChange) },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .sacredCanvas(),
        ) {
            AppContent(
                destination = destination,
                initialMoreSection = initialMoreSection,
                state = state,
                repository = repository,
                billingState = billingState,
                billingActions = billingActions,
                notificationPermissionActions = notificationPermissionActions,
                now = now,
                today = today,
                onCompanionAction = onCompanionAction,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@Composable
private fun rememberAppNotificationPermissionActions(
    context: android.content.Context,
    destination: TopLevelDestination,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
): NotificationPermissionActions =
    rememberNotificationPermissionActions(
        context = context,
        refreshKey =
            listOf(
                destination,
                state.launchFunnelSnapshot.selectedReminderTier,
                state.launchFunnelSnapshot.completedOnboardingAtIso,
            ),
    )

@Composable
private fun Modifier.sacredCanvas(): Modifier =
    background(
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f),
            ),
        ),
    )

@Composable
private fun BottomNavigation(
    destination: TopLevelDestination,
    onDestinationChange: (TopLevelDestination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 0.dp,
    ) {
        listOf(
            TopLevelDestination.TODAY,
            TopLevelDestination.FASTING_DAYS,
            TopLevelDestination.TRACK_FAST,
            TopLevelDestination.MORE,
        ).forEach { item ->
            val itemLabel = stringResource(item.labelRes())
            NavigationBarItem(
                selected = item == destination,
                onClick = { onDestinationChange(item) },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes()),
                        contentDescription = null,
                    )
                },
                label = { Text(itemLabel) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                modifier = Modifier,
            )
        }
    }
}

@Composable
private fun AppContent(
    destination: TopLevelDestination,
    initialMoreSection: MoreSection,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    repository: com.kevpierce.catholicfasting.core.data.AppRepository,
    billingState: com.kevpierce.catholicfasting.core.billing.BillingState,
    billingActions: BillingActions,
    notificationPermissionActions: NotificationPermissionActions,
    now: Instant,
    today: LocalDate,
    onCompanionAction: (CompanionNextAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val supportState =
        buildAppSupportState(
            context = context,
            state = state,
            premiumUnlocked = billingState.premiumUnlocked,
            now = now,
            today = today,
        )
    when (destination) {
        TopLevelDestination.TODAY ->
            TodayScreen(
                uiState = buildTodayUiState(context = context, state = state, supportState = supportState, today = today),
                onCompanionAction = onCompanionAction,
                modifier = modifier,
            )
        TopLevelDestination.FASTING_DAYS ->
            CalendarScreen(
                observances = state.observances,
                statusesById = state.statusesById,
                fridayNotesById = state.fridayNotesById,
                premiumSnapshot = supportState.premiumSnapshot,
                today = today,
                onStatusChange = repository::setStatus,
                onFridayNoteChange = repository::setFridayNote,
                modifier = modifier,
            )
        TopLevelDestination.TRACK_FAST ->
            TrackFastDestination(
                state = state,
                repository = repository,
                supportState = supportState,
                now = now,
                modifier = modifier,
            )
        TopLevelDestination.MORE ->
            MoreDestination(
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

private fun buildTodayUiState(
    context: android.content.Context,
    state: com.kevpierce.catholicfasting.core.data.DashboardState,
    supportState: AppSupportState,
    today: LocalDate,
): TodayUiState =
    TodayUiState(
        todayObservance = state.observances.firstOrNull { it.date == today.toString() },
        companionSnapshot = supportState.companionSnapshot,
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
        noticeSummary = context.getString(R.string.notice_independent_app_summary),
    )

private fun TopLevelDestination.labelRes(): Int =
    when (this) {
        TopLevelDestination.TODAY -> R.string.nav_today
        TopLevelDestination.FASTING_DAYS -> R.string.nav_fasting_days
        TopLevelDestination.TRACK_FAST -> R.string.nav_track_fast
        TopLevelDestination.MORE -> R.string.nav_more
    }

private fun TopLevelDestination.iconRes(): Int =
    when (this) {
        TopLevelDestination.TODAY -> R.drawable.ic_nav_today
        TopLevelDestination.FASTING_DAYS -> R.drawable.ic_nav_calendar
        TopLevelDestination.TRACK_FAST -> R.drawable.ic_nav_timer
        TopLevelDestination.MORE -> R.drawable.ic_nav_more
    }

internal fun appRouteFor(destination: CompanionActionDestination): AppLaunchDestination =
    when (destination) {
        CompanionActionDestination.TODAY ->
            AppLaunchDestination(TopLevelDestination.TODAY)
        CompanionActionDestination.FASTING_DAYS ->
            AppLaunchDestination(TopLevelDestination.FASTING_DAYS)
        CompanionActionDestination.TRACK_FAST ->
            AppLaunchDestination(TopLevelDestination.TRACK_FAST)
        CompanionActionDestination.GUIDANCE ->
            AppLaunchDestination(TopLevelDestination.MORE, MoreSection.GUIDANCE_RULES)
        CompanionActionDestination.SETUP ->
            AppLaunchDestination(TopLevelDestination.MORE, MoreSection.SETUP_REMINDERS)
        CompanionActionDestination.PREMIUM ->
            AppLaunchDestination(TopLevelDestination.MORE, MoreSection.SUPPORT_PREMIUM)
    }
