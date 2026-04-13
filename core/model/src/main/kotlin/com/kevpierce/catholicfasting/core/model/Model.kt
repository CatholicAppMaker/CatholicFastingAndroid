package com.kevpierce.catholicfasting.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import kotlin.math.roundToInt

@Serializable
enum class RegionProfile(val label: String) {
    US("United States"),
    CANADA("Canada"),
    OTHER("Other"),
}

@Serializable
enum class CalendarMode(val label: String) {
    USCCB("USCCB (Ordinary Form)"),
    TRADITIONAL_1962("Traditional (1962-inspired)"),
}

@Serializable
enum class AscensionObservance(val label: String) {
    THURSDAY("Thursday (traditional)"),
    SUNDAY("Sunday (transferred)"),
}

@Serializable
enum class FridayOutsideLentMode(val label: String) {
    ABSTAIN_FROM_MEAT("Abstain from meat"),
    SUBSTITUTE_PENANCE("Another penitential act"),
}

@Serializable
data class RuleSettings(
    val birthYear: Int = 0,
    val birthMonth: Int = 0,
    val birthDay: Int = 0,
    val isAge14OrOlderForAbstinence: Boolean = true,
    val isAge18OrOlderForFasting: Boolean = true,
    val hasMedicalDispensation: Boolean = false,
    val ascensionObservance: AscensionObservance = AscensionObservance.SUNDAY,
    val fridayOutsideLentMode: FridayOutsideLentMode = FridayOutsideLentMode.SUBSTITUTE_PENANCE,
    val calendarMode: CalendarMode = CalendarMode.USCCB,
    val regionProfile: RegionProfile = RegionProfile.US,
) {
    val hasFullBirthDate: Boolean
        get() = birthYear > 0 && birthMonth in 1..12 && birthDay in 1..31
}

@Serializable
enum class GuidanceScenario(val label: String) {
    NORMAL_DAY("Normal Day"),
    HEAVY_LABOR("Heavy Labor"),
    TRAVEL("Travel"),
    SOCIAL_MEAL("Social Meal"),
    MEDICAL_RECOVERY("Medical Recovery"),
}

@Serializable
data class FoodGuidanceExample(
    val title: String,
    val detail: String,
)

@Serializable
data class FoodGuidanceGroup(
    val title: String,
    val summary: String,
    val items: List<FoodGuidanceExample>,
)

@Serializable
data class FoodGuidanceSnapshot(
    val summaryLine: String,
    val whatCountsAsMeat: FoodGuidanceGroup,
    val generallyPermitted: FoodGuidanceGroup,
    val mealPattern: FoodGuidanceGroup,
    val extraGuidance: FoodGuidanceGroup,
    val stricterTraditionalPractice: List<String>,
    val ifUnsure: List<String>,
    val caveatLine: String,
    val sourceLine: String,
)

@Serializable
enum class RuleAuthority(val label: String) {
    UNIVERSAL_LAW("Universal Law"),
    USCCB("USCCB"),
    CCCB("CCCB"),
    PASTORAL("Pastoral Guidance"),
}

@Serializable
data class RuleCitation(
    val authority: RuleAuthority,
    val title: String,
    val shortReference: String,
)

@Serializable
data class RuleBundleMetadata(
    val id: String,
    val displayName: String,
    val version: String,
    val effectiveDate: String,
    val reviewedDate: String,
)

@Serializable
data class RuleBundleChange(
    val id: String,
    val date: String,
    val title: String,
    val detail: String,
)

@Serializable
data class RuleBundleAudit(
    val source: String,
    val isVerified: Boolean,
    val warnings: List<String>,
)

@Serializable
enum class LiturgicalSeason(val label: String) {
    ADVENT("Advent"),
    CHRISTMAS("Christmas"),
    LENT("Lent"),
    EASTER("Easter"),
    ORDINARY("Ordinary Time"),
}

