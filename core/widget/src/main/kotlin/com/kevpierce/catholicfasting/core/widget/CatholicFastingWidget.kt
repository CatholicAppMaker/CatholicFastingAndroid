package com.kevpierce.catholicfasting.core.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.kevpierce.catholicfasting.core.model.WidgetSnapshot

class CatholicFastingWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: android.content.Context,
        id: androidx.glance.GlanceId,
    ) {
        val snapshot = WidgetSnapshotStore.read(context)

        provideContent {
            widgetContent(
                context = context,
                snapshot = snapshot,
                openToday = actionStartActivity<TodayWidgetLaunchActivity>(),
                openCalendar = actionStartActivity<CalendarWidgetLaunchActivity>(),
                openTracker = actionStartActivity<TrackerWidgetLaunchActivity>(),
            )
        }
    }
}

@Composable
private fun widgetContent(
    context: Context,
    snapshot: WidgetSnapshot,
    openToday: androidx.glance.action.Action,
    openCalendar: androidx.glance.action.Action,
    openTracker: androidx.glance.action.Action,
) {
    Column(
        modifier =
            GlanceModifier
                .padding(12.dp),
    ) {
        Column(modifier = GlanceModifier.clickable(openToday)) {
            Text(snapshot.todayTitle)
            Text(snapshot.todayObligation)
        }
        Column(modifier = GlanceModifier.padding(top = 8.dp).clickable(openCalendar)) {
            Text(snapshot.nextRequiredTitle)
            Text(context.getString(R.string.widget_progress_value, (snapshot.completionRate * 100).toInt()))
        }
        if (snapshot.hasActiveIntermittentFast) {
            Column(modifier = GlanceModifier.padding(top = 8.dp).clickable(openTracker)) {
                Text(context.getString(R.string.widget_active_fast_value, snapshot.activeIntermittentTargetHours))
            }
        }
    }
}

class CatholicFastingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CatholicFastingWidget()
}
