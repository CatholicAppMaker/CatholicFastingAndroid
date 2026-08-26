package com.kevpierce.catholicfasting.core.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import com.kevpierce.catholicfasting.core.model.WidgetSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val Context.widgetDataStore by preferencesDataStore(name = "catholic_fasting_widget_store")

private val jsonCodec =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

private val snapshotKey = stringPreferencesKey("widget_snapshot_v1")

internal fun decodeWidgetSnapshotOrDefault(
    encoded: String?,
    defaultSnapshot: WidgetSnapshot,
): WidgetSnapshot {
    if (encoded == null) {
        return defaultSnapshot
    }
    return runCatching {
        jsonCodec.decodeFromString(WidgetSnapshot.serializer(), encoded)
    }.getOrDefault(defaultSnapshot)
}

object WidgetSnapshotStore {
    suspend fun persist(
        context: Context,
        snapshot: WidgetSnapshot,
    ) {
        context.widgetDataStore.edit { preferences ->
            preferences[snapshotKey] =
                jsonCodec.encodeToString(
                    WidgetSnapshot.serializer(),
                    snapshot,
                )
        }
        CatholicFastingWidget().updateAll(context)
    }

    suspend fun read(context: Context): WidgetSnapshot {
        val encoded = context.widgetDataStore.data.first()[snapshotKey]
        return decodeWidgetSnapshotOrDefault(encoded, defaultSnapshot(context))
    }

    private fun defaultSnapshot(context: Context): WidgetSnapshot =
        WidgetSnapshot(
            generatedAtIso = "",
            todayTitle = context.getString(R.string.widget_default_today_title),
            todayObligation = context.getString(R.string.widget_default_today_obligation),
            nextRequiredTitle = context.getString(R.string.widget_default_next_required_title),
            completionRate = 0.0,
            hasActiveIntermittentFast = false,
            activeIntermittentTargetHours = 16,
        )
}