@Serializable
enum class ObservanceKind(val label: String) {
    FAST_AND_ABSTINENCE("Fast + Abstinence"),
    ABSTINENCE("Abstinence"),
    FRIDAY_PENANCE("Friday Penance"),
    HOLY_DAY("Holy Day"),
    FEAST_DAY("Feast Day"),
    MEMORIAL_DAY("Memorial"),
    OPTIONAL_EMBER("Optional Ember Day"),
}

@Serializable
enum class ObservanceObligation(val label: String) {
    MANDATORY("Required"),
    OPTIONAL("Optional"),
    NOT_APPLICABLE("Not Required"),
}

@Serializable
data class Observance(
    val id: String,
    val title: String,
    val date: String,
    val kind: ObservanceKind,
    val obligation: ObservanceObligation,
    val detail: String? = null,
    val rationale: String,
    val citations: List<RuleCitation>,
    val ruleVersion: String,
)

@Serializable
enum class CompletionStatus(val label: String, val countsTowardProgress: Boolean) {
    NOT_STARTED("Not Started", false),
    COMPLETED("Completed", true),
    SUBSTITUTED("Substituted", true),
    DISPENSED("Dispensed", true),
    MISSED("Missed", false),
}

@Serializable
data class IntermittentFastSession(
    val id: String,
    val startIso: String,
    val endIso: String,
    val targetHours: Int,
    val completedTarget: Boolean,
)

@Serializable
data class ActiveIntermittentFast(
    val startIso: String,
    val targetHours: Int,
)

@Serializable
data class IntermittentSchedulePlan(
    val id: String,
    val name: String,
    val targetHours: Int,
    val startHour: Int,
    val weekdays: List<Int>,
)

@Serializable(with = HouseholdProfileSerializer::class)
data class HouseholdProfile(
    val id: String,
    val name: String,
    val isAge14OrOlderForAbstinence: Boolean,
    val isAge18OrOlderForFasting: Boolean,
    val medicalDispensation: Boolean,
)

object HouseholdProfileSerializer : kotlinx.serialization.KSerializer<HouseholdProfile> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("HouseholdProfile", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: HouseholdProfile,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: throw SerializationException("HouseholdProfile requires Json encoding.")
        jsonEncoder.encodeJsonElement(
            kotlinx.serialization.json.buildJsonObject {
                put("id", JsonPrimitive(value.id))
                put("name", JsonPrimitive(value.name))
                put("isAge14OrOlderForAbstinence", JsonPrimitive(value.isAge14OrOlderForAbstinence))
                put("isAge18OrOlderForFasting", JsonPrimitive(value.isAge18OrOlderForFasting))
                put("medicalDispensation", JsonPrimitive(value.medicalDispensation))
            },
        )
    }

    override fun deserialize(decoder: Decoder): HouseholdProfile {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("HouseholdProfile requires Json decoding.")
        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
        val id =
            jsonObject["id"]?.jsonPrimitive?.content
                ?: throw SerializationException("HouseholdProfile.id missing")
        val name =
            jsonObject["name"]?.jsonPrimitive?.content
                ?: throw SerializationException("HouseholdProfile.name missing")
        val medicalDispensation = jsonObject["medicalDispensation"]?.jsonPrimitive?.booleanOrNull ?: false
        val explicitAbstinence = jsonObject["isAge14OrOlderForAbstinence"]?.jsonPrimitive?.booleanOrNull
        val explicitFasting = jsonObject["isAge18OrOlderForFasting"]?.jsonPrimitive?.booleanOrNull

        if (explicitAbstinence != null && explicitFasting != null) {
            return HouseholdProfile(
                id = id,
                name = name,
                isAge14OrOlderForAbstinence = explicitAbstinence,
                isAge18OrOlderForFasting = explicitFasting,
                medicalDispensation = medicalDispensation,
            )
        }

        val legacyAge =
            legacyAge(
                birthYear = jsonObject["birthYear"]?.jsonPrimitive?.intOrNull ?: 0,
                birthMonth = jsonObject["birthMonth"]?.jsonPrimitive?.intOrNull ?: 0,
                birthDay = jsonObject["birthDay"]?.jsonPrimitive?.intOrNull ?: 0,
            )
        return HouseholdProfile(
            id = id,
            name = name,
            isAge14OrOlderForAbstinence = legacyAge?.let { it >= 14 } ?: true,
            isAge18OrOlderForFasting = legacyAge?.let { it in 18..<60 } ?: true,
            medicalDispensation = medicalDispensation,
        )
    }

    private fun legacyAge(
        birthYear: Int,
        birthMonth: Int,
        birthDay: Int,
    ): Int? {
        if (birthYear < 1900) {
            return null
        }

        val today = LocalDate.now()
        return runCatching {
            if (birthMonth in 1..12 && birthDay in 1..31) {
                val birthDate = LocalDate.of(birthYear, birthMonth, birthDay)
                var age = today.year - birthYear
                if (today < birthDate.plusYears(age.toLong())) {
                    age -= 1
                }
                age.coerceAtLeast(0)
            } else {
                (today.year - birthYear).coerceAtLeast(0)
            }
        }.getOrElse {
            (today.year - birthYear).coerceAtLeast(0)
        }
    }
}

