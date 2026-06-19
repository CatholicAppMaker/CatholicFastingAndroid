package com.kevpierce.catholicfasting.core.model

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class IntermittentFastSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun oldCompletedSessionJsonDefaultsIntentionAndReviewNoteToNull() {
        val session =
            json.decodeFromString<IntermittentFastSession>(
                """
                {
                  "id": "old-session",
                  "startIso": "2026-03-13T08:00:00Z",
                  "endIso": "2026-03-14T00:00:00Z",
                  "targetHours": 16,
                  "completedTarget": true
                }
                """.trimIndent(),
            )

        assertThat(session.intentionId).isNull()
        assertThat(session.reviewNote).isNull()
    }

    @Test
    fun oldActiveFastJsonDefaultsIntentionToNull() {
        val activeFast =
            json.decodeFromString<ActiveIntermittentFast>(
                """
                {
                  "startIso": "2026-03-13T08:00:00Z",
                  "targetHours": 16
                }
                """.trimIndent(),
            )

        assertThat(activeFast.intentionId).isNull()
    }

    @Test
    fun oldLaunchFunnelJsonDefaultsFirstIntention() {
        val snapshot =
            json.decodeFromString<LaunchFunnelSnapshot>(
                """
                {
                  "startedAtIso": "2026-03-13T08:00:00Z",
                  "selectedReminderTier": "BALANCED"
                }
                """.trimIndent(),
            )

        assertThat(snapshot.selectedIntermittentIntentionId)
            .isEqualTo(IntermittentFastIntention.PERSONAL_DISCIPLINE.name)
        assertThat(snapshot.intermittentIntentionSelected).isFalse()
        assertThat(snapshot.regionSelected).isFalse()
        assertThat(snapshot.reminderTierSelected).isFalse()
    }
}
