package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class SeasonalHeroStateTest {
    @Test
    fun buildSeasonalHeroStateUsesLentenEnglishPack() {
        val heroState =
            buildSeasonalHeroState(
                locale = Locale.US,
                today = LocalDate.of(2026, 3, 10),
            )

        assertThat(heroState.campaignTitle).isEqualTo("Lenten Discipline")
        assertThat(heroState.campaignSubtitle).contains("Pray, fast, and give alms")
        assertThat(heroState.formationLine).isNotEmpty()
        assertThat(heroState.quote.id).contains("seasonal-lent")
        assertThat(heroState.imagery).hasSize(3)
    }

    @Test
    fun buildSeasonalHeroStateFallsBackToSpanishOrdinaryPackWhenSeasonPackIsMissing() {
        val heroState =
            buildSeasonalHeroState(
                locale = Locale("es", "ES"),
                today = LocalDate.of(2026, 12, 5),
            )

        assertThat(heroState.campaignTitle).isEqualTo("Constancia en Tiempo Ordinario")
        assertThat(heroState.campaignSubtitle).contains("fidelidad diaria")
        assertThat(heroState.formationLine).isNotEmpty()
        assertThat(heroState.quote.author).isEqualTo("San Bernardo")
        assertThat(heroState.imagery).hasSize(3)
    }
}
