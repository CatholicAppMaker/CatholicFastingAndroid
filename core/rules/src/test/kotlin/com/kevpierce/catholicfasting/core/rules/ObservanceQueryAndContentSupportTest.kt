package com.kevpierce.catholicfasting.core.rules

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.CalendarWindow
import com.kevpierce.catholicfasting.core.model.CatholicFastingQuote
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.ContentLocale
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceFilter
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.ObservanceSortOrder
import org.junit.Test
import java.time.LocalDate

class ObservanceQueryAndContentSupportTest {
    @Test
    fun requiredOnlyFilterReturnsMandatoryObservances() {
        val result =
            ObservanceQueryEngine.filter(
                observances = sampleObservances(),
                query = "",
                filter = ObservanceFilter.REQUIRED_ONLY,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 3, 1),
            )

        assertThat(result.map(Observance::title)).containsExactly("Ash Wednesday", "Good Friday").inOrder()
    }

    @Test
    fun trackedOnlyFilterUsesStatusesDictionary() {
        val items = sampleObservances()
        val result =
            ObservanceQueryEngine.filter(
                observances = items,
                query = "",
                filter = ObservanceFilter.TRACKED_ONLY,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById =
                    mapOf(
                        items[1].id to CompletionStatus.COMPLETED,
                        items[2].id to CompletionStatus.MISSED,
                    ),
                now = LocalDate.of(2026, 3, 1),
            )

        assertThat(result.map(Observance::title)).containsExactly("Good Friday", "Ember Day").inOrder()
    }

    @Test
    fun queryMatchesDetailKindAndObligationLabels() {
        val items = sampleObservances()

        val detailMatch =
            ObservanceQueryEngine.filter(
                observances = items,
                query = "meat",
                filter = ObservanceFilter.ALL,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 3, 1),
            )
        val kindMatch =
            ObservanceQueryEngine.filter(
                observances = items,
                query = "holy day",
                filter = ObservanceFilter.ALL,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 3, 1),
            )
        val obligationMatch =
            ObservanceQueryEngine.filter(
                observances = items,
                query = "required",
                filter = ObservanceFilter.ALL,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 3, 1),
            )

        assertThat(detailMatch.map(Observance::title)).containsExactly("Friday Penance (Outside Lent)")
        assertThat(kindMatch.map(Observance::title)).containsExactly("Ascension")
        assertThat(obligationMatch.map(Observance::title)).containsAtLeast("Ash Wednesday", "Good Friday")
    }

    @Test
    fun windowAndSortBehaviorMatchesIosExpectations() {
        val thisMonth =
            ObservanceQueryEngine.filter(
                observances = sampleObservances(),
                query = "",
                filter = ObservanceFilter.ALL,
                window = CalendarWindow.THIS_MONTH,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 3, 20),
            )
        val next30Days =
            ObservanceQueryEngine.filter(
                observances = sampleObservances() + beyondWindowObservance(),
                query = "",
                filter = ObservanceFilter.ALL,
                window = CalendarWindow.NEXT_30_DAYS,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 3, 1),
            )
        val requiredFirst =
            ObservanceQueryEngine.filter(
                observances = sampleObservances(),
                query = "",
                filter = ObservanceFilter.ALL,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.REQUIRED_FIRST,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 3, 1),
            )

        assertThat(thisMonth.map(Observance::title)).containsExactly("Ash Wednesday", "Good Friday").inOrder()
        assertThat(next30Days.map(Observance::title)).doesNotContain("Beyond Window")
        assertThat(requiredFirst.first().obligation).isEqualTo(ObservanceObligation.MANDATORY)
        assertThat(requiredFirst.last().obligation).isEqualTo(ObservanceObligation.NOT_APPLICABLE)
    }

    @Test
    fun seasonalPackAndDailyQuoteFallbackBehaveAsExpected() {
        val spanishAdvent = SeasonalContentPackCatalog.pack(LiturgicalSeason.ADVENT, ContentLocale.SPANISH)
        val englishLent = SeasonalContentPackCatalog.pack(LiturgicalSeason.LENT, ContentLocale.ENGLISH)
        val quote =
            SeasonalContentSupport.dailyQuote(
                season = LiturgicalSeason.LENT,
                pack = englishLent,
                date = LocalDate.of(2026, 3, 10),
            )

        assertThat(spanishAdvent.season).isEqualTo(LiturgicalSeason.ORDINARY)
        assertThat(spanishAdvent.locale).isEqualTo(ContentLocale.SPANISH)
        assertThat(SeasonalContentSupport.dailyFormationLine(englishLent, LocalDate.of(2026, 3, 10))).isNotEmpty()
        assertThat(quote).isInstanceOf(CatholicFastingQuote::class.java)
        assertThat(SacredImageryCatalog.fastingGallery).isNotEmpty()
    }

    private fun sampleObservances(): List<Observance> =
        listOf(
            testObservance(
                id = "2026-03-04|Ash Wednesday|fastAndAbstinence",
                title = "Ash Wednesday",
                date = "2026-03-04",
                obligation = ObservanceObligation.MANDATORY,
                detail = "One full meal and two smaller meals.",
                kind = ObservanceKind.FAST_AND_ABSTINENCE,
            ),
            testObservance(
                id = "2026-03-31|Good Friday|fastAndAbstinence",
                title = "Good Friday",
                date = "2026-03-31",
                obligation = ObservanceObligation.MANDATORY,
                detail = "Fast and abstinence are required.",
                kind = ObservanceKind.FAST_AND_ABSTINENCE,
            ),
            testObservance(
                id = "2026-09-23|Ember Day|optionalEmber",
                title = "Ember Day",
                date = "2026-09-23",
                obligation = ObservanceObligation.OPTIONAL,
                detail = "Optional in U.S. profile mode.",
                kind = ObservanceKind.OPTIONAL_EMBER,
            ),
            testObservance(
                id = "2026-05-22|Friday Penance (Outside Lent)|fridayPenance",
                title = "Friday Penance (Outside Lent)",
                date = "2026-05-22",
                obligation = ObservanceObligation.OPTIONAL,
                detail = "Choose abstinence from meat or another penitential act.",
                kind = ObservanceKind.FRIDAY_PENANCE,
            ),
            testObservance(
                id = "2026-05-10|Ascension|holyDay",
                title = "Ascension",
                date = "2026-05-10",
                obligation = ObservanceObligation.NOT_APPLICABLE,
                detail = "Holy day in some regions.",
                kind = ObservanceKind.HOLY_DAY,
            ),
        )

    private fun beyondWindowObservance(): Observance =
        testObservance(
            id = "2026-04-01|Beyond Window|optional",
            title = "Beyond Window",
            date = "2026-04-01",
            obligation = ObservanceObligation.OPTIONAL,
            detail = null,
            kind = ObservanceKind.FEAST_DAY,
        )

    private fun testObservance(
        id: String,
        title: String,
        date: String,
        obligation: ObservanceObligation,
        detail: String?,
        kind: ObservanceKind,
    ): Observance =
        Observance(
            id = id,
            title = title,
            date = date,
            kind = kind,
            obligation = obligation,
            detail = detail,
            rationale = "test",
            citations = emptyList(),
            ruleVersion = "test",
        )
}
