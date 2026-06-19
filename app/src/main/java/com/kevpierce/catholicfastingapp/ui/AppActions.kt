package com.kevpierce.catholicfastingapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.kevpierce.catholicfasting.core.billing.BillingRepository
import com.kevpierce.catholicfasting.core.model.ReminderTier

internal data class BillingActions(
    val onRefresh: () -> Unit,
    val onManageSubscription: () -> Unit,
    val onPurchase: (String) -> Unit,
)

internal data class NotificationPermissionActions(
    val granted: Boolean,
    val requestPermission: () -> Unit,
)

internal data class SetupReminderActions(
    val onReminderTierChange: (ReminderTier) -> Unit,
    val onDailyQuoteReminderEnabledChange: (Boolean) -> Unit,
    val onDailyQuoteReminderTimeChange: (Int, Int) -> Unit,
    val onNoticeAcknowledgedChange: (Boolean) -> Unit,
    val onCompleteOnboarding: () -> Unit,
)

internal fun appBillingActions(
    billingRepository: BillingRepository,
    context: Context,
) = BillingActions(
    onRefresh = billingRepository::refresh,
    onManageSubscription = { billingRepository.openManageSubscription(context) },
    onPurchase = { productId ->
        (context as? ComponentActivity)?.let { activity ->
            billingRepository.launchPurchase(activity, productId)
        }
    },
)

private fun notificationsEnabled(context: Context): Boolean =
    !notificationPermissionSupported() ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

internal fun notificationPermissionSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@Composable
internal fun rememberNotificationPermissionActions(
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
