@file:Suppress("TooManyFunctions")

package com.kevpierce.catholicfasting.core.rules

import com.kevpierce.catholicfasting.core.model.CalendarWindow
import com.kevpierce.catholicfasting.core.model.CatholicFastingQuote
import com.kevpierce.catholicfasting.core.model.CompletionStatus
import com.kevpierce.catholicfasting.core.model.ContentLocale
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceFilter
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.ObservanceSortOrder
import com.kevpierce.catholicfasting.core.model.SacredImageryItem
import com.kevpierce.catholicfasting.core.model.SeasonalContentPack
import com.kevpierce.catholicfasting.core.model.SeasonalContentQuote
import java.time.LocalDate

object ObservanceQueryEngine {
    fun filter(
        observances: List<Observance>,
        query: String,
        filter: ObservanceFilter,
        window: CalendarWindow,
        sortOrder: ObservanceSortOrder,
        statusesById: Map<String, CompletionStatus>,
        now: LocalDate = LocalDate.now(),
    ): List<Observance> {
        val normalizedQuery = query.trim()
        val endOfNext30Days = now.plusDays(30)

        return observances
            .filter { matchesFilter(it, filter, statusesById) }
            .filter { matchesWindow(it, window, now, endOfNext30Days) }
            .filter { matchesQuery(it, normalizedQuery) }
            .sortedWith(sortComparator(sortOrder))
    }
}

object SeasonalContentPackCatalog {
    fun pack(
        season: LiturgicalSeason,
        locale: ContentLocale,
    ): SeasonalContentPack {
        val packs = if (locale == ContentLocale.SPANISH) spanishPacks else englishPacks
        return packs[season] ?: packs.getValue(LiturgicalSeason.ORDINARY)
    }
}

object SacredImageryCatalog {
    val fastingGallery: List<SacredImageryItem> =
        listOf(
            imagery("chi-rho", "SacredChiRho", "Chi-Rho", "Offer each fast in Christ."),
            imagery("monstrance", "SacredMonstrance", "Monstrance", "Let prayer anchor discipline."),
            imagery("sacred-heart", "SacredSacredHeart", "Sacred Heart", "Unite fasting to charity."),
            imagery("rosary-cross", "SacredRosaryCross", "Rosary Cross", "Pray while you abstain."),
            imagery(
                "cathedral-light",
                "SacredCathedralLight",
                "Cathedral Light",
                "Remember the liturgy while you fast.",
            ),
            imagery("ash-wednesday", "SacredAshWednesday", "Ash Cross", "Repentance remains the core of fasting."),
            imagery(
                "desert-pilgrimage",
                "SacredDesertPilgrimage",
                "Desert Pilgrimage",
                "Keep your sacrifice steady over time.",
            ),
            imagery(
                "scripture-candle",
                "SacredScriptureCandle",
                "Scripture Candle",
                "Anchor discipline in prayer and the Word.",
            ),
            imagery("palm-sunday", "SacredPalmSunday", "Palm Branch", "Prepare your heart for Holy Week."),
            imagery(
                "chalice-vine",
                "SacredChaliceVine",
                "Chalice and Vine",
                "Offer fasting in a Eucharistic spirit.",
            ),
            imagery("pantocrator", "HeroSacred", "Christ Pantocrator", "Keep your fasting centered on Christ."),
            imagery(
                "basilica",
                "GuidanceSacred",
                "St. Peter's Basilica",
                "Stay rooted in the life and teaching of the Church.",
            ),
            imagery(
                "jerusalem-cross",
                "SacredJerusalemCross",
                "Jerusalem Cross",
                "Let your sacrifice witness to the Gospel.",
            ),
            imagery(
                "marian-monogram",
                "SacredMarianMonogram",
                "Marian Monogram",
                "Fast with humility and trust in Mary's example.",
            ),
            imagery(
                "concept-chi-rho",
                "SacredConceptChiRho",
                "Chi-Rho Crest",
                "Keep each offering centered on Christ.",
            ),
            imagery(
                "concept-rosary",
                "SacredConceptRosary",
                "Rosary Emblem",
                "Unite prayer and discipline day by day.",
            ),
            imagery("concept-heart", "SacredConceptHeart", "Heart of Mercy", "Let fasting lead to deeper charity."),
            imagery(
                "monstrance-adoration-night",
                "SacredMonstrance",
                "Adoration Night",
                "Anchor discipline in Eucharistic worship.",
            ),
            imagery(
                "scripture-candle-watch",
                "SacredScriptureCandle",
                "Watchful Prayer",
                "Keep vigil in prayer while you fast.",
            ),
            imagery(
                "cathedral-light-vestibule",
                "SacredCathedralLight",
                "Church Light",
                "Bring fasting into the rhythm of the liturgy.",
            ),
            imagery(
                "palm-branch-procession",
                "SacredPalmSunday",
                "Procession",
                "Walk with Christ through discipline and mercy.",
            ),
            imagery(
                "jerusalem-cross-pilgrim",
                "SacredJerusalemCross",
                "Pilgrim Cross",
                "Offer each sacrifice for the Church and world.",
            ),
            imagery(
                "marian-monogram-fiat",
                "SacredMarianMonogram",
                "Marian Fiat",
                "Practice faithful discipline with humility.",
            ),
            imagery(
                "chi-rho-victory",
                "SacredConceptChiRho",
                "Christ Our Victory",
                "Keep every fast ordered to Christ.",
            ),
            imagery(
                "rosary-emblem-perseverance",
                "SacredConceptRosary",
                "Rosary Perseverance",
                "Persevere in small sacrifices with prayer.",
            ),
        )
}

