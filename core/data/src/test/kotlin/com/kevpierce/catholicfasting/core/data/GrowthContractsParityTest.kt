package com.kevpierce.catholicfasting.core.data

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.ContentLocale
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.ReminderTier
import com.kevpierce.catholicfasting.core.model.RuleSettings
import com.kevpierce.catholicfasting.core.model.SubscriptionOfferCatalog
import com.kevpierce.catholicfasting.core.rules.LiturgicalSeasonThemeEngine
import com.kevpierce.catholicfasting.core.rules.SeasonalContentPackCatalog
import com.kevpierce.catholicfasting.core.rules.SeasonalContentSupport
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class GrowthContractsParityTest {
    @Test
    fun ruleSettingsDefaultsRegionProfileToUs() {
        val settings =
            RuleSettings(
                birthYear = 1990,
                hasMedicalDispensation = false,
            )

        assertThat(settings.regionProfile).isEqualTo(RegionProfile.US)
    }

    @Test
    fun subscriptionOfferCatalogHasMonthlyAndYearlyInSingleCatalog() {
        val catalog = SubscriptionOfferCatalog.catholicFasting

        assertThat(catalog.pillars).hasSize(3)
        assertThat(catalog.offers).hasSize(2)
        assertThat(catalog.offers.map { it.id })
            .containsExactly(
                "com.kevpierce.catholicfasting.premium.yearly.v3",
                "com.kevpierce.catholicfasting.premium.monthly.v3",
            )
        assertThat(catalog.offers.count { it.isPrimaryAnchor }).isEqualTo(1)
    }

    @Test
    fun reminderTierInferenceMatchesExpectedCadence() {
        assertThat(
            ReminderTier.infer(
                supportEnabled = false,
                morningEnabled = false,
                eveningEnabled = false,
            ),
        ).isEqualTo(ReminderTier.MINIMAL)
        assertThat(
            ReminderTier.infer(
                supportEnabled = true,
                morningEnabled = true,
                eveningEnabled = false,
            ),
        ).isEqualTo(ReminderTier.BALANCED)
        assertThat(
            ReminderTier.infer(
                supportEnabled = true,
                morningEnabled = true,
                eveningEnabled = true,
            ),
        ).isEqualTo(ReminderTier.GUIDED)
    }

    @Test
    fun seasonalContentPackReturnsLocalizedContent() {
        val season = LiturgicalSeasonThemeEngine.seasonFor(LocalDate.of(2026, 3, 10))
        val englishLent = SeasonalContentPackCatalog.pack(season, ContentLocale.ENGLISH)
        val spanishLent = SeasonalContentPackCatalog.pack(season, ContentLocale.SPANISH)

        assertThat(englishLent.quotes).isNotEmpty()
        assertThat(spanishLent.quotes).isNotEmpty()
        assertThat(englishLent.campaignTitle).isNotEqualTo(spanishLent.campaignTitle)
        assertThat(englishLent.locale).isEqualTo(ContentLocale.ENGLISH)
        assertThat(spanishLent.locale).isEqualTo(ContentLocale.SPANISH)
    }

    @Test
    fun dailyQuoteUsesLocalizedSeasonalQuotePool() {
        val date = LocalDate.of(2026, 3, 10)
        val season = LiturgicalSeasonThemeEngine.seasonFor(date)
        val englishPack = SeasonalContentPackCatalog.pack(season, ContentLocale.ENGLISH)
        val spanishPack = SeasonalContentPackCatalog.pack(season, ContentLocale.SPANISH)

        val englishQuote = SeasonalContentSupport.dailyQuote(season, englishPack, date)
        val spanishQuote = SeasonalContentSupport.dailyQuote(season, spanishPack, date)

        assertThat(englishQuote.text).isNotEmpty()
        assertThat(spanishQuote.text).isNotEmpty()
        assertThat(englishQuote.text).isNotEqualTo(spanishQuote.text)
    }

    @Test
    fun seasonalHeroStateUsesLocaleSpecificContentAndStableImageryCount() {
        val englishHero = buildSeasonalHeroState(locale = Locale.US, today = LocalDate.of(2026, 3, 10))
        val spanishHero = buildSeasonalHeroState(locale = Locale("es", "ES"), today = LocalDate.of(2026, 3, 10))

        assertThat(englishHero.campaignTitle).isEqualTo("Lenten Discipline")
        assertThat(spanishHero.campaignTitle).isEqualTo("Disciplina Cuaresmal")
        assertThat(englishHero.imagery).hasSize(3)
        assertThat(spanishHero.imagery).hasSize(3)
    }
}
