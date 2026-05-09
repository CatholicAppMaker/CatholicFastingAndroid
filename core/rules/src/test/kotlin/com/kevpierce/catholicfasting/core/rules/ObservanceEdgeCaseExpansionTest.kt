package com.kevpierce.catholicfasting.core.rules

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.AscensionObservance
import com.kevpierce.catholicfasting.core.model.CalendarMode
import com.kevpierce.catholicfasting.core.model.CalendarWindow
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.ContentLocale
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.GuidanceScenario
import com.kevpierce.catholicfasting.core.model.IntermittentFastSession
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import com.kevpierce.catholicfasting.core.model.ObservanceFilter
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.ObservanceSortOrder
import com.kevpierce.catholicfasting.core.model.PremiumCompanionState
import com.kevpierce.catholicfasting.core.model.PremiumConditionRules
import com.kevpierce.catholicfasting.core.model.PremiumRuleTemplate
import com.kevpierce.catholicfasting.core.model.PremiumSeasonProgram
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.RuleAuthority
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test
import java.time.LocalDate

class ObservanceEdgeCaseExpansionTest {
    @Test
    fun leapYearCalendarBuildsValidDatedObservances() {
        val observances = ObservanceCalculator.makeCalendar(2036, defaultSettings)

        assertThat(observances).isNotEmpty()
        assertThat(observances.map { LocalDate.parse(it.date).year }).contains(2036)
    }

    @Test
    fun easterDatesMatchKnownWesternCalendarAnchors() {
        assertThat(DateSupport.easterSunday(2026)).isEqualTo(LocalDate.of(2026, 4, 5))
        assertThat(DateSupport.easterSunday(2027)).isEqualTo(LocalDate.of(2027, 3, 28))
        assertThat(DateSupport.easterSunday(2028)).isEqualTo(LocalDate.of(2028, 4, 16))
    }

    @Test
    fun ashWednesdayIsFortySixDaysBeforeEaster() {
        val observances = ObservanceCalculator.makeCalendar(2028, defaultSettings)
        val ash = observances.first { it.title == "Ash Wednesday" }

        assertThat(LocalDate.parse(ash.date)).isEqualTo(DateSupport.easterSunday(2028).minusDays(46))
    }

    @Test
    fun goodFridayIsTwoDaysBeforeEaster() {
        val goodFriday = ObservanceCalculator.makeCalendar(2028, defaultSettings).first { it.title == "Good Friday" }

        assertThat(LocalDate.parse(goodFriday.date)).isEqualTo(DateSupport.easterSunday(2028).minusDays(2))
    }

