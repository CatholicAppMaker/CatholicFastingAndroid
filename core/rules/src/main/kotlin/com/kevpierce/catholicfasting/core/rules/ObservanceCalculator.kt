@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfasting.core.rules

import com.kevpierce.catholicfasting.core.model.AscensionObservance
import com.kevpierce.catholicfasting.core.model.CalendarMode
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceKind
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.RuleAuthority
import com.kevpierce.catholicfasting.core.model.RuleBundleAudit
import com.kevpierce.catholicfasting.core.model.RuleBundleChange
import com.kevpierce.catholicfasting.core.model.RuleBundleMetadata
import com.kevpierce.catholicfasting.core.model.RuleCitation
import com.kevpierce.catholicfasting.core.model.RuleSettings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

private const val RULE_VERSION = "android-bootstrap-0.1"

object ObservanceCalculator {
    fun ruleBundleMetadata(): RuleBundleMetadata =
        RuleBundleMetadata(
            id = "bundled-android-bootstrap",
            displayName = "Bundled Catholic Fasting Rules",
            version = RULE_VERSION,
            effectiveDate = "2026-01-01",
            reviewedDate = "2026-03-13",
        )

    fun ruleBundleAudit(): RuleBundleAudit =
        RuleBundleAudit(
            source = "bundled",
            isVerified = true,
            warnings = emptyList(),
        )

    fun ruleBundleChanges(): List<RuleBundleChange> =
        listOf(
            RuleBundleChange(
                id = "android-bootstrap",
                date = "2026-03-13",
                title = "Initial Android rule port",
                detail = "Bootstrapped the Android rules engine from the current Swift behavior.",
            ),
        )

    fun makeCalendar(
        year: Int,
        settings: RuleSettings,
    ): List<Observance> = buildCalendar(year, settings)
}

private data class LiturgicalDates(
    val easter: LocalDate,
    val ashWednesday: LocalDate,
    val goodFriday: LocalDate,
    val holySaturday: LocalDate,
    val pentecost: LocalDate,
    val ascension: LocalDate,
)

private fun buildCalendar(
    year: Int,
    settings: RuleSettings,
): List<Observance> {
    val dates = calculateLiturgicalDates(year, settings)
    val items = mutableListOf<Observance>()

    items += buildMandatoryFastObservances(dates, settings)
    items += buildLentFridayObservances(dates, settings)
    items += buildFridayPenanceObservances(year, dates, settings)
    items += buildHolyDayObservances(year, dates.ascension, settings)
    items += buildFeastDayObservances(year, dates, settings)
    items += buildMemorialObservances(year, dates, settings)
    items += buildEmberDayObservances(year, dates, settings)

    return items.distinctBy { it.id }.sortedBy { it.date }
}

private fun calculateLiturgicalDates(
    year: Int,
    settings: RuleSettings,
): LiturgicalDates {
    val easter = DateSupport.easterSunday(year)
    val ashWednesday = easter.minusDays(46)
    val goodFriday = easter.minusDays(2)
    val holySaturday = easter.minusDays(1)
    val pentecost = easter.plusDays(49)
    val ascensionOffset =
        if (settings.ascensionObservance == AscensionObservance.SUNDAY) 42 else 39

    return LiturgicalDates(
        easter = easter,
        ashWednesday = ashWednesday,
        goodFriday = goodFriday,
        holySaturday = holySaturday,
        pentecost = pentecost,
        ascension = easter.plusDays(ascensionOffset.toLong()),
    )
}

private fun buildMandatoryFastObservances(
    dates: LiturgicalDates,
    settings: RuleSettings,
): List<Observance> =
    listOf(
        fastAndAbstinenceObservance("Ash Wednesday", dates.ashWednesday, settings),
        fastAndAbstinenceObservance("Good Friday", dates.goodFriday, settings),
    )

