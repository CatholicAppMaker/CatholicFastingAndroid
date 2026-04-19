package com.kevpierce.catholicfasting.core.rules

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.AscensionObservance
import com.kevpierce.catholicfasting.core.model.CalendarMode
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.GuidanceScenario
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.RuleAuthority
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ObservanceCalculatorTest {
    private val defaultSettings =
        RuleSettings(
            birthYear = 1990,
            hasMedicalDispensation = false,
            ascensionObservance = AscensionObservance.SUNDAY,
            fridayOutsideLentMode = FridayOutsideLentMode.SUBSTITUTE_PENANCE,
        )

    @Test
    fun ashWednesdayAndGoodFridayArePresent() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)

        assertThat(observances.any { it.title == "Ash Wednesday" }).isTrue()
        assertThat(observances.any { it.title == "Good Friday" }).isTrue()
    }

    @Test
    fun ashWednesdayUsesFastAndAbstinenceForAdultProfile() {
        val ash = ObservanceCalculator.makeCalendar(2026, defaultSettings).first { it.title == "Ash Wednesday" }

        assertThat(ash.kind).isEqualTo(ObservanceKind.FAST_AND_ABSTINENCE)
        assertThat(ash.obligation).isEqualTo(ObservanceObligation.MANDATORY)
    }

    @Test
    fun teenProfileGetsAbstinenceOnlyOnAshWednesday() {
        val teen =
            defaultSettings.copy(
                birthYear = 2010,
                birthMonth = 1,
                birthDay = 1,
                isAge18OrOlderForFasting = false,
            )

        val ash = ObservanceCalculator.makeCalendar(2026, teen).first { it.title == "Ash Wednesday" }

        assertThat(ash.kind).isEqualTo(ObservanceKind.ABSTINENCE)
        assertThat(ash.detail).contains("Abstinence from meat")
    }

    @Test
    fun medicalDispensationRemovesMandatoryFasting() {
        val observances =
            ObservanceCalculator.makeCalendar(
                2026,
                defaultSettings.copy(hasMedicalDispensation = true),
            )

        val ash = observances.first { it.title == "Ash Wednesday" }
        val goodFriday = observances.first { it.title == "Good Friday" }

        assertThat(ash.obligation).isEqualTo(ObservanceObligation.NOT_APPLICABLE)
        assertThat(goodFriday.obligation).isEqualTo(ObservanceObligation.NOT_APPLICABLE)
    }

    @Test
    fun underSevenHolyDayIsNotRequired() {
        val child = defaultSettings.copy(birthYear = 2022)

        val christmas = ObservanceCalculator.makeCalendar(2026, child).first { it.title == "Christmas" }

        assertThat(christmas.obligation).isEqualTo(ObservanceObligation.NOT_APPLICABLE)
    }

    @Test
    fun ascensionDifferenceIsThreeDaysBetweenModes() {
        val sunday =
            ObservanceCalculator.makeCalendar(2026, defaultSettings)
                .first { it.title == "Ascension" }.date
        val thursday =
            ObservanceCalculator.makeCalendar(
                2026,
                defaultSettings.copy(ascensionObservance = AscensionObservance.THURSDAY),
            ).first { it.title == "Ascension" }.date

        val days = ChronoUnit.DAYS.between(LocalDate.parse(thursday), LocalDate.parse(sunday))
        assertThat(days).isEqualTo(3)
    }

    @Test
    fun christmasIsMandatoryForAdultProfile() {
        val christmas = ObservanceCalculator.makeCalendar(2026, defaultSettings).first { it.title == "Christmas" }

        assertThat(christmas.obligation).isEqualTo(ObservanceObligation.MANDATORY)
    }

    @Test
    fun lentSeasonDateIsDetected() {
        assertThat(LiturgicalSeasonThemeEngine.seasonFor(LocalDate.of(2026, 3, 10))).isEqualTo(LiturgicalSeason.LENT)
    }

    @Test
    fun fridayModeAbstainFromMeatChangesDetail() {
        val firstFridayOutsideLent =
            ObservanceCalculator.makeCalendar(
                2026,
                defaultSettings.copy(fridayOutsideLentMode = FridayOutsideLentMode.ABSTAIN_FROM_MEAT),
            ).first { it.kind == ObservanceKind.FRIDAY_PENANCE }

        assertThat(firstFridayOutsideLent.detail).contains("abstain from meat")
    }

    @Test
    fun outsideLentFridaysAreGenerated() {
        val outsideLentFridays =
            ObservanceCalculator.makeCalendar(2026, defaultSettings)
                .filter { it.kind == ObservanceKind.FRIDAY_PENANCE }

        assertThat(outsideLentFridays.size).isGreaterThan(40)
        assertThat(outsideLentFridays.all { LocalDate.parse(it.date).dayOfWeek.value == 5 }).isTrue()
    }

    @Test
    fun fridayPenanceOutsideLentNeverIncludesGoodFriday() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)
        val outsideLent = observances.filter { it.kind == ObservanceKind.FRIDAY_PENANCE }
        val goodFriday = observances.first { it.title == "Good Friday" }

        assertThat(outsideLent.map { it.date }).doesNotContain(goodFriday.date)
    }

    @Test
    fun immaculateConceptionIsMandatoryWhenNotTransferred() {
        val immaculate =
            ObservanceCalculator.makeCalendar(2026, defaultSettings)
                .first { it.title == "Immaculate Conception" }

        assertThat(immaculate.obligation).isEqualTo(ObservanceObligation.MANDATORY)
    }

    @Test
    fun fridayOutsideLentDetailReflectsSelectedMode() {
        val abstain =
            ObservanceCalculator.makeCalendar(
                2026,
                defaultSettings.copy(fridayOutsideLentMode = FridayOutsideLentMode.ABSTAIN_FROM_MEAT),
            ).first { it.kind == ObservanceKind.FRIDAY_PENANCE }
        val substitute =
            ObservanceCalculator.makeCalendar(
                2026,
                defaultSettings.copy(fridayOutsideLentMode = FridayOutsideLentMode.SUBSTITUTE_PENANCE),
            ).first { it.kind == ObservanceKind.FRIDAY_PENANCE }

        assertThat(abstain.detail).contains("abstain from meat")
        assertThat(substitute.detail).contains("penitential act")
        assertThat(abstain.detail).isNotEqualTo(substitute.detail)
    }

    @Test
    fun memorialDaysIncludeFixedAndMoveableEntries() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)

        val kateri =
            observances.firstOrNull {
                it.title == "Saint Kateri Tekakwitha, Virgin" &&
                    it.kind == ObservanceKind.MEMORIAL_DAY
            }
        val motherChurch =
            observances.firstOrNull {
                it.title == "Blessed Virgin Mary, Mother of the Church" &&
                    it.kind == ObservanceKind.MEMORIAL_DAY
            }

        assertThat(kateri?.date).isEqualTo("2026-07-14")
        assertThat(motherChurch?.date).isEqualTo("2026-05-25")
        assertThat(kateri?.obligation).isEqualTo(ObservanceObligation.NOT_APPLICABLE)
    }

    @Test
    fun canadaHolyDaysUseNationalBaselineObligations() {
        val christmas =
            ObservanceCalculator.makeCalendar(
                2026,
                defaultSettings.copy(regionProfile = RegionProfile.CANADA),
            ).first { it.title == "Christmas" }

        assertThat(christmas.obligation).isEqualTo(ObservanceObligation.OPTIONAL)
        assertThat(christmas.rationale).contains("Canada")
    }

    @Test
    fun canadaFridayPenanceUsesCccbGuidanceAndCitations() {
        val canadaFriday =
            ObservanceCalculator.makeCalendar(
                2026,
                defaultSettings.copy(regionProfile = RegionProfile.CANADA),
            ).first {
                it.title == "Friday Penance (Outside Lent)" && it.date.startsWith("2026-05-01")
            }

        assertThat(canadaFriday.obligation).isEqualTo(ObservanceObligation.MANDATORY)
        assertThat(canadaFriday.detail).contains("Canada")
        assertThat(canadaFriday.detail).contains("charity or piety")
        assertThat(canadaFriday.rationale).contains("CCCB")
        assertThat(canadaFriday.citations.map { it.authority }).contains(RuleAuthority.CCCB)
    }

    @Test
    fun transferredImmaculateConceptionIsNotMandatoryInUs() {
        val transferred =
            ObservanceCalculator.makeCalendar(2024, defaultSettings)
                .first { it.title == "Immaculate Conception (Transferred)" }

        assertThat(transferred.obligation).isEqualTo(ObservanceObligation.OPTIONAL)
        assertThat(transferred.detail).contains("does not transfer")
    }

    @Test
    fun traditionalModeMarksEmberDetailDifferently() {
        val usccbEmber =
            ObservanceCalculator.makeCalendar(2026, defaultSettings)
                .first { it.kind == ObservanceKind.OPTIONAL_EMBER }
        val traditionalEmber =
            ObservanceCalculator.makeCalendar(
                2026,
                defaultSettings.copy(
                    calendarMode = CalendarMode.TRADITIONAL_1962,
                ),
            ).first { it.kind == ObservanceKind.OPTIONAL_EMBER }

        assertThat(usccbEmber.detail).isNotEqualTo(traditionalEmber.detail)
        assertThat(traditionalEmber.detail).contains("Traditional")
    }

    @Test
    fun holyDayWeekdayAbrogationRuleForAllSaints() {
        val allSaints =
            ObservanceCalculator.makeCalendar(2025, defaultSettings)
                .first { it.title == "All Saints" }

        assertThat(LocalDate.parse(allSaints.date).dayOfWeek.value).isEqualTo(6)
        assertThat(allSaints.obligation).isEqualTo(ObservanceObligation.OPTIONAL)
        assertThat(allSaints.detail).contains("abrogated")
    }

    @Test
    fun observancesAreSortedByDateAscending() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)

        assertThat(observances).isEqualTo(observances.sortedBy { it.date })
    }

    @Test
    fun foodGuidanceMedicalRecoveryMentionsDispensation() {
        val recommendations =
            FoodGuidanceEngine.recommendations(
                scenario = GuidanceScenario.MEDICAL_RECOVERY,
                settings = defaultSettings,
            )

        assertThat(recommendations.any { it.contains("dispensation", ignoreCase = true) }).isTrue()
    }

    @Test
    fun foodGuidanceCanadaSnapshotIncludesCccbSourceLine() {
        val snapshot =
            FoodGuidanceEngine.snapshot(
                scenario = GuidanceScenario.NORMAL_DAY,
                settings = defaultSettings.copy(regionProfile = RegionProfile.CANADA),
            )

        assertThat(snapshot.sourceLine).contains("CCCB")
        assertThat(
            snapshot.generallyPermitted.items.any { it.detail.contains("cheese", ignoreCase = true) },
        ).isTrue()
        assertThat(
            snapshot.extraGuidance.items.any { it.detail.contains("broth", ignoreCase = true) },
        ).isTrue()
    }

    @Test
    fun requiredDayReminderPlannerSortsFiltersAndLimits() {
        val observances =
            listOf(
                testObservance("future-b", "Future B", "2026-02-20", ObservanceObligation.MANDATORY),
                testObservance("past", "Past", "2026-02-17", ObservanceObligation.MANDATORY),
                testObservance("today", "Today", "2026-02-18", ObservanceObligation.MANDATORY),
                testObservance("optional", "Optional", "2026-02-19", ObservanceObligation.OPTIONAL),
                testObservance("future-a", "Future A", "2026-02-19", ObservanceObligation.MANDATORY),
            )

        val planned =
            RequiredDayReminderPlanner.upcomingMandatoryObservances(
                observances = observances,
                now = LocalDate.of(2026, 2, 18),
                limit = 2,
            )

        assertThat(planned.map { it.id }).containsExactly("today", "future-a").inOrder()
        assertThat(RequiredDayReminderPlanner.maximumRequiredReminders(20)).isEqualTo(44)
        assertThat(
            RequiredDayReminderPlanner.additionalRequiredReminderSlots(
                existingRequiredPendingCount = 10,
                existingNonRequiredPendingCount = 10,
            ),
        ).isEqualTo(40)
    }

    private fun testObservance(
        id: String,
        title: String,
        date: String,
        obligation: ObservanceObligation,
    ): Observance =
        Observance(
            id = id,
            title = title,
            date = date,
            kind = ObservanceKind.HOLY_DAY,
            obligation = obligation,
            rationale = "test",
            citations = emptyList(),
            ruleVersion = "test",
        )
}