@Serializable
data class ReflectionJournalEntry(
    val id: String,
    val createdAtIso: String,
    val title: String,
    val body: String,
)

@Serializable
data class PremiumChecklistItem(
    val id: String,
    val title: String,
    val isDone: Boolean,
)

@Serializable
data class WeeklyIntention(
    val id: String,
    val weekday: Int,
    val note: String,
)

@Serializable
data class SeasonCommitment(
    val id: String,
    val season: LiturgicalSeason,
    val title: String,
    val isEnabled: Boolean,
)

@Serializable
data class FastingPlanningData(
    val requiredGoal: Int,
    val optionalGoal: Int,
    val weeklyIntentions: List<WeeklyIntention>,
    val seasonCommitments: List<SeasonCommitment>,
) {
    companion object {
        val default =
            FastingPlanningData(
                requiredGoal = 20,
                optionalGoal = 40,
                weeklyIntentions =
                    listOf(
                        WeeklyIntention(id = "mass-examen", weekday = 1, note = "Mass and examen"),
                        WeeklyIntention(id = "friday-almsgiving", weekday = 5, note = "Friday penance with almsgiving"),
                    ),
                seasonCommitments =
                    listOf(
                        SeasonCommitment(
                            id = "advent",
                            season = LiturgicalSeason.ADVENT,
                            title = "Simplify one meal weekly",
                            isEnabled = true,
                        ),
                        SeasonCommitment(
                            id = "lent",
                            season = LiturgicalSeason.LENT,
                            title = "Fast with daily Rosary",
                            isEnabled = true,
                        ),
                        SeasonCommitment(
                            id = "easter",
                            season = LiturgicalSeason.EASTER,
                            title = "Add thanksgiving prayer at meals",
                            isEnabled = true,
                        ),
                        SeasonCommitment(
                            id = "ordinary",
                            season = LiturgicalSeason.ORDINARY,
                            title = "Friday abstinence or substitute penance",
                            isEnabled = true,
                        ),
                    ),
            )
    }
}

@Serializable
enum class PremiumRuleTemplate(val label: String) {
    BEGINNER("Beginner"),
    STEADY("Steady"),
    DISCIPLINED("Disciplined"),
    TRADITIONAL("Traditional"),
    CUSTOM("Custom"),
}

@Serializable
enum class PremiumSeasonProgram(val label: String) {
    LITURGICAL_RHYTHM("Liturgical Rhythm"),
    LENT_DEEPEN("Lenten Deepen"),
    ADVENT_WATCH("Advent Watch"),
    FRIDAY_FIDELITY("Friday Fidelity"),
}

@Serializable
data class PremiumConditionRules(
    val remindIfUnloggedByNoon: Boolean = true,
    val requiredDaysDoubleReminder: Boolean = true,
    val milestoneNudgesForActiveFast: Boolean = true,
)

@Serializable
data class PremiumVirtueLog(
    val id: String,
    val createdAtIso: String,
    val virtue: String,
    val note: String,
)