private fun buildLentFridayObservances(
    dates: LiturgicalDates,
    settings: RuleSettings,
): List<Observance> {
    val abstinenceRequired = isAbstinenceRequired(settings)
    val obligation =
        if (abstinenceRequired) {
            ObservanceObligation.MANDATORY
        } else {
            ObservanceObligation.NOT_APPLICABLE
        }
    val detail =
        if (abstinenceRequired) {
            "No meat from mammals or poultry."
        } else {
            ageDispensationDetail(settings)
        }

    return lentFridays(dates.ashWednesday, dates.holySaturday)
        .filterNot { it == dates.goodFriday }
        .map { friday ->
            makeObservance(
                title = "Friday of Lent",
                date = friday,
                kind = ObservanceKind.ABSTINENCE,
                obligation = obligation,
                detail = detail,
                settings = settings,
            )
        }
}

private fun buildFridayPenanceObservances(
    year: Int,
    dates: LiturgicalDates,
    settings: RuleSettings,
): List<Observance> {
    val abstinenceRequired = isAbstinenceRequired(settings)
    val obligation =
        if (abstinenceRequired) {
            ObservanceObligation.MANDATORY
        } else {
            ObservanceObligation.NOT_APPLICABLE
        }

    return fridaysOutsideLent(year, dates.ashWednesday, dates.holySaturday).map { friday ->
        makeObservance(
            title = "Friday Penance (Outside Lent)",
            date = friday,
            kind = ObservanceKind.FRIDAY_PENANCE,
            obligation = obligation,
            detail = fridayPenanceDisplayDetail(abstinenceRequired, settings),
            settings = settings,
        )
    }
}

private fun fridayPenanceDisplayDetail(
    abstinenceRequired: Boolean,
    settings: RuleSettings,
): String =
    if (abstinenceRequired) {
        fridayPenanceDetail(settings.fridayOutsideLentMode, settings)
    } else {
        ageDispensationDetail(settings)
    }

private fun buildHolyDayObservances(
    year: Int,
    ascension: LocalDate,
    settings: RuleSettings,
): List<Observance> =
    holyDays(year, ascension).map { (title, date) ->
        makeObservance(
            title = title,
            date = date,
            kind = ObservanceKind.HOLY_DAY,
            obligation = holyDayObligation(title, date, settings),
            detail = holyDayDetail(title, date, settings),
            settings = settings,
        )
    }

private fun buildFeastDayObservances(
    year: Int,
    dates: LiturgicalDates,
    settings: RuleSettings,
): List<Observance> =
    feastDays(year, dates.easter, dates.pentecost).map { feast ->
        makeObservance(
            title = feast.first,
            date = feast.second,
            kind = ObservanceKind.FEAST_DAY,
            obligation = ObservanceObligation.NOT_APPLICABLE,
            detail = feast.third,
            settings = settings,
        )
    }

private fun buildMemorialObservances(
    year: Int,
    dates: LiturgicalDates,
    settings: RuleSettings,
): List<Observance> =
    memorialDays(year, dates, settings).map { memorial ->
        makeObservance(
            title = memorial.first,
            date = memorial.second,
            kind = ObservanceKind.MEMORIAL_DAY,
            obligation = ObservanceObligation.NOT_APPLICABLE,
            detail = memorial.third,
            settings = settings,
        )
    }

private fun buildEmberDayObservances(
    year: Int,
    dates: LiturgicalDates,
    settings: RuleSettings,
): List<Observance> =
    emberDays(year, dates.ashWednesday, dates.pentecost).map { ember ->
        makeObservance(
            title = "Ember Day",
            date = ember,
            kind = ObservanceKind.OPTIONAL_EMBER,
            obligation = ObservanceObligation.OPTIONAL,
            detail = emberDayDetail(settings),
            settings = settings,
        )
    }

private fun emberDayDetail(settings: RuleSettings): String =
    if (settings.calendarMode == CalendarMode.TRADITIONAL_1962) {
        "Traditional calendar mode: Ember day of prayer, fasting, and abstinence."
    } else {
        "Optional observance in U.S. profile mode."
    }

