package com.kevpierce.catholicfasting.core.widget

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.WidgetSnapshot
import org.junit.Test

class WidgetSnapshotCodecTest {
    @Test
    fun missingAndCorruptPayloadsReturnTheDefaultSnapshot() {
        val defaultSnapshot =
            WidgetSnapshot(
                generatedAtIso = "",
                todayTitle = "Today",
                todayObligation = "No obligation",
                nextRequiredTitle = "Next required day",
                completionRate = 0.0,
                hasActiveIntermittentFast = false,
                activeIntermittentTargetHours = 16,
            )

        assertThat(decodeWidgetSnapshotOrDefault(null, defaultSnapshot)).isSameInstanceAs(defaultSnapshot)
        assertThat(decodeWidgetSnapshotOrDefault("{not-json", defaultSnapshot)).isSameInstanceAs(defaultSnapshot)
    }
}