@Serializable
data class PremiumCompanionState(
    val template: PremiumRuleTemplate = PremiumRuleTemplate.STEADY,
    val optionalDisciplinesPerWeek: Int = 2,
    val fixedFastWeekday: Int = 6,
    val protectFeastDays: Boolean = true,
    val conditionRules: PremiumConditionRules = PremiumConditionRules(),
    val seasonProgram: PremiumSeasonProgram = PremiumSeasonProgram.LITURGICAL_RHYTHM,
    val seasonProgramStartIso: String,
    val completedProgramActions: List<String> = emptyList(),
    val virtueLogs: List<PremiumVirtueLog> = emptyList(),
)

@Serializable
data class MissedDayRecoveryPlan(
    val titleLine: String,
    val summaryLine: String,
    val steps: List<String>,
    val nextRequiredLine: String,
)

@Serializable
data class PremiumSeasonPlan(
    val titleLine: String,
    val focusLine: String,
    val practices: List<String>,
    val fastingIntensity: String,
)

@Serializable
data class PremiumReminderRecommendation(
    val shouldEnableDailySupport: Boolean,
    val shouldEnableMorning: Boolean,
    val shouldEnableEvening: Boolean,
    val summaryLine: String,
)

@Serializable
data class PremiumSeasonCompletionRow(
    val id: String,
    val season: LiturgicalSeason,
    val completedCount: Int,
    val totalCount: Int,
) {
    val completionPercent: Int
        get() {
            if (totalCount <= 0) {
                return 0
            }
            return ((completedCount.toDouble() / totalCount.toDouble()) * 100.0).roundToInt()
        }
}

@Serializable
data class PremiumAnalyticsSummary(
    val requiredCompletionPercent: Int,
    val overallCompletionPercent: Int,
    val missedCount: Int,
    val substitutedCount: Int,
    val intermittentTargetHitPercent: Int,
    val seasonRows: List<PremiumSeasonCompletionRow>,
)

@Serializable
data class PremiumReflection(
    val title: String,
    val body: String,
    val action: String,
)

@Serializable
data class PremiumAdaptiveRulePlan(
    val title: String,
    val summary: String,
    val weeklyActions: List<String>,
    val caution: String,
)

@Serializable
data class PremiumRecoveryCoachPlan(
    val title: String,
    val summary: String,
    val steps: List<String>,
)

@Serializable
enum class PremiumSubscriptionState {
    SUBSCRIBED,
    EXPIRED,
    IN_GRACE_PERIOD,
    IN_BILLING_RETRY,
    REVOKED,
}

@Serializable
data class WidgetSnapshot(
    val generatedAtIso: String,
    val todayTitle: String,
    val todayObligation: String,
    val nextRequiredTitle: String,
    val nextRequiredDateIso: String? = null,
    val completionRate: Double,
    val hasActiveIntermittentFast: Boolean,
    val activeIntermittentFastStartIso: String? = null,
    val activeIntermittentTargetHours: Int,
    val premiumMotivationLine: String = "Stay faithful in small daily disciplines.",
)

@Serializable
enum class ReminderTier(
    val label: String,
    val summary: String,
    val supportEnabled: Boolean,
    val morningEnabled: Boolean,
    val eveningEnabled: Boolean,
) {
    MINIMAL("Minimal", "Required days only", false, false, false),
    BALANCED("Balanced", "Required days plus a morning check-in", true, true, false),
    GUIDED("Guided", "Required days with morning and evening support", true, true, true),
    ;

    companion object {
        fun infer(
            supportEnabled: Boolean,
            morningEnabled: Boolean,
            eveningEnabled: Boolean,
        ): ReminderTier {
            return when {
                supportEnabled && morningEnabled && eveningEnabled -> GUIDED
                supportEnabled && morningEnabled -> BALANCED
                else -> MINIMAL
            }
        }
    }
}

@Serializable
enum class ObservanceFilter(val label: String) {
    ALL("All"),
    REQUIRED_ONLY("Required"),
    TRACKED_ONLY("Tracked"),
}