private fun fastAndAbstinenceObservance(
    title: String,
    date: LocalDate,
    settings: RuleSettings,
): Observance {
    val fastRequired = isFastRequired(settings)
    val abstinenceRequired = isAbstinenceRequired(settings)

    return when {
        fastRequired && abstinenceRequired ->
            makeObservance(
                title = title,
                date = date,
                kind = ObservanceKind.FAST_AND_ABSTINENCE,
                obligation = ObservanceObligation.MANDATORY,
                detail = fastDetail(settings),
                settings = settings,
            )
        abstinenceRequired ->
            makeObservance(
                title = title,
                date = date,
                kind = ObservanceKind.ABSTINENCE,
                obligation = ObservanceObligation.MANDATORY,
                detail = "Abstinence from meat is required. Fasting does not bind for your age profile.",
                settings = settings,
            )
        else ->
            makeObservance(
                title = title,
                date = date,
                kind = ObservanceKind.FAST_AND_ABSTINENCE,
                obligation = ObservanceObligation.NOT_APPLICABLE,
                detail = ageDispensationDetail(settings),
                settings = settings,
            )
    }
}

private fun makeObservance(
    title: String,
    date: LocalDate,
    kind: ObservanceKind,
    obligation: ObservanceObligation,
    detail: String?,
    settings: RuleSettings,
): Observance {
    val dayKey = DateSupport.formatDayKey(date)
    return Observance(
        id = "$dayKey|$title|${kind.name.lowercase()}",
        title = title,
        date = dayKey,
        kind = kind,
        obligation = obligation,
        detail = detail,
        rationale = defaultRationale(title, kind, obligation, settings),
        citations = defaultCitations(title, kind, settings),
        ruleVersion = ObservanceCalculator.ruleBundleMetadata().version,
    )
}

private fun holyDays(
    year: Int,
    ascension: LocalDate,
): List<Pair<String, LocalDate>> {
    val days = mutableListOf<Pair<String, LocalDate>>()
    days += "Mary, Mother of God" to LocalDate.of(year, Month.JANUARY, 1)
    days += "Assumption of the Blessed Virgin Mary" to LocalDate.of(year, Month.AUGUST, 15)
    days += "All Saints" to LocalDate.of(year, Month.NOVEMBER, 1)
    days += "Christmas" to LocalDate.of(year, Month.DECEMBER, 25)

    val dec8 = LocalDate.of(year, Month.DECEMBER, 8)
    days +=
        if (dec8.dayOfWeek == DayOfWeek.SUNDAY) {
            "Immaculate Conception (Transferred)" to dec8.plusDays(1)
        } else {
            "Immaculate Conception" to dec8
        }
    days += "Ascension" to ascension
    return days
}

private fun feastDays(
    year: Int,
    easter: LocalDate,
    pentecost: LocalDate,
): List<Triple<String, LocalDate, String>> {
    val detail = "Included from the liturgical calendar used for devotional planning."
    return listOf(
        Triple("Epiphany of the Lord", DateSupport.epiphanySunday(year), detail),
        Triple(
            "The Baptism of the Lord",
            DateSupport.epiphanySunday(year).plusWeeks(1),
            detail,
        ),
        Triple("Palm Sunday of the Passion of the Lord", easter.minusDays(7), detail),
        Triple(
            "Holy Thursday (Evening Mass of the Lord's Supper)",
            easter.minusDays(3),
            detail,
        ),
        Triple("Easter Sunday", easter, detail),
        Triple("Pentecost", pentecost, detail),
        Triple(
            "Our Lord Jesus Christ, King of the Universe",
            DateSupport.firstSundayOfAdvent(year).minusDays(7),
            detail,
        ),
        Triple(
            "The Holy Family of Jesus, Mary, and Joseph",
            DateSupport.holyFamilyDate(year),
            detail,
        ),
    )
}

