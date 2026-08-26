package com.kevpierce.catholicfastingapp.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class UiClockSupportTest {
    @Test
    fun exactMinuteWaitsForNextBoundary() {
        assertThat(millisUntilNextMinute(Instant.parse("2026-02-18T13:00:00Z"))).isEqualTo(60_000L)
    }

    @Test
    fun finalMillisecondWaitsOneMillisecond() {
        assertThat(millisUntilNextMinute(Instant.parse("2026-02-18T13:00:59.999Z"))).isEqualTo(1L)
    }

    @Test
    fun preEpochInstantUsesFloorMod() {
        assertThat(millisUntilNextMinute(Instant.ofEpochMilli(-1L))).isEqualTo(1L)
    }

    @Test
    fun localDateUsesInjectedClockZone() {
        val instant = Instant.parse("2026-02-18T02:00:00Z")

        assertThat(localDateAt(instant, ZoneId.of("America/New_York")).toString()).isEqualTo("2026-02-17")
        assertThat(localDateAt(instant, ZoneId.of("Europe/Rome")).toString()).isEqualTo("2026-02-18")
    }
}
