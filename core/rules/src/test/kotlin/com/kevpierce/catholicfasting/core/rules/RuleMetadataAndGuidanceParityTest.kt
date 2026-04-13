package com.kevpierce.catholicfasting.core.rules

import com.google.common.truth.Truth.assertThat
import com.kevpierce.catholicfasting.core.model.GuidanceScenario
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.RuleSettings
import org.junit.Test

class RuleMetadataAndGuidanceParityTest {
    @Test
    fun ruleBundleAuditIsVerifiedOrCarriesWarnings() {
        val audit = ObservanceCalculator.ruleBundleAudit()

        assertThat(audit.source).isNotEmpty()
        assertThat(audit.isVerified || audit.warnings.isNotEmpty()).isTrue()
    }

    @Test
    fun ashWednesdayCarriesRationaleCitationsAndRuleVersion() {
        val observances = ObservanceCalculator.makeCalendar(year = 2026, settings = sampleSettings())
        val ashWednesday = observances.first { it.title == "Ash Wednesday" }

        assertThat(ashWednesday.rationale).isNotEmpty()
        assertThat(ashWednesday.citations).isNotEmpty()
        assertThat(ashWednesday.ruleVersion).isNotEmpty()
    }

    @Test
    fun heavyLaborGuidanceMentionsAdjustmentOrPastoralJudgment() {
        val recommendations =
            FoodGuidanceEngine.recommendations(
                scenario = GuidanceScenario.HEAVY_LABOR,
                settings = sampleSettings(),
            )

        assertThat(
            recommendations.any { line ->
                line.contains("heavy labor", ignoreCase = true) ||
                    line.contains("pastor", ignoreCase = true) ||
                    line.contains("unsafe", ignoreCase = true)
            },
        ).isTrue()
    }

    @Test
    fun canadaGuidanceUsesCccbSourceLine() {
        val snapshot =
            FoodGuidanceEngine.snapshot(
                scenario = GuidanceScenario.NORMAL_DAY,
                settings = sampleSettings().copy(regionProfile = RegionProfile.CANADA),
            )

        assertThat(snapshot.sourceLine).contains("CCCB")
        assertThat(snapshot.sourceLine).contains("U.S.-first abstinence examples")
    }

    @Test
    fun normalDaySnapshotKeepsBrothInExtraGuidanceNotCoreMeatList() {
        val snapshot =
            FoodGuidanceEngine.snapshot(
                scenario = GuidanceScenario.NORMAL_DAY,
                settings = sampleSettings(),
            )

        assertThat(
            snapshot.extraGuidance.items.any { item ->
                item.detail.contains("broth", ignoreCase = true)
            },
        ).isTrue()
        assertThat(
            snapshot.whatCountsAsMeat.items.any { item ->
                item.detail.contains("broth", ignoreCase = true)
            },
        ).isFalse()
    }

    private fun sampleSettings(): RuleSettings = RuleSettings(regionProfile = RegionProfile.US)
}
