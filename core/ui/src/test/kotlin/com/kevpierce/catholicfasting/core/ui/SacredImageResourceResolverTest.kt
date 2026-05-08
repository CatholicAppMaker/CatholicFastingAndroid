package com.kevpierce.catholicfasting.core.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SacredImageResourceResolverTest {
    @Test
    fun productionSacredGalleryAssetsResolveToDrawables() {
        val activeAssetNames =
            listOf(
                "HeroSacred",
                "SacredCrucifixAltar",
                "SacredPlanningJournal",
                "SacredChaliceVine",
                "SacredMonstrance",
                "SacredSacredHeart",
                "SacredRosaryCross",
                "SacredChiRho",
                "SacredScriptureCandle",
                "SacredJerusalemCross",
                "SacredMarianMonogram",
                "SacredAdventWreath",
                "SacredPaschalCandle",
                "SacredLentenPath",
                "SacredAlmsgivingTable",
                "SacredEmberDays",
                "SacredPurpleVeil",
                "SacredFridayAbstinence",
            )

        assertThat(activeAssetNames).hasSize(18)
        activeAssetNames.forEach { assetName ->
            assertThat(SacredImageResourceResolver.drawableResFor(assetName))
                .isNotNull()
        }
    }

    @Test
    fun archivedConceptAssetsDoNotResolve() {
        assertThat(SacredImageResourceResolver.drawableResFor("SacredConceptChiRho")).isNull()
        assertThat(SacredImageResourceResolver.drawableResFor("SacredConceptRosary")).isNull()
        assertThat(SacredImageResourceResolver.drawableResFor("SacredConceptHeart")).isNull()
    }
}
