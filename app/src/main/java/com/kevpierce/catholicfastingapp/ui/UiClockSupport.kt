package com.kevpierce.catholicfastingapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val MILLIS_PER_MINUTE = 60_000L

@Composable
internal fun rememberMinuteNow(clock: Clock): Instant {
    val lifecycleOwner = LocalLifecycleOwner.current
    val now by
        produceState(
            initialValue = clock.instant(),
            key1 = clock,
            key2 = lifecycleOwner,
        ) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val sample = clock.instant()
                    value = sample
                    delay(millisUntilNextMinute(sample))
                }
            }
        }
    return now
}

internal fun millisUntilNextMinute(now: Instant): Long {
    val millisIntoMinute = Math.floorMod(now.toEpochMilli(), MILLIS_PER_MINUTE)
    return MILLIS_PER_MINUTE - millisIntoMinute
}

internal fun localDateAt(
    now: Instant,
    zone: ZoneId,
): LocalDate = now.atZone(zone).toLocalDate()
