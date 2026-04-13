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
        val encoded = context.widgetDataStore.data.first()[snapshotKey] ?: return defaultSnapshot()
        return runCatching {
            jsonCodec.decodeFromString(WidgetSnapshot.serializer(), encoded)
        }.getOrElse {
            defaultSnapshot()
        }
    }

    private fun defaultSnapshot(): WidgetSnapshot =
        WidgetSnapshot(
            generatedAtIso = "",
            todayTitle = "Catholic Fasting",
            todayObligation = "Open app for today's plan",
            nextRequiredTitle = "Widget ready",
            completionRate = 0.0,
            hasActiveIntermittentFast = false,
            activeIntermittentTargetHours = 16,
        )
}