object SeasonalContentSupport {
    fun dailyFormationLine(
        pack: SeasonalContentPack,
        date: LocalDate = LocalDate.now(),
    ): String {
        if (pack.formationLines.isEmpty()) {
            return "Offer today's discipline with prayer and charity."
        }
        val index = (date.dayOfYear - 1) % pack.formationLines.size
        return pack.formationLines[index]
    }

    fun dailyQuote(
        season: LiturgicalSeason,
        pack: SeasonalContentPack,
        date: LocalDate = LocalDate.now(),
    ): CatholicFastingQuote {
        if (pack.quotes.isEmpty()) {
            return CatholicFastingQuote(
                id = "fallback-seasonal-quote",
                text = "Fast with fidelity, pray with humility, and give with charity.",
                author = "Catholic Fasting",
                source = "In-app formation",
                tradition = "Pastoral",
            )
        }
        val day = date.dayOfYear
        val quote = pack.quotes[(day - 1) % pack.quotes.size]
        return CatholicFastingQuote(
            id = "seasonal-${season.name.lowercase()}-$day",
            text = quote.text,
            author = quote.author,
            source = quote.source,
            tradition = quote.tradition,
        )
    }
}

private fun matchesFilter(
    observance: Observance,
    filter: ObservanceFilter,
    statusesById: Map<String, CompletionStatus>,
): Boolean =
    when (filter) {
        ObservanceFilter.ALL -> true
        ObservanceFilter.REQUIRED_ONLY -> observance.obligation == ObservanceObligation.MANDATORY
        ObservanceFilter.TRACKED_ONLY ->
            (statusesById[observance.id] ?: CompletionStatus.NOT_STARTED) != CompletionStatus.NOT_STARTED
    }

private fun matchesWindow(
    observance: Observance,
    window: CalendarWindow,
    startOfToday: LocalDate,
    endOfNext30Days: LocalDate,
): Boolean {
    val day = LocalDate.parse(observance.date)
    return when (window) {
        CalendarWindow.ALL_YEAR -> true
        CalendarWindow.THIS_MONTH -> day.month == startOfToday.month && day.year == startOfToday.year
        CalendarWindow.NEXT_30_DAYS -> day >= startOfToday && day <= endOfNext30Days
    }
}

private fun matchesQuery(
    observance: Observance,
    query: String,
): Boolean {
    if (query.isEmpty()) {
        return true
    }

    return observance.title.contains(query, ignoreCase = true) ||
        observance.kind.label.contains(query, ignoreCase = true) ||
        observance.obligation.label.contains(query, ignoreCase = true) ||
        (observance.detail?.contains(query, ignoreCase = true) ?: false)
}

