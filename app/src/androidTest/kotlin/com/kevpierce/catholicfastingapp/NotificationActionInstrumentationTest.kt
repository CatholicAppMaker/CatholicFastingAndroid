package com.kevpierce.catholicfastingapp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfastingapp.notifications.NotificationActionReceiver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class NotificationActionInstrumentationTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppContainer.initialize(context)
        AppContainer.repository.cancelIntermittentFast()
    }

    @Test
    fun endFastActionEndsActiveFastAndAddsCompletedSession() {
        val repository = AppContainer.repository
        val start = Instant.now().minus(Duration.ofHours(25))
        repository.setIntermittentPresetHours(4)
        repository.startIntermittentFast(now = start)

        NotificationActionReceiver().onReceive(
            context,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_END_FAST
                putExtra(NotificationActionReceiver.EXTRA_START_ISO, start.toString())
                putExtra(NotificationActionReceiver.EXTRA_TARGET_HOURS, 4)
            },
        )

        val dashboardState = repository.dashboardState.value
        assertThat(dashboardState.activeIntermittentFast).isNull()
        assertThat(dashboardState.intermittentSessions).isNotEmpty()
        assertThat(dashboardState.intermittentSessions.first().completedTarget).isTrue()
    }

    @Test
    fun unrelatedActionLeavesActiveFastUntouched() {
        val repository = AppContainer.repository
        repository.startIntermittentFast(now = Instant.parse("2026-03-13T08:00:00Z"))
        val stateBefore = repository.dashboardState.value

        NotificationActionReceiver().onReceive(
            context,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = "com.kevpierce.catholicfastingapp.action.UNKNOWN"
            },
        )

        val stateAfter = repository.dashboardState.value
        assertThat(stateAfter.activeIntermittentFast).isEqualTo(stateBefore.activeIntermittentFast)
        assertThat(stateAfter.intermittentSessions).isEqualTo(stateBefore.intermittentSessions)
    }
}