private fun memorialDays(
    year: Int,
    dates: LiturgicalDates,
    settings: RuleSettings,
): List<Triple<String, LocalDate, String>> {
    val defaultDetail =
        when (settings.regionProfile) {
            RegionProfile.US ->
                "Memorial included for liturgical awareness in the U.S. calendar profile."
            RegionProfile.CANADA ->
                "Memorial included for liturgical awareness in the Canada " +
                    "profile. Regional proper calendars may differ."
            RegionProfile.OTHER ->
                "Memorial included for liturgical awareness. Local calendars may differ."
        }

    val items =
        memorialCatalogEntries()
            .map { entry ->
                Triple(
                    entry.title,
                    LocalDate.of(year, entry.month, entry.day),
                    defaultDetail,
                )
            }
            .toMutableList()

    items +=
        Triple(
            "Blessed Virgin Mary, Mother of the Church",
            dates.easter.plusDays(50),
            defaultDetail,
        )
    items +=
        Triple(
            "The Immaculate Heart of the Blessed Virgin Mary",
            dates.easter.plusDays(69),
            defaultDetail,
        )

    return items.distinctBy { "${it.second}|${it.first}" }.sortedBy { it.second }
}

private data class MemorialCatalogEntry(
    val title: String,
    val month: Int,
    val day: Int,
)

private fun memorialCatalogEntries(): List<MemorialCatalogEntry> =
    listOf(
        MemorialCatalogEntry("Saints Basil the Great and Gregory Nazianzen", 1, 2),
        MemorialCatalogEntry("Saint John Neumann, Bishop", 1, 5),
        MemorialCatalogEntry("Saint Anthony, Abbot", 1, 17),
        MemorialCatalogEntry("Saint Agnes, Virgin and Martyr", 1, 21),
        MemorialCatalogEntry("Saint Thomas Aquinas, Priest and Doctor", 1, 28),
        MemorialCatalogEntry("Saint Agatha, Virgin and Martyr", 2, 5),
        MemorialCatalogEntry("Saint Scholastica, Virgin", 2, 10),
        MemorialCatalogEntry("Saint Polycarp, Bishop and Martyr", 2, 23),
        MemorialCatalogEntry("Saints Perpetua and Felicity, Martyrs", 3, 7),
        MemorialCatalogEntry("Saint Patrick, Bishop", 3, 17),
        MemorialCatalogEntry("Saint Catherine of Siena, Virgin and Doctor", 4, 29),
        MemorialCatalogEntry("Saint Athanasius, Bishop and Doctor", 5, 2),
        MemorialCatalogEntry("Saint Philip Neri, Priest", 5, 26),
        MemorialCatalogEntry("Saint Justin, Martyr", 6, 1),
        MemorialCatalogEntry("Saint Boniface, Bishop and Martyr", 6, 5),
        MemorialCatalogEntry("Saint Barnabas, Apostle", 6, 11),
        MemorialCatalogEntry("Saint Anthony of Padua, Priest and Doctor", 6, 13),
        MemorialCatalogEntry("Saint Aloysius Gonzaga, Religious", 6, 21),
        MemorialCatalogEntry("Saint Benedict, Abbot", 7, 11),
        MemorialCatalogEntry("Saint Kateri Tekakwitha, Virgin", 7, 14),
        MemorialCatalogEntry("Saint Bonaventure, Bishop and Doctor", 7, 15),
        MemorialCatalogEntry("Saints Joachim and Anne", 7, 26),
        MemorialCatalogEntry("Saint Ignatius of Loyola, Priest", 7, 31),
        MemorialCatalogEntry("Saint Alphonsus Liguori, Bishop and Doctor", 8, 1),
        MemorialCatalogEntry("Saint John Vianney, Priest", 8, 4),
        MemorialCatalogEntry("Saint Dominic, Priest", 8, 8),
        MemorialCatalogEntry("Saint Clare, Virgin", 8, 11),
        MemorialCatalogEntry("Saint Maximilian Kolbe, Priest and Martyr", 8, 14),
        MemorialCatalogEntry("Saint Augustine, Bishop and Doctor", 8, 28),
        MemorialCatalogEntry("Saint Gregory the Great, Pope and Doctor", 9, 3),
        MemorialCatalogEntry("Saint Peter Claver, Priest", 9, 9),
        MemorialCatalogEntry("Our Lady of Sorrows", 9, 15),
        MemorialCatalogEntry("Saint Vincent de Paul, Priest", 9, 27),
        MemorialCatalogEntry("Saint Jerome, Priest and Doctor", 9, 30),
        MemorialCatalogEntry("Saint Therese of the Child Jesus, Virgin and Doctor", 10, 1),
        MemorialCatalogEntry("The Holy Guardian Angels", 10, 2),
        MemorialCatalogEntry("Our Lady of the Rosary", 10, 7),
        MemorialCatalogEntry("Saint Teresa of Jesus, Virgin and Doctor", 10, 15),
        MemorialCatalogEntry("Saint John Henry Newman, Priest", 10, 9),
        MemorialCatalogEntry("Saint Charles Borromeo, Bishop", 11, 4),
        MemorialCatalogEntry("Saint Frances Xavier Cabrini, Virgin", 11, 13),
        MemorialCatalogEntry("Saint Rose Philippine Duchesne, Virgin", 11, 18),
        MemorialCatalogEntry("Saint Miguel Agustin Pro, Priest and Martyr", 11, 23),
        MemorialCatalogEntry("Saint Cecilia, Virgin and Martyr", 11, 22),
        MemorialCatalogEntry("Saint Francis Xavier, Priest", 12, 3),
        MemorialCatalogEntry("Saint Ambrose, Bishop and Doctor", 12, 7),
        MemorialCatalogEntry("Saint Lucy, Virgin and Martyr", 12, 13),
        MemorialCatalogEntry("Saint John of the Cross, Priest and Doctor", 12, 14),
    )

