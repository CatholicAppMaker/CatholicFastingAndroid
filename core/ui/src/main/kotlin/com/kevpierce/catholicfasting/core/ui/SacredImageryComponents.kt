@file:Suppress("MatchingDeclarationName")

package com.kevpierce.catholicfasting.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.model.SacredImageryItem

object SacredImageResourceResolver {
    private val drawableResources =
        mapOf(
            "HeroSacred" to R.drawable.hero_sacred,
            "GuidanceSacred" to R.drawable.guidance_sacred,
            "SacredAdventWreath" to R.drawable.sacred_advent_wreath,
            "SacredAlmsgivingTable" to R.drawable.sacred_almsgiving_table,
            "SacredAshWednesday" to R.drawable.sacred_ash_wednesday,
            "SacredCathedralLight" to R.drawable.sacred_cathedral_light,
            "SacredChaliceVine" to R.drawable.sacred_chalice_vine,
            "SacredChiRho" to R.drawable.sacred_chi_rho,
            "SacredCrucifixAltar" to R.drawable.sacred_crucifix_altar,
            "SacredDesertPilgrimage" to R.drawable.sacred_desert_pilgrimage,
            "SacredEmberDays" to R.drawable.sacred_ember_days,
            "SacredFridayAbstinence" to R.drawable.sacred_friday_abstinence,
            "SacredJerusalemCross" to R.drawable.sacred_jerusalem_cross,
            "SacredLentenPath" to R.drawable.sacred_lenten_path,
            "SacredMarianMonogram" to R.drawable.sacred_marian_monogram,
            "SacredMonstrance" to R.drawable.sacred_monstrance,
            "SacredPalmSunday" to R.drawable.sacred_palm_sunday,
            "SacredPaschalCandle" to R.drawable.sacred_paschal_candle,
            "SacredPlanningJournal" to R.drawable.sacred_planning_journal,
            "SacredPurpleVeil" to R.drawable.sacred_purple_veil,
            "SacredRosaryCross" to R.drawable.sacred_rosary_cross,
            "SacredSacredHeart" to R.drawable.sacred_sacred_heart,
            "SacredScriptureCandle" to R.drawable.sacred_scripture_candle,
        )

    fun drawableResFor(assetName: String): Int? = drawableResources[assetName]
}

@Composable
fun SacredImageryCard(
    item: SacredImageryItem,
    modifier: Modifier = Modifier,
) {
    val spacing = CatholicFastingThemeValues.spacing
    val drawableRes = SacredImageResourceResolver.drawableResFor(item.assetName)

    Card(
        modifier =
            modifier
                .width(206.dp)
                .semantics {
                    contentDescription = "${item.title}. ${item.subtitle}"
                },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
        ) {
            if (drawableRes != null) {
                Image(
                    painter = painterResource(drawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(116.dp)
                            .clip(RoundedCornerShape(14.dp)),
                )
            }
            Text(item.title, style = CatholicFastingThemeValues.typography.sectionTitle)
            Text(item.subtitle, style = CatholicFastingThemeValues.typography.supporting)
        }
    }
}