private fun sortComparator(order: ObservanceSortOrder): Comparator<Observance> =
    when (order) {
        ObservanceSortOrder.CHRONOLOGICAL ->
            compareBy<Observance> { it.date }.thenBy { it.title }
        ObservanceSortOrder.REQUIRED_FIRST ->
            compareBy<Observance> { obligationRank(it.obligation) }
                .thenBy { it.date }
                .thenBy { it.title }
    }

private fun obligationRank(obligation: ObservanceObligation): Int =
    when (obligation) {
        ObservanceObligation.MANDATORY -> 0
        ObservanceObligation.OPTIONAL -> 1
        ObservanceObligation.NOT_APPLICABLE -> 2
    }

private fun imagery(
    id: String,
    assetName: String,
    title: String,
    subtitle: String,
): SacredImageryItem =
    SacredImageryItem(
        id = id,
        assetName = assetName,
        title = title,
        subtitle = subtitle,
    )

private val englishPacks: Map<LiturgicalSeason, SeasonalContentPack> =
    mapOf(
        LiturgicalSeason.LENT to
            pack(
                season = LiturgicalSeason.LENT,
                locale = ContentLocale.ENGLISH,
                heroAssetNames =
                    listOf(
                        "SacredAshWednesday",
                        "SacredMonstrance",
                        "SacredPalmSunday",
                        "SacredDesertPilgrimage",
                    ),
                campaignTitle = "Lenten Discipline",
                campaignSubtitle = "Pray, fast, and give alms with consistency.",
                formationLines =
                    listOf(
                        "Keep required days visible and plan Friday penance before the week starts.",
                        "Pair each fast with one concrete act of mercy.",
                        "Use hunger as a cue for prayer, not frustration.",
                    ),
                quotes =
                    listOf(
                        quote(
                            "Prayer joined to sacrifice constitutes the most powerful force in human history.",
                            "Pope St. John Paul II",
                            "Address on Prayer and Sacrifice",
                            "Pope",
                        ),
                        quote(
                            "Fasting is the soul of prayer, and mercy is the lifeblood of fasting.",
                            "St. Peter Chrysologus",
                            "Sermon 43",
                            "Church Father",
                        ),
                    ),
            ),
        LiturgicalSeason.ADVENT to
            pack(
                season = LiturgicalSeason.ADVENT,
                locale = ContentLocale.ENGLISH,
                heroAssetNames =
                    listOf(
                        "SacredScriptureCandle",
                        "SacredMarianMonogram",
                        "SacredCathedralLight",
                    ),
                campaignTitle = "Advent Watchfulness",
                campaignSubtitle = "Practice hopeful discipline while awaiting Christ.",
                formationLines =
                    listOf(
                        "Keep disciplines modest and sustainable.",
                        "Use fasting to create room for prayer and silence.",
                    ),
                quotes =
                    listOf(
                        quote(
                            "Denying material food helps us listen to Christ and be nourished by his saving word.",
                            "Pope Benedict XVI",
                            "Lenten Message",
                            "Pope",
                        ),
                    ),
            ),
        LiturgicalSeason.CHRISTMAS to
            pack(
                season = LiturgicalSeason.CHRISTMAS,
                locale = ContentLocale.ENGLISH,
                heroAssetNames =
                    listOf(
                        "SacredChaliceVine",
                        "SacredCathedralLight",
                        "HeroSacred",
                    ),
                campaignTitle = "Christmas Sobriety and Joy",
                campaignSubtitle = "Celebrate faithfully while keeping Friday discipline.",
                formationLines =
                    listOf(
                        "Practice gratitude at meals.",
                        "Keep penitential practice with gentleness and charity.",
                    ),
                quotes =
                    listOf(
                        quote(
                            "Penance without love is heavy, but penance with love becomes joy.",
                            "St. Bernard of Clairvaux",
                            "Sermons",
                            "Doctor of the Church",
                        ),
                    ),
            ),
        LiturgicalSeason.EASTER to
            pack(
                season = LiturgicalSeason.EASTER,
                locale = ContentLocale.ENGLISH,
                heroAssetNames =
                    listOf(
                        "SacredChaliceVine",
                        "SacredJerusalemCross",
                        "HeroSacred",
                    ),
                campaignTitle = "Easter Fidelity",
                campaignSubtitle = "Carry Lenten discipline into ordinary life.",
                formationLines =
                    listOf(
                        "Maintain Friday penance intentionally.",
                        "Use gratitude and mercy as your primary acts.",
                    ),
                quotes =
                    listOf(
                        quote(
                            "The abstinence of one should become the refreshment of another.",
                            "St. Gregory the Great",
                            "Homilies on the Gospels",
                            "Pope & Doctor",
                        ),
                    ),
            ),
        LiturgicalSeason.ORDINARY to
            pack(
                season = LiturgicalSeason.ORDINARY,
                locale = ContentLocale.ENGLISH,
                heroAssetNames =
                    listOf(
                        "SacredChiRho",
                        "SacredRosaryCross",
                        "SacredConceptHeart",
                        "HeroSacred",
                    ),
                campaignTitle = "Ordinary Time Consistency",
                campaignSubtitle = "Small faithful habits form long-term discipline.",
                formationLines =
                    listOf(
                        "Plan your weekly fast day in advance.",
                        "Review completed and missed days each week.",
                    ),
                quotes =
                    listOf(
                        quote(
                            "A faithful small sacrifice is better than a dramatic one you cannot sustain.",
                            "Catholic Fasting",
                            "In-app formation",
                            "Pastoral",
                        ),
                    ),
            ),
    )

