package com.kevpierce.catholicfasting.feature.premium

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason

@Composable
internal fun LiturgicalSeason.localizedLabel(): String =
    when (this) {
        LiturgicalSeason.ADVENT -> stringResource(R.string.premium_label_season_advent)
        LiturgicalSeason.CHRISTMAS -> stringResource(R.string.premium_label_season_christmas)
        LiturgicalSeason.LENT -> stringResource(R.string.premium_label_season_lent)
        LiturgicalSeason.EASTER -> stringResource(R.string.premium_label_season_easter)
        LiturgicalSeason.ORDINARY -> stringResource(R.string.premium_label_season_ordinary)
    }