@Serializable
enum class CalendarWindow(val label: String) {
    ALL_YEAR("All Year"),
    THIS_MONTH("This Month"),
    NEXT_30_DAYS("Next 30 Days"),
}

@Serializable
enum class ObservanceSortOrder(val label: String) {
    CHRONOLOGICAL("By Date"),
    REQUIRED_FIRST("Required First"),
}

@Serializable
data class ObservanceQueryState(
    val query: String = "",
    val filter: ObservanceFilter = ObservanceFilter.ALL,
    val window: CalendarWindow = CalendarWindow.ALL_YEAR,
    val sortOrder: ObservanceSortOrder = ObservanceSortOrder.CHRONOLOGICAL,
)

@Serializable
data class SyncSnapshot(
    val lastSyncDateIso: String? = null,
    val completedObservancesCount: Int = 0,
    val fridayNotesCount: Int = 0,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class OnboardingState(
    val isCompleted: Boolean = false,
    val currentStep: Int = 1,
    val totalSteps: Int = 4,
    val noticeAcknowledged: Boolean = false,
    val selectedRegion: RegionProfile = RegionProfile.US,
    val selectedReminderTier: ReminderTier = ReminderTier.BALANCED,
    val dailyQuoteReminderEnabled: Boolean = false,
    val dailyQuoteReminderHour: Int = 8,
    val dailyQuoteReminderMinute: Int = 0,
    val hasFullBirthDate: Boolean = false,
)

@Serializable
data class SetupProgressState(
    val completedSteps: Int,
    val totalSteps: Int,
    val birthProfileComplete: Boolean,
    val independentNoticeAcknowledged: Boolean,
    val regionSelected: Boolean,
    val reminderTierSelected: Boolean,
    val onboardingCompleted: Boolean,
)

@Serializable
data class ReminderCenterState(
    val selectedTier: ReminderTier = ReminderTier.BALANCED,
    val supportRemindersEnabled: Boolean = true,
    val morningCheckInEnabled: Boolean = true,
    val eveningCheckInEnabled: Boolean = false,
    val dailyQuoteReminderEnabled: Boolean = false,
    val dailyQuoteReminderHour: Int = 8,
    val dailyQuoteReminderMinute: Int = 0,
) {
    val dailyQuoteTimeLabel: String
        get() = "%02d:%02d".format(dailyQuoteReminderHour, dailyQuoteReminderMinute)
}

@Serializable
data class StorageDiagnosticsState(
    val lastLocalWriteIso: String? = null,
    val completedObservancesCount: Int = 0,
    val fridayNotesCount: Int = 0,
    val intermittentSessionsCount: Int = 0,
    val reflectionsCount: Int = 0,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class SeasonalHeroState(
    val campaignTitle: String,
    val campaignSubtitle: String,
    val formationLine: String,
    val quote: CatholicFastingQuote,
    val imagery: List<SacredImageryItem>,
)

@Serializable
enum class ContentLocale {
    ENGLISH,
    SPANISH,
}

@Serializable
data class CatholicFastingQuote(
    val id: String,
    val text: String,
    val author: String,
    val source: String,
    val tradition: String,
)

@Serializable
data class SacredImageryItem(
    val id: String,
    val assetName: String,
    val title: String,
    val subtitle: String,
)

@Serializable
data class SeasonalContentQuote(
    val text: String,
    val author: String,
    val source: String,
    val tradition: String,
)

@Serializable
data class SeasonalContentPack(
    val season: LiturgicalSeason,
    val locale: ContentLocale,
    val heroAssetNames: List<String>,
    val campaignTitle: String,
    val campaignSubtitle: String,
    val formationLines: List<String>,
    val quotes: List<SeasonalContentQuote>,
)

@Serializable
data class LaunchFunnelSnapshot(
    val startedAtIso: String,
    val completedOnboardingAtIso: String? = null,
    val independentAppNoticeAcknowledged: Boolean = false,
    val selectedRegion: RegionProfile = RegionProfile.US,
    val selectedReminderTier: ReminderTier = ReminderTier.BALANCED,
    val dailyQuoteReminderEnabled: Boolean = false,
    val dailyQuoteReminderHour: Int = 8,
    val dailyQuoteReminderMinute: Int = 0,
    val firstActionCompletedAtIso: String? = null,
    val paywallSeenAtIso: String? = null,
    val paywallViewCount: Int = 0,
    val lockedUpgradeTapCount: Int = 0,
    val premiumPreviewSeenAtIso: String? = null,
    val purchaseStartedAtIso: String? = null,
    val premiumUnlockedAtIso: String? = null,
)

@Serializable
data class SubscriptionOffer(
    val id: String,
    val displayTitle: String,
    val durationLabel: String,
    val billingCadenceLabel: String,
    val outcomeSummary: String,
    val isPrimaryAnchor: Boolean,
)

@Serializable
data class SubscriptionPillar(
    val id: String,
    val title: String,
    val subtitle: String,
    val outcomes: List<String>,
)

@Serializable
data class SubscriptionOfferCatalog(
    val title: String,
    val subtitle: String,
    val pillars: List<SubscriptionPillar>,
    val offers: List<SubscriptionOffer>,
) {
    companion object {
        val catholicFasting =
            SubscriptionOfferCatalog(
                title = "Formation Toolkit",
                subtitle = "Premium helps users plan ahead, stay steady after misses, and reflect prayerfully.",
                pillars =
                    listOf(
                        SubscriptionPillar(
                            id = "planning",
                            title = "Planning",
                            subtitle = "Know what to do next without fighting the Church calendar.",
                            outcomes =
                                listOf(
                                    "See a realistic season path instead of guessing week to week",
                                    "Shape personal long-fast disciplines without losing feast-day balance",
                                    "Protect celebration days so personal discipline does not overreach",
                                ),
                        ),
                        SubscriptionPillar(
                            id = "accountability",
                            title = "Accountability",
                            subtitle = "Recover quickly and keep momentum when discipline slips.",
                            outcomes =
                                listOf(
                                    "Turn reminders into a steadier rule of life",
                                    "Spot slippage early with completion trends and recovery guidance",
                                    "Review longer intermittent history with milestone feedback",
                                ),
                        ),
                        SubscriptionPillar(
                            id = "reflection",
                            title = "Reflection",
                            subtitle = "Connect fasting to prayer, virtue, and honest review.",
                            outcomes =
                                listOf(
                                    "Use guided prompts to examine intention, not just completion",
                                    "Keep a private local journal with virtue notes",
                                    "Export a fasting summary for spiritual direction or personal review",
                                ),
                        ),
                    ),
                offers =
                    listOf(
                        SubscriptionOffer(
                            id = "com.kevpierce.catholicfasting.premium.yearly.v3",
                            displayTitle = "Premium Yearly",
                            durationLabel = "1 year",
                            billingCadenceLabel = "Billed once per year",
                            outcomeSummary = "Best value for staying consistent through the full liturgical year.",
                            isPrimaryAnchor = true,
                        ),
                        SubscriptionOffer(
                            id = "com.kevpierce.catholicfasting.premium.monthly.v3",
                            displayTitle = "Premium Monthly",
                            durationLabel = "1 month",
                            billingCadenceLabel = "Billed monthly",
                            outcomeSummary = "Lower-friction way to begin premium support and review habits.",
                            isPrimaryAnchor = false,
                        ),
                    ),
            )
    }
}

object AppDeepLinks {
    const val SCHEME = "catholicfasting"
    const val HOST = "open"
    const val TODAY = "$SCHEME://$HOST/today"
    const val CALENDAR = "$SCHEME://$HOST/calendar"
    const val TRACKER = "$SCHEME://$HOST/tracker"
    const val MORE_PREMIUM = "$SCHEME://$HOST/more/premium"
    const val MORE_SETUP = "$SCHEME://$HOST/more/setup"
    const val MORE_PRIVACY = "$SCHEME://$HOST/more/privacy"
    const val CALENDAR_FRIDAY_NOTE = "$SCHEME://$HOST/calendar/friday-note"
}
