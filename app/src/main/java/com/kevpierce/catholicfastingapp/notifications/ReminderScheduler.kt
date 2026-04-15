package com.kevpierce.catholicfastingapp.notifications

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kevpierce.catholicfasting.core.data.DashboardState
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.rules.RequiredDayReminderPlanner
import com.kevpierce.catholicfastingapp.R
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val REQUIRED_TAG = "required_observance_reminder"
    private const val SUPPORT_WORK = "support_reminder"
    private const val MORNING_WORK = "morning_reminder"
    private const val EVENING_WORK = "evening_reminder"

    fun sync(
        context: Context,
        state: DashboardState,
    ) {
        NotificationChannels.ensureReminderChannel(context)
        val workManager = WorkManager.getInstance(context)
        val reminderTier = state.launchFunnelSnapshot.selectedReminderTier
        scheduleRequiredDayReminders(workManager, state.observances)
        scheduleDailySupport(
            context = context,
            workManager = workManager,
            uniqueName = SUPPORT_WORK,
            enabled = reminderTier.supportEnabled,
            hourOfDay = 12,
        )
        scheduleDailySupport(
            context = context,
            workManager = workManager,
            uniqueName = MORNING_WORK,
            enabled = reminderTier.morningEnabled,
            hourOfDay = 8,
        )
        scheduleDailySupport(
            context = context,
            workManager = workManager,
            uniqueName = EVENING_WORK,
            enabled = reminderTier.eveningEnabled,
            hourOfDay = 19,
        )
    }

    private fun scheduleRequiredDayReminders(
        workManager: WorkManager,
        observances: List<Observance>,
    ) {
        workManager.cancelAllWorkByTag(REQUIRED_TAG)
        val plannedObservances =
            RequiredDayReminderPlanner.upcomingMandatoryObservances(
                observances = observances,
                now = LocalDate.now(),
                limit =
                    RequiredDayReminderPlanner
                        .maximumRequiredReminders(existingNonRequiredPendingCount = 0)
                        .coerceAtMost(12),
            )

        plannedObservances.forEach { observance ->
            val request =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delayUntil(observance.date, 8))
                    .setInputData(requiredReminderInput(observance))
                    .setConstraints(reminderConstraints())
                    .addTag(REQUIRED_TAG)
                    .build()
            workManager.enqueueUniqueWork(
                "required-${observance.id}",
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }

    private fun scheduleDailySupport(
        context: Context,
        workManager: WorkManager,
        uniqueName: String,
        enabled: Boolean,
        hourOfDay: Int,
    ) {
        if (!enabled) {
            workManager.cancelUniqueWork(uniqueName)
            return
        }

        val request =
            PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayUntilNextHour(hourOfDay))
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                .setConstraints(reminderConstraints())
                .setInputData(
                    Data.Builder()
                        .putString(ReminderWorker.KEY_TITLE, supportTitle(context, uniqueName))
                        .putString(ReminderWorker.KEY_BODY, supportBody(context, uniqueName))
                        .putString(
                            ReminderWorker.KEY_DEEP_LINK,
                            ReminderNotificationRoutes.deepLinkForKind(reminderKind(uniqueName)),
                        )
                        .build(),
                ).build()

        workManager.enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun requiredReminderInput(observance: Observance): Data =
        Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, observance.title)
            .putString(
                ReminderWorker.KEY_BODY,
                observance.detail ?: observance.rationale,
            ).putString(
                ReminderWorker.KEY_DEEP_LINK,
                ReminderNotificationRoutes.deepLinkForKind(ReminderNotificationRoutes.KIND_REQUIRED_DAY),
            ).build()

    private fun reminderConstraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

    private fun delayUntil(
        dateIso: String,
        hourOfDay: Int,
    ): Duration {
        val targetDate = LocalDate.parse(dateIso)
        val targetTime = LocalDateTime.of(targetDate, LocalTime.of(hourOfDay, 0))
        val now = LocalDateTime.now()
        val adjustedTarget = if (targetTime.isAfter(now)) targetTime else now.plusMinutes(15)
        return Duration.between(now, adjustedTarget)
    }

    private fun delayUntilNextHour(hourOfDay: Int): Duration {
        val now = LocalDateTime.now()
        val todayTarget = LocalDateTime.of(LocalDate.now(), LocalTime.of(hourOfDay, 0))
        val nextTarget = if (todayTarget.isAfter(now)) todayTarget else todayTarget.plusDays(1)
        return Duration.between(now, nextTarget.atZone(ZoneId.systemDefault()).toLocalDateTime())
    }

    private fun supportTitle(
        context: Context,
        uniqueName: String,
    ): String =
        when (uniqueName) {
            SUPPORT_WORK -> context.getString(R.string.notification_support_title)
            MORNING_WORK -> context.getString(R.string.notification_morning_title)
            EVENING_WORK -> context.getString(R.string.notification_evening_title)
            else -> context.getString(R.string.notification_support_generic_title)
        }

    private fun supportBody(
        context: Context,
        uniqueName: String,
    ): String =
        when (uniqueName) {
            SUPPORT_WORK -> context.getString(R.string.notification_support_body)
            MORNING_WORK -> context.getString(R.string.notification_morning_body)
            EVENING_WORK -> context.getString(R.string.notification_evening_body)
            else -> context.getString(R.string.notification_support_generic_body)
        }

    private fun reminderKind(uniqueName: String): String =
        when (uniqueName) {
            SUPPORT_WORK -> ReminderNotificationRoutes.KIND_SUPPORT
            MORNING_WORK -> ReminderNotificationRoutes.KIND_MORNING
            EVENING_WORK -> ReminderNotificationRoutes.KIND_EVENING
            else -> ReminderNotificationRoutes.KIND_SUPPORT
        }
}
