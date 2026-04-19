package com.kevpierce.catholicfasting.core.rules

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import org.junit.Test
import java.time.LocalDate

class LiturgicalSeasonThemeEngineTest {
    @Test
    fun lentDateIsDetected() {
        assertThat(LiturgicalSeasonThemeEngine.seasonFor(LocalDate.of(2026, 3, 10)))
            .isEqualTo(LiturgicalSeason.LENT)
    }

    @Test
    fun easterSeasonDateIsDetected() {
        assertThat(LiturgicalSeasonThemeEngine.seasonFor(LocalDate.of(2026, 4, 12)))
            .isEqualTo(LiturgicalSeason.EASTER)
    }

    @Test
    fun adventDateIsDetected() {
        assertThat(LiturgicalSeasonThemeEngine.seasonFor(LocalDate.of(2026, 12, 10)))
            .isEqualTo(LiturgicalSeason.ADVENT)
    }

    @Test
    fun christmasDateIsDetected() {
        assertThat(LiturgicalSeasonThemeEngine.seasonFor(LocalDate.of(2026, 12, 26)))
            .isEqualTo(LiturgicalSeason.CHRISTMAS)
    }

    @Test
    fun earlyJanuaryIsChristmasSeason() {
        assertThat(LiturgicalSeasonThemeEngine.seasonFor(LocalDate.of(2027, 1, 8)))
            .isEqualTo(LiturgicalSeason.CHRISTMAS)
    }

    @Test
    fun ordinaryTimeDateIsDetected() {
        assertThat(LiturgicalSeasonThemeEngine.seasonFor(LocalDate.of(2026, 7, 8)))
            .isEqualTo(LiturgicalSeason.ORDINARY)
    }
}