private fun holyDayObligation(
    title: String,
    date: LocalDate,
    settings: RuleSettings,
): ObservanceObligation {
    val ageStatus = liturgicalObligationAgeStatus(date, settings)
    val saturdayOrMonday =
        date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.MONDAY

    return when {
        settings.regionProfile != RegionProfile.US -> ObservanceObligation.OPTIONAL
        ageStatus == LiturgicalObligationAgeStatus.UNDER_SEVEN ->
            ObservanceObligation.NOT_APPLICABLE
        title in transferableHolyDays() && saturdayOrMonday ->
            ObservanceObligation.OPTIONAL
        title.contains("Immaculate Conception") && title.contains("Transferred") ->
            ObservanceObligation.OPTIONAL
        title == "Christmas" || title == "Ascension" -> ObservanceObligation.MANDATORY
        title.contains("Immaculate Conception") -> ObservanceObligation.MANDATORY
        title in transferableHolyDays() -> ObservanceObligation.MANDATORY
        else -> ObservanceObligation.OPTIONAL
    }
}

private fun transferableHolyDays(): Set<String> =
    setOf(
        "Mary, Mother of God",
        "Assumption of the Blessed Virgin Mary",
        "All Saints",
    )

private fun holyDayDetail(
    title: String,
    date: LocalDate,
    settings: RuleSettings,
): String =
    when (settings.regionProfile) {
        RegionProfile.CANADA ->
            "Listed for Canada planning context. This release does not claim " +
                "full conference-level holy day obligation parity."
        RegionProfile.OTHER ->
            "Listed for planning context. Holy day obligations vary by " +
                "episcopal conference and local law outside the U.S. profile."
        RegionProfile.US -> holyDayUsDetail(title, date)
    }

private fun holyDayUsDetail(
    title: String,
    date: LocalDate,
): String {
    val saturdayOrMonday =
        date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.MONDAY

    return when {
        title == "Christmas" -> "Holy Day of Obligation in the U.S."
        title.contains("Immaculate Conception") && title.contains("Transferred") ->
            "Transferred from Sunday, December 8. In U.S. usage, the Mass obligation does not transfer to Monday."
        saturdayOrMonday && title in transferableHolyDays() ->
            "In U.S. norms, obligation may be abrogated this year because this holy day falls on Saturday or Monday."
        title == "Ascension" ->
            "Observed on Thursday or transferred to Sunday by province; obligation depends on local observance rules."
        else ->
            "Holy Day of Obligation in the U.S., subject to local episcopal conference directives."
    }
}

