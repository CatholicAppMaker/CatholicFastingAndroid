package com.kevpierce.catholicfasting.core.rules

import com.kevpierce.catholicfasting.core.model.FoodGuidanceExample
import com.kevpierce.catholicfasting.core.model.FoodGuidanceGroup
import com.kevpierce.catholicfasting.core.model.FoodGuidanceSnapshot
import com.kevpierce.catholicfasting.core.model.GuidanceScenario
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.ObservanceObligation
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.RuleSettings
import java.time.LocalDate

object FoodGuidanceEngine {
    fun snapshot(
        scenario: GuidanceScenario,
        settings: RuleSettings,
    ): FoodGuidanceSnapshot =
        FoodGuidanceSnapshot(
            summaryLine = summaryLineFor(scenario),
            whatCountsAsMeat = whatCountsAsMeatGroup(),
            generallyPermitted = generallyPermittedGroup(),
            mealPattern = mealPatternGroup(),
            extraGuidance = extraGuidanceGroup(),
            stricterTraditionalPractice = stricterTraditionalPracticeLines(),
            ifUnsure = ifUnsureLines(),
            caveatLine = caveatLineFor(scenario),
            sourceLine = sourceLineFor(settings.regionProfile),
        )

    fun recommendations(
        scenario: GuidanceScenario,
        settings: RuleSettings,
    ): List<String> {
        val snapshot = snapshot(scenario, settings)
        val medicallyDispensed =
            settings.hasMedicalDispensation || scenario == GuidanceScenario.MEDICAL_RECOVERY

        if (medicallyDispensed) {
            return listOf(
                snapshot.summaryLine,
                "Your health comes first. A medical or pastoral dispensation likely applies.",
                "Choose a substitute penance if possible (prayer, charity, Scripture, or another sacrifice).",
                "Resume normal fasting only when it is prudent and safe.",
            )
        }

        return listOf(
            snapshot.summaryLine,
            "Abstinence: avoid meat from land animals and birds, including " +
                "chicken, beef, pork, turkey, and lamb.",
            "Generally permitted on abstinence days: fish, shellfish, eggs, " +
                "dairy, grains, fruit, and vegetables.",
            "Fasting: one full meal and up to two smaller meals that together " +
                "are less than a second full meal.",
            "Extra guidance: broth, gravies, and animal-fat seasonings may be " +
                "technically permitted, but many Catholics avoid them in " +
                "stricter practice.",
            snapshot.caveatLine,
        )
    }
}

object RequiredDayReminderPlanner {
    const val PENDING_NOTIFICATION_LIMIT: Int = 64
    const val RESERVED_SLOTS_FOR_NON_REQUIRED: Int = 14
    const val ABSOLUTE_REQUIRED_REMINDER_CAP: Int =
        PENDING_NOTIFICATION_LIMIT - RESERVED_SLOTS_FOR_NON_REQUIRED

    fun maximumRequiredReminders(existingNonRequiredPendingCount: Int): Int {
        val nonRequiredCount = existingNonRequiredPendingCount.coerceAtLeast(0)
        val remainingQueueCapacity =
            (PENDING_NOTIFICATION_LIMIT - nonRequiredCount).coerceAtLeast(0)
        return minOf(ABSOLUTE_REQUIRED_REMINDER_CAP, remainingQueueCapacity)
    }

    fun additionalRequiredReminderSlots(
        existingRequiredPendingCount: Int,
        existingNonRequiredPendingCount: Int,
    ): Int {
        val requiredCount = existingRequiredPendingCount.coerceAtLeast(0)
        val maxRequired = maximumRequiredReminders(existingNonRequiredPendingCount)
        return (maxRequired - requiredCount).coerceAtLeast(0)
    }

    fun upcomingMandatoryObservances(
        observances: List<Observance>,
        now: LocalDate = LocalDate.now(),
        limit: Int,
    ): List<Observance> {
        if (limit <= 0) {
            return emptyList()
        }

        val sortedCandidates =
            observances
                .filter { observance ->
                    observance.obligation == ObservanceObligation.MANDATORY &&
                        LocalDate.parse(observance.date) >= now
                }
                .sortedWith(compareBy<Observance> { it.date }.thenBy { it.id })

        val seenIds = mutableSetOf<String>()
        val planned = mutableListOf<Observance>()

        sortedCandidates.forEach { observance ->
            if (seenIds.add(observance.id)) {
                planned += observance
            }
        }

        return planned.take(limit)
    }
}

private fun sourceLineFor(regionProfile: RegionProfile): String =
    when (regionProfile) {
        RegionProfile.US ->
            "Sources: USCCB fast/abstinence guidance and universal law."
        RegionProfile.CANADA ->
            "Sources: CCCB Friday guidance, universal law, and U.S.-first abstinence examples."
        RegionProfile.OTHER ->
            "Sources: universal law and local pastoral guidance."
    }