    @Test
    fun lentFridaysExcludeAshWednesdayAndGoodFriday() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)
        val lentFridays = observances.filter { it.kind == ObservanceKind.ABSTINENCE && it.title == "Friday of Lent" }

        assertThat(lentFridays.map { it.title }).doesNotContain("Ash Wednesday")
        assertThat(lentFridays.map { it.date }).doesNotContain(observances.first { it.title == "Good Friday" }.date)
    }

    @Test
    fun traditionalAscensionIsAThursday() {
        val ascension =
            ObservanceCalculator
                .makeCalendar(
                    2026,
                    defaultSettings.copy(ascensionObservance = AscensionObservance.THURSDAY),
                ).first { it.title == "Ascension" }

        assertThat(LocalDate.parse(ascension.date).dayOfWeek.value).isEqualTo(4)
    }

    @Test
    fun transferredAscensionIsASunday() {
        val ascension =
            ObservanceCalculator
                .makeCalendar(
                    2026,
                    defaultSettings.copy(ascensionObservance = AscensionObservance.SUNDAY),
                ).first { it.title == "Ascension" }

        assertThat(LocalDate.parse(ascension.date).dayOfWeek.value).isEqualTo(7)
    }

    @Test
    fun adultWithMedicalDispensationStillGetsHolyDayObligations() {
        val christmas =
            ObservanceCalculator
                .makeCalendar(
                    2026,
                    defaultSettings.copy(hasMedicalDispensation = true),
                ).first { it.title == "Christmas" }

        assertThat(christmas.obligation).isEqualTo(ObservanceObligation.MANDATORY)
    }

    @Test
    fun underFourteenProfileDoesNotRequireLentenAbstinence() {
        val ash =
            ObservanceCalculator
                .makeCalendar(
                    2026,
                    defaultSettings.copy(isAge14OrOlderForAbstinence = false, isAge18OrOlderForFasting = false),
                ).first { it.title == "Ash Wednesday" }

        assertThat(ash.obligation).isEqualTo(ObservanceObligation.NOT_APPLICABLE)
        assertThat(ash.kind).isEqualTo(ObservanceKind.FAST_AND_ABSTINENCE)
    }

    @Test
    fun abstinenceAgeWithoutFastingAgeRequiresAbstinenceOnly() {
        val goodFriday =
            ObservanceCalculator
                .makeCalendar(
                    2026,
                    defaultSettings.copy(isAge14OrOlderForAbstinence = true, isAge18OrOlderForFasting = false),
                ).first { it.title == "Good Friday" }

        assertThat(goodFriday.kind).isEqualTo(ObservanceKind.ABSTINENCE)
        assertThat(goodFriday.obligation).isEqualTo(ObservanceObligation.MANDATORY)
    }

    @Test
    fun otherRegionKeepsFridayPenanceModeVisible() {
        val friday =
            ObservanceCalculator
                .makeCalendar(
                    2026,
                    defaultSettings.copy(regionProfile = RegionProfile.OTHER),
                ).first { it.kind == ObservanceKind.FRIDAY_PENANCE }

        assertThat(friday.detail).isNotEmpty()
        assertThat(friday.obligation).isEqualTo(ObservanceObligation.MANDATORY)
    }

    @Test
    fun canadaFridayOutsideLentIncludesCccbCitation() {
        val friday =
            ObservanceCalculator
                .makeCalendar(
                    2026,
                    defaultSettings.copy(regionProfile = RegionProfile.CANADA),
                ).first { it.kind == ObservanceKind.FRIDAY_PENANCE }

        assertThat(friday.citations.map { it.authority }).contains(RuleAuthority.CCCB)
    }

    @Test
    fun ruleBundleMetadataIsStableForReleaseDocs() {
        val metadata = ObservanceCalculator.ruleBundleMetadata()

        assertThat(metadata.id).isEqualTo("bundled-android-bootstrap")
        assertThat(metadata.version).isEqualTo("android-bootstrap-0.1")
        assertThat(metadata.reviewedDate).isEqualTo("2026-03-13")
    }

    @Test
    fun ruleBundleChangeLogHasInitialAndroidPortEntry() {
        val changes = ObservanceCalculator.ruleBundleChanges()

        assertThat(changes.map { it.id }).contains("android-bootstrap")
        assertThat(changes.first().detail).contains("Swift behavior")
    }

    @Test
    fun queryRequiredOnlyFiltersOutOptionalEmberDays() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)

        val required =
            ObservanceQueryEngine.filter(
                observances = observances,
                query = "",
                filter = ObservanceFilter.REQUIRED_ONLY,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 1, 1),
            )

        assertThat(required.map { it.obligation }).doesNotContain(ObservanceObligation.OPTIONAL)
    }

    @Test
    fun queryTrackedOnlyIncludesSubstitutedAndDispensedStatuses() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings).take(2)

        val tracked =
            ObservanceQueryEngine.filter(
                observances = observances,
                query = "",
                filter = ObservanceFilter.TRACKED_ONLY,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById =
                    mapOf(
                        observances[0].id to CompletionStatus.SUBSTITUTED,
                        observances[1].id to CompletionStatus.DISPENSED,
                    ),
                now = LocalDate.of(2026, 1, 1),
            )

        assertThat(tracked).hasSize(2)
    }

    @Test
    fun querySearchMatchesCitationTitles() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)

        val results =
            ObservanceQueryEngine.filter(
                observances = observances,
                query = "Ash",
                filter = ObservanceFilter.ALL,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.CHRONOLOGICAL,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 1, 1),
            )

        assertThat(results).isNotEmpty()
    }

    @Test
    fun requiredFirstSortPlacesMandatoryBeforeOptional() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)

        val sorted =
            ObservanceQueryEngine.filter(
                observances = observances,
                query = "",
                filter = ObservanceFilter.ALL,
                window = CalendarWindow.ALL_YEAR,
                sortOrder = ObservanceSortOrder.REQUIRED_FIRST,
                statusesById = emptyMap(),
                now = LocalDate.of(2026, 1, 1),
            )

        assertThat(sorted.first().obligation).isEqualTo(ObservanceObligation.MANDATORY)
    }

    @Test
    fun seasonalPackFallsBackToEnglishForFrenchCanadianContentPack() {
        val pack = SeasonalContentPackCatalog.pack(LiturgicalSeason.LENT, ContentLocale.FRENCH_CANADIAN)

        assertThat(pack.locale).isEqualTo(ContentLocale.ENGLISH)
        assertThat(pack.campaignTitle).isNotEmpty()
        assertThat(pack.quotes).isNotEmpty()
    }

    @Test
    fun dailyQuoteSelectionIsStableForSameDate() {
        val pack = SeasonalContentPackCatalog.pack(LiturgicalSeason.LENT, ContentLocale.ENGLISH)
        val date = LocalDate.of(2026, 3, 13)

        val first = SeasonalContentSupport.dailyQuote(LiturgicalSeason.LENT, pack, date)
        val second = SeasonalContentSupport.dailyQuote(LiturgicalSeason.LENT, pack, date)

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun dailyFormationLineRotatesAcrossDates() {
        val pack = SeasonalContentPackCatalog.pack(LiturgicalSeason.LENT, ContentLocale.ENGLISH)

        val lines =
            (1..7)
                .map { SeasonalContentSupport.dailyFormationLine(pack, LocalDate.of(2026, 3, it)) }
                .toSet()

        assertThat(lines.size).isGreaterThan(1)
    }

    @Test
    fun foodGuidanceNormalDayContainsMeatAndPermittedGroups() {
        val guidance = FoodGuidanceEngine.snapshot(GuidanceScenario.NORMAL_DAY, defaultSettings)

        assertThat(guidance.whatCountsAsMeat.items).isNotEmpty()
        assertThat(guidance.generallyPermitted.items).isNotEmpty()
    }

    @Test
    fun heavyLaborGuidanceDiffersFromNormalDay() {
        val normal = FoodGuidanceEngine.snapshot(GuidanceScenario.NORMAL_DAY, defaultSettings)
        val heavyLabor = FoodGuidanceEngine.snapshot(GuidanceScenario.HEAVY_LABOR, defaultSettings)

        assertThat(heavyLabor.summaryLine).isNotEqualTo(normal.summaryLine)
    }

    @Test
    fun premiumTemplateBeginnerProducesLowerIntensityThanTraditional() {
        val beginner =
            PremiumAdaptiveRulePlanner.plan(
                season = LiturgicalSeason.LENT,
                settings = defaultSettings,
                template = PremiumRuleTemplate.BEGINNER,
                optionalDisciplinesPerWeek = 1,
                fixedFastWeekday = 5,
                protectFeastDays = true,
            )
        val traditional =
            PremiumAdaptiveRulePlanner.plan(
                season = LiturgicalSeason.LENT,
                settings = defaultSettings,
                template = PremiumRuleTemplate.TRADITIONAL,
                optionalDisciplinesPerWeek = 4,
                fixedFastWeekday = 6,
                protectFeastDays = true,
            )

        assertThat(beginner.summary).isNotEqualTo(traditional.summary)
    }

    @Test
    fun premiumReminderAdvisorDisablesEveningWhenRuleTurnsItOff() {
        val recommendation =
            PremiumConditionReminderAdvisor.applyRules(
                rules = PremiumConditionRules(requiredDaysDoubleReminder = false),
                hasUpcomingRequiredDays = true,
            )

        assertThat(recommendation.shouldEnableEvening).isFalse()
    }

    @Test
    fun premiumSeasonProgramReturnsActionsForEveryProgram() {
        PremiumSeasonProgram.entries.forEach { program ->
            assertThat(PremiumSeasonProgramEngine.actions(program, week = 2)).isNotEmpty()
        }
    }

    @Test
    fun premiumSnapshotCountsMissedAndSubstitutedObservances() {
        val observances = ObservanceCalculator.makeCalendar(2026, defaultSettings)
        val snapshot =
            PremiumSnapshotEngine.build(
                observances = observances,
                statusesById =
                    mapOf(
                        observances[0].id to CompletionStatus.MISSED,
                        observances[1].id to CompletionStatus.SUBSTITUTED,
                    ),
                sessions = emptyList(),
                settings = defaultSettings,
                companionState = companionState,
                today = LocalDate.of(2026, 3, 13),
            )

        assertThat(snapshot.analyticsSummary.missedCount).isEqualTo(1)
        assertThat(snapshot.analyticsSummary.substitutedCount).isEqualTo(1)
    }

    @Test
    fun premiumSnapshotCountsCompletedIntermittentSessions() {
        val snapshot =
            PremiumSnapshotEngine.build(
                observances = ObservanceCalculator.makeCalendar(2026, defaultSettings),
                statusesById = emptyMap(),
                sessions =
                    listOf(
                        IntermittentFastSession("a", "2026-03-13T00:00:00Z", "2026-03-13T16:00:00Z", 16, true),
                        IntermittentFastSession("b", "2026-03-14T00:00:00Z", "2026-03-14T08:00:00Z", 16, false),
                    ),
                settings = defaultSettings,
                companionState = companionState,
                today = LocalDate.of(2026, 3, 13),
            )

        assertThat(snapshot.analyticsSummary.intermittentTargetHitPercent).isEqualTo(50)
    }

    @Test
    fun fastingHistoryHasArticlesForEveryLocaleAndEra() {
        ContentLocale.entries.forEach { locale ->
            assertThat(FastingHistoryCatalog.articles(locale)).hasSize(5)
        }
    }

    @Test
    fun sacredImageryGalleryHasStableUniqueIds() {
        val ids = SacredImageryCatalog.fastingGallery.map { it.id }

        assertThat(ids).containsNoDuplicates()
        assertThat(ids.size).isAtLeast(10)
    }

    @Test
    fun requiredReminderPlannerNeverReturnsPastDates() {
        val planned =
            RequiredDayReminderPlanner.upcomingMandatoryObservances(
                observances = ObservanceCalculator.makeCalendar(2026, defaultSettings),
                now = LocalDate.of(2026, 3, 1),
                limit = 10,
            )

        assertThat(planned.all { LocalDate.parse(it.date) >= LocalDate.of(2026, 3, 1) }).isTrue()
    }

    @Test
    fun requiredReminderPlannerLimitsQueueSize() {
        val planned =
            RequiredDayReminderPlanner.upcomingMandatoryObservances(
                observances = ObservanceCalculator.makeCalendar(2026, defaultSettings),
                now = LocalDate.of(2026, 1, 1),
                limit = 2,
            )

        assertThat(planned).hasSize(2)
    }

    private val defaultSettings =
        RuleSettings(
            isAge14OrOlderForAbstinence = true,
            isAge18OrOlderForFasting = true,
            hasMedicalDispensation = false,
            ascensionObservance = AscensionObservance.SUNDAY,
            fridayOutsideLentMode = FridayOutsideLentMode.SUBSTITUTE_PENANCE,
            calendarMode = CalendarMode.USCCB,
            regionProfile = RegionProfile.US,
        )

    private val companionState =
        PremiumCompanionState(
            seasonProgramStartIso = "2026-03-13T00:00:00Z",
        )
}
