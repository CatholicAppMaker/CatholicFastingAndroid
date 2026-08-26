package com.kevpierce.catholicfasting.feature.tracker

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant

class ActiveFastProgressTest {
    private val start = Instant.parse("2026-02-18T12:00:00Z")

    @Test
    fun duringFastReportsSharedElapsedRemainingAndProgress() {
        val progress = activeFastProgress(start.toString(), 16, start.plusSeconds(4 * 3600L))

        assertThat(progress.elapsedSeconds).isEqualTo(4 * 3600L)
        assertThat(progress.remainingSeconds).isEqualTo(12 * 3600L)
        assertThat(progress.progress).isWithin(0.0001f).of(0.25f)
    }

    @Test
    fun exactTargetIsComplete() {
        val progress = activeFastProgress(start.toString(), 16, start.plusSeconds(16 * 3600L))

        assertThat(progress.remainingSeconds).isEqualTo(0L)
        assertThat(progress.progress).isEqualTo(1f)
    }

    @Test
    fun overdueFastClampsProgressAndRemaining() {
        val progress = activeFastProgress(start.toString(), 16, start.plusSeconds(20 * 3600L))

        assertThat(progress.remainingSeconds).isEqualTo(0L)
        assertThat(progress.progress).isEqualTo(1f)
    }

    @Test
    fun malformedOrFutureStartFallsBackToZeroElapsed() {
        val malformed = activeFastProgress("not-an-instant", 16, start)
        val future = activeFastProgress(start.plusSeconds(60).toString(), 16, start)

        assertThat(malformed.elapsedSeconds).isEqualTo(0L)
        assertThat(future.elapsedSeconds).isEqualTo(0L)
        assertThat(malformed.remainingSeconds).isEqualTo(16 * 3600L)
        assertThat(future.remainingSeconds).isEqualTo(16 * 3600L)
    }

    @Test
    fun nonPositiveTargetIsSafe() {
        val progress = activeFastProgress(start.toString(), 0, start.plusSeconds(60))

        assertThat(progress.targetSeconds).isEqualTo(0L)
        assertThat(progress.remainingSeconds).isEqualTo(0L)
        assertThat(progress.progress).isEqualTo(0f)
    }
}