private fun whatCountsAsMeatGroup(): FoodGuidanceGroup =
    FoodGuidanceGroup(
        title = "What counts as meat",
        summary = "Abstinence from meat includes the flesh of land animals and birds.",
        items =
            listOf(
                FoodGuidanceExample(
                    title = "Generally avoid",
                    detail =
                        "Beef, pork, chicken, turkey, lamb, bacon, ham, and " +
                            "similar land-animal or bird meats.",
                ),
                FoodGuidanceExample(
                    title = "Chicken counts as meat",
                    detail = "Poultry is included in abstinence from meat.",
                ),
            ),
    )

private fun generallyPermittedGroup(): FoodGuidanceGroup =
    FoodGuidanceGroup(
        title = "Generally permitted",
        summary = "These are generally permitted on abstinence days.",
        items =
            listOf(
                FoodGuidanceExample(
                    title = "Fish and shellfish",
                    detail = "Fish, shellfish, and other seafood are generally permitted.",
                ),
                FoodGuidanceExample(
                    title = "Eggs and dairy",
                    detail =
                        "Eggs, milk, butter, cheese, and similar dairy products " +
                            "are not treated as meat.",
                ),
                FoodGuidanceExample(
                    title = "Plant foods",
                    detail =
                        "Grains, vegetables, legumes, fruit, breads, and oils " +
                            "are generally permitted.",
                ),
            ),
    )

private fun mealPatternGroup(): FoodGuidanceGroup =
    FoodGuidanceGroup(
        title = "Meal pattern on fasting days",
        summary = "Fasting is distinct from abstinence.",
        items =
            listOf(
                FoodGuidanceExample(
                    title = "Core fasting norm",
                    detail =
                        "One full meal and up to two smaller meals that " +
                            "together do not equal a second full meal.",
                ),
                FoodGuidanceExample(
                    title = "What to avoid",
                    detail =
                        "Eating in a way that effectively becomes a second " +
                            "full meal, even if spread out.",
                ),
            ),
    )

private fun extraGuidanceGroup(): FoodGuidanceGroup =
    FoodGuidanceGroup(
        title = "Extra guidance for common questions",
        summary = "These are the gray-area cases people actually ask about.",
        items =
            listOf(
                FoodGuidanceExample(
                    title = "Broths, gravies, and sauces",
                    detail =
                        "Meat broths, chicken broth, consomme, or gravies " +
                            "flavored with meat are often understood as " +
                            "technically not forbidden under the strict legal minimum.",
                ),
                FoodGuidanceExample(
                    title = "Animal-fat seasonings",
                    detail =
                        "Condiments or seasonings made from animal fat can " +
                            "fall into the same technically-not-forbidden category.",
                ),
                FoodGuidanceExample(
                    title = "Fish remains permitted",
                    detail =
                        "Fish and shellfish remain permitted, even though they " +
                            "are animal foods.",
                ),
                FoodGuidanceExample(
                    title = "Dairy is generally permitted",
                    detail =
                        "Butter, cheese, milk, and eggs are generally " +
                            "permitted and do not count as meat.",
                ),
            ),
    )

private fun stricterTraditionalPracticeLines(): List<String> =
    listOf(
        "Many Catholics and traditional moral theologians choose to avoid " +
            "meat broths, gravies, and animal-fat products as part of a " +
            "stricter penitential practice.",
        "If you want the simpler and more penitential option, avoid foods " +
            "that are strongly meat-derived even when they may not be strictly forbidden.",
    )

private fun ifUnsureLines(): List<String> =
    listOf(
        "Choose the simpler non-meat option.",
        "Consult your pastor if you need certainty in a disputed or local case.",
        "Follow medical guidance where health is involved.",
    )

private fun summaryLineFor(scenario: GuidanceScenario): String =
    when (scenario) {
        GuidanceScenario.MEDICAL_RECOVERY ->
            "Health and pastoral guidance come first. If medical recovery " +
                "is involved, fasting may not bind in the ordinary way."
        GuidanceScenario.HEAVY_LABOR ->
            "Keep the discipline, but adjust prudently if heavy labor would " +
                "make strict fasting unsafe."
        GuidanceScenario.TRAVEL ->
            "Travel can limit options. Choose the simplest penitential " +
                "option you can keep faithfully."
        GuidanceScenario.SOCIAL_MEAL ->
            "Keep charity and discretion at shared meals while still " +
                "honoring abstinence and fasting norms."
        GuidanceScenario.NORMAL_DAY ->
            "Use this section for the practical food questions Catholics " +
                "actually ask about fasting and abstinence."
    }

private fun caveatLineFor(scenario: GuidanceScenario): String =
    when (scenario) {
        GuidanceScenario.MEDICAL_RECOVERY ->
            "If health or recovery is involved, follow medical advice and " +
                "use substitute penance where appropriate."
        GuidanceScenario.HEAVY_LABOR ->
            "If your state in life makes the legal minimum unsafe, reduce " +
                "rigor and add another penitential act."
        GuidanceScenario.TRAVEL ->
            "When options are limited, the safer and simpler non-meat " +
                "option is usually best."
        GuidanceScenario.SOCIAL_MEAL ->
            "When hospitality creates ambiguity, choose the simpler " +
                "penitential option without turning the meal into a spectacle."
        GuidanceScenario.NORMAL_DAY ->
            "Examples here are practical guidance, not a replacement for " +
                "pastoral judgment."
    }
