package com.kevpierce.catholicfasting.core.ui

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import org.junit.Test

class SeasonToneTest {
    @Test
    fun seasonToneReturnsDistinctReadableColorsForEachLightSeason() {
        LiturgicalSeason.entries.forEach { season ->
            val tone = seasonTone(season = season, darkTheme = false)

            assertThat(tone.containerColor).isNotEqualTo(tone.contentColor)
            assertThat(tone.borderColor).isNotEqualTo(tone.containerColor)
            assertThat(tone.accentColor).isNotEqualTo(tone.contentColor)
        }
    }

    @Test
    fun seasonToneReturnsDistinctReadableColorsForEachDarkSeason() {
        LiturgicalSeason.entries.forEach { season ->
            val tone = seasonTone(season = season, darkTheme = true)

            assertThat(tone.containerColor).isNotEqualTo(tone.contentColor)
            assertThat(tone.borderColor).isNotEqualTo(tone.containerColor)
            assertThat(tone.accentColor).isNotEqualTo(tone.contentColor)
        }
    }
}