private fun defaultRationale(
    title: String,
    kind: ObservanceKind,
    obligation: ObservanceObligation,
    settings: RuleSettings,
): String =
    when (kind) {
        ObservanceKind.FAST_AND_ABSTINENCE ->
            rationaleForFastDay(title, obligation)
        ObservanceKind.ABSTINENCE ->
            rationaleForAbstinence(title)
        ObservanceKind.FRIDAY_PENANCE ->
            rationaleForFridayPenance(settings)
        ObservanceKind.HOLY_DAY ->
            rationaleForHolyDay(settings)
        ObservanceKind.FEAST_DAY ->
            "Celebrate this feast day; it is not a fasting obligation."
        ObservanceKind.MEMORIAL_DAY ->
            "Celebrate this memorial day; it is not a fasting obligation."
        ObservanceKind.OPTIONAL_EMBER ->
            "Ember days are optional in this mode and offered as devotional practice."
    }

private fun rationaleForFastDay(
    title: String,
    obligation: ObservanceObligation,
): String =
    if (obligation == ObservanceObligation.MANDATORY) {
        "$title is a universal fast/abstinence day for the Latin Church in this profile."
    } else {
        "$title is listed, but your profile indicates the obligation does not strictly bind."
    }

private fun rationaleForAbstinence(title: String): String =
    if (title == "Ash Wednesday" || title == "Good Friday") {
        "$title requires abstinence for those bound by age and health norms."
    } else {
        "Fridays in Lent are days of abstinence for those bound by age and health norms."
    }

private fun rationaleForFridayPenance(settings: RuleSettings): String =
    when (settings.regionProfile) {
        RegionProfile.US ->
            "Outside Lent Friday penance follows your selected U.S. profile mode."
        RegionProfile.CANADA ->
            "Outside Lent Friday penance follows CCCB guidance: Friday " +
                "remains penitential, with abstinence or another act of " +
                "charity or piety."
        RegionProfile.OTHER ->
            "Outside Lent Friday penance depends on local episcopal law and pastoral guidance."
    }

private fun rationaleForHolyDay(settings: RuleSettings): String =
    when (settings.regionProfile) {
        RegionProfile.US ->
            "Holy day obligation may vary by universal, national, and local norms."
        RegionProfile.CANADA ->
            "Holy day listing is shown for planning context. Canada support " +
                "is partial, so local obligation should be confirmed when it " +
                "matters."
        RegionProfile.OTHER ->
            "Holy day listing is informational outside the U.S. profile unless local law is known."
    }

private fun defaultCitations(
    title: String,
    kind: ObservanceKind,
    settings: RuleSettings,
): List<RuleCitation> =
    when (kind) {
        ObservanceKind.FAST_AND_ABSTINENCE,
        ObservanceKind.ABSTINENCE,
        -> fastingCitations(settings)
        ObservanceKind.FRIDAY_PENANCE ->
            fridayPenanceCitations(settings)
        ObservanceKind.HOLY_DAY ->
            holyDayCitations(title, settings)
        ObservanceKind.FEAST_DAY,
        ObservanceKind.MEMORIAL_DAY,
        ->
            listOf(
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Liturgical Calendar",
                    "Devotional observance",
                ),
            )
        ObservanceKind.OPTIONAL_EMBER ->
            listOf(
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Traditional Ember Practice",
                    "Optional in U.S. usage",
                ),
            )
    }

private fun fastingCitations(settings: RuleSettings): List<RuleCitation> =
    buildList {
        add(
            RuleCitation(
                RuleAuthority.UNIVERSAL_LAW,
                "Code of Canon Law",
                "Can. 1249-1253",
            ),
        )
        when (settings.regionProfile) {
            RegionProfile.US ->
                add(
                    RuleCitation(
                        RuleAuthority.USCCB,
                        "Pastoral Statement on Penance and Abstinence",
                        "USCCB 1966",
                    ),
                )
            RegionProfile.CANADA ->
                add(
                    RuleCitation(
                        RuleAuthority.CCCB,
                        "Keeping Friday",
                        "CCCB Friday guidance",
                    ),
                )
            RegionProfile.OTHER ->
                add(
                    RuleCitation(
                        RuleAuthority.PASTORAL,
                        "Local Catholic Guidance",
                        "Consult local conference norms",
                    ),
                )
        }
    }

