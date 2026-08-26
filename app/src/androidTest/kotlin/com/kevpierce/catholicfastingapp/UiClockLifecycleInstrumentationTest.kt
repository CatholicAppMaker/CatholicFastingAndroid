package com.kevpierce.catholicfastingapp

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kevpierce.catholicfastingapp.ui.rememberMinuteNow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class UiClockLifecycleInstrumentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun minuteClockSuspendsInBackgroundAndResamplesOnResume() {
        val lifecycleOwner = MutableLifecycleOwner()
        val initialInstant = Instant.parse("2026-02-18T12:59:00Z")
        val startedInstant = Instant.parse("2026-02-18T13:00:00Z")
        val resumedInstant = Instant.parse("2026-02-18T13:05:00Z")
        val clock = MutableClock(initialInstant, ZoneId.of("America/New_York"))

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                Text(
                    text = rememberMinuteNow(clock).toString(),
                    modifier = Modifier.testTag(CLOCK_TEST_TAG),
                )
            }
        }

        composeRule.runOnIdle {
            lifecycleOwner.moveTo(Lifecycle.State.CREATED)
            clock.currentInstant = startedInstant
            lifecycleOwner.moveTo(Lifecycle.State.STARTED)
        }
        waitForInstant(startedInstant)

        composeRule.runOnIdle {
            lifecycleOwner.moveTo(Lifecycle.State.CREATED)
            clock.currentInstant = resumedInstant
        }
        composeRule.onNodeWithTag(CLOCK_TEST_TAG).assertTextEquals(startedInstant.toString())

        composeRule.runOnIdle {
            lifecycleOwner.moveTo(Lifecycle.State.STARTED)
        }
        waitForInstant(resumedInstant)

        composeRule.runOnIdle {
            lifecycleOwner.moveTo(Lifecycle.State.DESTROYED)
        }
    }

    private fun waitForInstant(instant: Instant) {
        val expected = instant.toString()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(expected).fetchSemanticsNodes().size == 1
        }
        composeRule.onNodeWithTag(CLOCK_TEST_TAG).assertTextEquals(expected)
    }

    private class MutableLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle = registry

        fun moveTo(state: Lifecycle.State) {
            registry.currentState = state
        }
    }

    private class MutableClock(
        var currentInstant: Instant,
        private val zoneId: ZoneId,
    ) : Clock() {
        override fun getZone(): ZoneId = zoneId

        override fun withZone(zone: ZoneId): Clock = MutableClock(currentInstant, zone)

        override fun instant(): Instant = currentInstant
    }

    private companion object {
        const val CLOCK_TEST_TAG = "ui-clock-instant"
    }
}
