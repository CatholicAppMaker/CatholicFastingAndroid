package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.ActiveIntermittentFast
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test
import java.time.Instant

class IntermittentFastStateParityTest {
    @Test
    fun presetHoursAreBounded() {
        assertThat(boundedPresetHours(9)).isEqualTo(12)
        assertThat(boundedPresetHours(72)).isEqualTo(72)
        assertThat(boundedPresetHours(400)).isEqualTo(336)
    }

    @Test
    fun resolveEndedFastStateReturnsNullForFutureStart() {
        val now = Instant.parse("2026-03-13T12:00:00Z")
        val futureFast =
            ActiveIntermittentFast(
                startIso = "2026-03-13T14:00:00Z",
                targetHours = 16,
            )

        val endedState =
            resolveEndedFastState(
                liveState = sampleState(activeIntermittentFast = futureFast),
                storedState = sampleState(),
                now = now,
            )

        assertThat(endedState).isNull()
    }

    @Test
    fun resolveEndedFastStateCapsSessionHistoryAtFiveHundred() {
        val now = Instant.parse("2026-03-13T12:00:00Z")
        val existingSessions =
            (1..500).map { index ->
                IntermittentFastSession(
                    id = "session-$index",
                    startIso = "2026-03-12T00:00:00Z",
                    endIso = "2026-03-12T16:00:00Z",
                    targetHours = 16,
                    completedTarget = true,
                )
            }

        val endedState =
            resolveEndedFastState(
                liveState =
                    sampleState(
                        intermittentSessions = existingSessions,
                        activeIntermittentFast =
                            ActiveIntermittentFast(
                                startIso = "2026-03-13T08:00:00Z",
                                targetHours = 4,
                            ),
                    ),
                storedState = sampleState(),
                now = now,
            )

        assertThat(endedState).isNotNull()
        assertThat(endedState!!.intermittentSessions).hasSize(500)
        assertThat(endedState.intermittentSessions.first().startIso).isEqualTo("2026-03-13T08:00:00Z")
    }

    @Test
    fun resolveEndedFastStateUsesNotificationFallbackPayload() {
        val now = Instant.parse("2026-03-13T12:00:00Z")

        val endedState =
            resolveEndedFastState(
                liveState = sampleState(),
                storedState = sampleState(),
                fallbackActiveFast =
                    ActiveIntermittentFast(
                        startIso = "2026-03-13T08:00:00Z",
                        targetHours = 500,
                    ),
                now = now,
            )

        assertThat(endedState).isNotNull()
        assertThat(endedState!!.intermittentSessions.first().targetHours).isEqualTo(500)
        assertThat(endedState.intermittentSessions.first().completedTarget).isFalse()
    }

    private fun sampleState(
        intermittentSessions: List<IntermittentFastSession> = emptyList(),
        activeIntermittentFast: ActiveIntermittentFast? = null,
    ): DashboardState {
        val settings = RuleSettings()
        return DashboardState(
            settings = settings,
            year = 2026,
            observances = observancesFor(2026, settings),
            intermittentSessions = intermittentSessions,
            activeIntermittentFast = activeIntermittentFast,
        )
    }
}