private fun fridayPenanceCitations(settings: RuleSettings): List<RuleCitation> =
    when (settings.regionProfile) {
        RegionProfile.US ->
            listOf(
                RuleCitation(
                    RuleAuthority.USCCB,
                    "Penance and Abstinence Guidance",
                    "USCCB Norms",
                ),
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Pastoral Direction",
                    "Consult pastor for substitutions",
                ),
            )
        RegionProfile.CANADA ->
            listOf(
                RuleCitation(
                    RuleAuthority.CCCB,
                    "Keeping Friday",
                    "Friday remains penitential",
                ),
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Pastoral Direction",
                    "Choose abstinence or another penitential work",
                ),
            )
        RegionProfile.OTHER ->
            listOf(
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Local Conference Guidance",
                    "Friday practice varies",
                ),
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Pastoral Direction",
                    "Consult local Church authority",
                ),
            )
    }

private fun holyDayCitations(
    title: String,
    settings: RuleSettings,
): List<RuleCitation> =
    buildList {
        add(
            RuleCitation(
                RuleAuthority.UNIVERSAL_LAW,
                "Code of Canon Law",
                "Can. 1246-1248",
            ),
        )
        addAll(holyDayRegionalCitations(settings))
        if (settings.regionProfile == RegionProfile.US && requiresParticularLawNote(title)) {
            add(
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Particular Law",
                    "Province dependent",
                ),
            )
        }
    }

private fun holyDayRegionalCitations(settings: RuleSettings): List<RuleCitation> =
    when (settings.regionProfile) {
        RegionProfile.US ->
            listOf(
                RuleCitation(
                    RuleAuthority.USCCB,
                    "U.S. Holy Days",
                    "USCCB Liturgical Norms",
                ),
            )
        RegionProfile.CANADA ->
            listOf(
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Canada Support Scope",
                    "Holy day handling is partial",
                ),
            )
        RegionProfile.OTHER ->
            listOf(
                RuleCitation(
                    RuleAuthority.PASTORAL,
                    "Conference and Local Law",
                    "Review local obligation law",
                ),
            )
    }

private fun requiresParticularLawNote(title: String): Boolean {
    return title.contains("Ascension") || title.contains("Immaculate")
}

private fun fastDetail(settings: RuleSettings): String =
    if (settings.hasMedicalDispensation) {
        "Dispensation enabled in your profile. Follow your pastor and medical guidance."
    } else {
        "For ages 18-59: one full meal and two smaller meals (not equal to a second full meal)."
    }

private fun fridayPenanceDetail(
    mode: FridayOutsideLentMode,
    settings: RuleSettings,
): String =
    when (settings.regionProfile) {
        RegionProfile.CANADA -> canadaFridayPenanceDetail(mode)
        RegionProfile.OTHER ->
            "Friday remains penitential outside Lent, but the exact practice " +
                "depends on local Church law."
        RegionProfile.US -> usFridayPenanceDetail(mode)
    }

private fun canadaFridayPenanceDetail(mode: FridayOutsideLentMode): String =
    when (mode) {
        FridayOutsideLentMode.ABSTAIN_FROM_MEAT ->
            "In Canada, Friday remains penitential throughout the year. You " +
                "chose abstinence from meat for your Friday practice."
        FridayOutsideLentMode.SUBSTITUTE_PENANCE ->
            "In Canada, Friday remains penitential throughout the year. You " +
                "chose another act of charity or piety for your Friday " +
                "practice."
    }

