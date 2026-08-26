package com.kevpierce.catholicfastingapp

import android.content.Context
import com.kevpierce.catholicfasting.core.data.AppContainer
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderTier
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal object ReleaseTestFixture {
    val fixedNow: Instant = Instant.parse("2026-02-18T13:00:00Z")
    val fixedClock: Clock = Clock.fixed(fixedNow, ZoneId.of("America/New_York"))

    fun seed(context: Context) {
        AppContainer.resetForTesting(context)
        AppContainer.repository.apply {
            updateYear(2026)
            setIndependentAppNoticeAcknowledged(true)
            setSelectedRegion(RegionProfile.US)
            setReminderTier(ReminderTier.BALANCED)
            setDailyQuoteReminderEnabled(true)
            setDailyQuoteReminderTime(8, 0)
            setIntermittentIntention(IntermittentFastIntention.PRAYER.name)
            setIntermittentPresetHours(16)
            completeOnboarding(fixedNow.minusSeconds(24 * 3600L))
            startIntermittentFastWithIntention(
                intentionId = IntermittentFastIntention.PRAYER.name,
                now = fixedNow.minusSeconds(3600L),
            )
            flushForTesting()
        }
    }
}