private val spanishPacks: Map<LiturgicalSeason, SeasonalContentPack> =
    mapOf(
        LiturgicalSeason.LENT to
            pack(
                season = LiturgicalSeason.LENT,
                locale = ContentLocale.SPANISH,
                heroAssetNames =
                    listOf(
                        "SacredAshWednesday",
                        "SacredMonstrance",
                        "SacredPalmSunday",
                    ),
                campaignTitle = "Disciplina Cuaresmal",
                campaignSubtitle = "Orar, ayunar y dar limosna con constancia.",
                formationLines =
                    listOf(
                        "Planea los dias obligatorios con anticipacion.",
                        "Une cada ayuno con una obra concreta de misericordia.",
                    ),
                quotes =
                    listOf(
                        quote(
                            "El ayuno es el alma de la oracion y la misericordia es su vida.",
                            "San Pedro Crisologo",
                            "Sermon 43",
                            "Padre de la Iglesia",
                        ),
                    ),
            ),
        LiturgicalSeason.ORDINARY to
            pack(
                season = LiturgicalSeason.ORDINARY,
                locale = ContentLocale.SPANISH,
                heroAssetNames =
                    listOf(
                        "SacredChiRho",
                        "SacredRosaryCross",
                        "HeroSacred",
                    ),
                campaignTitle = "Constancia en Tiempo Ordinario",
                campaignSubtitle = "La fidelidad diaria forma el corazon.",
                formationLines =
                    listOf(
                        "Manten una disciplina semanal realista.",
                        "Revisa cada semana tus avances y tus omisiones.",
                    ),
                quotes =
                    listOf(
                        quote(
                            "La penitencia con amor se vuelve alegria.",
                            "San Bernardo",
                            "Sermones",
                            "Doctor de la Iglesia",
                        ),
                    ),
            ),
    )

private fun pack(
    season: LiturgicalSeason,
    locale: ContentLocale,
    heroAssetNames: List<String>,
    campaignTitle: String,
    campaignSubtitle: String,
    formationLines: List<String>,
    quotes: List<SeasonalContentQuote>,
): SeasonalContentPack =
    SeasonalContentPack(
        season = season,
        locale = locale,
        heroAssetNames = heroAssetNames,
        campaignTitle = campaignTitle,
        campaignSubtitle = campaignSubtitle,
        formationLines = formationLines,
        quotes = quotes,
    )

private fun quote(
    text: String,
    author: String,
    source: String,
    tradition: String,
): SeasonalContentQuote =
    SeasonalContentQuote(
        text = text,
        author = author,
        source = source,
        tradition = tradition,
    )