private fun usFridayPenanceDetail(mode: FridayOutsideLentMode): String =
    when (mode) {
        FridayOutsideLentMode.ABSTAIN_FROM_MEAT ->
            "Outside Lent: abstain from meat as your Friday penance."
        FridayOutsideLentMode.SUBSTITUTE_PENANCE ->
            "Outside Lent: choose a penitential act (e.g., extra prayer, charity, or another sacrifice)."
    }

private fun ageDispensationDetail(settings: RuleSettings): String =
    if (settings.hasMedicalDispensation) {
        "Not required due to medical dispensation setting."
    } else {
        "Not required for your age eligibility toggle settings."
    }

private fun isFastRequired(settings: RuleSettings): Boolean {
    return !settings.hasMedicalDispensation && settings.isAge18OrOlderForFasting
}

private fun isAbstinenceRequired(settings: RuleSettings): Boolean {
    return !settings.hasMedicalDispensation && settings.isAge14OrOlderForAbstinence
}

private enum class LiturgicalObligationAgeStatus {
    UNDER_SEVEN,
    SEVEN_OR_OLDER,
    UNKNOWN,
}

private fun liturgicalObligationAgeStatus(
    date: LocalDate,
    settings: RuleSettings,
): LiturgicalObligationAgeStatus {
    val hasBirthDate = settings.birthYear >= 1900
    return if (hasBirthDate) {
        val age =
            DateSupport.ageOn(
                date,
                settings.birthYear,
                settings.birthMonth,
                settings.birthDay,
            )
        if (age >= 7) {
            LiturgicalObligationAgeStatus.SEVEN_OR_OLDER
        } else {
            LiturgicalObligationAgeStatus.UNDER_SEVEN
        }
    } else if (settings.isAge14OrOlderForAbstinence || settings.isAge18OrOlderForFasting) {
        LiturgicalObligationAgeStatus.SEVEN_OR_OLDER
    } else {
        LiturgicalObligationAgeStatus.UNKNOWN
    }
}

private fun lentFridays(
    start: LocalDate,
    end: LocalDate,
): List<LocalDate> {
    val days = mutableListOf<LocalDate>()
    var cursor = start
    while (!cursor.isAfter(end)) {
        if (cursor.dayOfWeek == DayOfWeek.FRIDAY) {
            days += cursor
        }
        cursor = cursor.plusDays(1)
    }
    return days
}

private fun fridaysOutsideLent(
    year: Int,
    lentStart: LocalDate,
    lentEnd: LocalDate,
): List<LocalDate> {
    val days = mutableListOf<LocalDate>()
    var cursor = LocalDate.of(year, 1, 1)
    val end = LocalDate.of(year, 12, 31)

    while (!cursor.isAfter(end)) {
        val outsideLent = cursor.isBefore(lentStart) || cursor.isAfter(lentEnd)
        if (cursor.dayOfWeek == DayOfWeek.FRIDAY && outsideLent) {
            days += cursor
        }
        cursor = cursor.plusDays(1)
    }
    return days
}

private fun emberDays(
    year: Int,
    ashWednesday: LocalDate,
    pentecost: LocalDate,
): List<LocalDate> {
    val set = linkedSetOf<LocalDate>()
    val firstSundayOfLent =
        DateSupport.nextWeekdayOnOrAfter(ashWednesday.plusDays(1), DayOfWeek.SUNDAY)

    listOf(3L, 5L, 6L).forEach { offset ->
        set += firstSundayOfLent.plusDays(offset)
        set += pentecost.plusDays(offset)
    }

    addEmberWeek(set, LocalDate.of(year, 9, 14))
    addEmberWeek(set, LocalDate.of(year, 12, 13))

    return set.toList().sorted()
}

private fun addEmberWeek(
    set: MutableSet<LocalDate>,
    anchor: LocalDate,
) {
    set += DateSupport.nextWeekdayAfter(anchor, DayOfWeek.WEDNESDAY)
    set += DateSupport.nextWeekdayAfter(anchor, DayOfWeek.FRIDAY)
    set += DateSupport.nextWeekdayAfter(anchor, DayOfWeek.SATURDAY)
}
