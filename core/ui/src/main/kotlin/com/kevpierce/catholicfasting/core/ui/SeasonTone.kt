package com.kevpierce.catholicfasting.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason

@Immutable
data class SeasonTone(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val accentColor: Color,
)

fun seasonTone(
    season: LiturgicalSeason,
    darkTheme: Boolean,
): SeasonTone = if (darkTheme) darkSeasonTone(season) else lightSeasonTone(season)

@Composable
fun rememberSeasonTone(season: LiturgicalSeason): SeasonTone = seasonTone(season, isSystemInDarkTheme())

private fun darkSeasonTone(season: LiturgicalSeason) =
    when (season) {
        LiturgicalSeason.LENT ->
            SeasonTone(
                containerColor = Color(0xFF3C3248),
                contentColor = Color(0xFFF1E9F9),
                borderColor = Color(0xFF8D72A8),
                accentColor = Color(0xFFC7A4E4),
            )
        LiturgicalSeason.ADVENT ->
            SeasonTone(
                containerColor = Color(0xFF2D3648),
                contentColor = Color(0xFFE7EEFB),
                borderColor = Color(0xFF6F87A8),
                accentColor = Color(0xFFAEC6E8),
            )
        LiturgicalSeason.CHRISTMAS ->
            SeasonTone(
                containerColor = Color(0xFF3E3525),
                contentColor = Color(0xFFFAF0D8),
                borderColor = Color(0xFFB09760),
                accentColor = Color(0xFFE6C67A),
            )
        LiturgicalSeason.EASTER ->
            SeasonTone(
                containerColor = Color(0xFF3D322A),
                contentColor = Color(0xFFFBEEE4),
                borderColor = Color(0xFFC48E6A),
                accentColor = Color(0xFFF2B28D),
            )
        LiturgicalSeason.ORDINARY ->
            SeasonTone(
                containerColor = Color(0xFF22392A),
                contentColor = Color(0xFFE3F4E8),
                borderColor = Color(0xFF5D9A70),
                accentColor = Color(0xFFA9D7B5),
            )
    }

private fun lightSeasonTone(season: LiturgicalSeason) =
    when (season) {
        LiturgicalSeason.LENT ->
            SeasonTone(
                containerColor = Color(0xFFF4EFF9),
                contentColor = Color(0xFF2F2340),
                borderColor = Color(0xFFC6B5DA),
                accentColor = Color(0xFF6F528C),
            )
        LiturgicalSeason.ADVENT ->
            SeasonTone(
                containerColor = Color(0xFFEEF3FA),
                contentColor = Color(0xFF243149),
                borderColor = Color(0xFFBCCBE0),
                accentColor = Color(0xFF49688E),
            )
        LiturgicalSeason.CHRISTMAS ->
            SeasonTone(
                containerColor = Color(0xFFFFF7E9),
                contentColor = Color(0xFF47371A),
                borderColor = Color(0xFFE1CFA5),
                accentColor = Color(0xFF9A772A),
            )
        LiturgicalSeason.EASTER ->
            SeasonTone(
                containerColor = Color(0xFFFFF1EA),
                contentColor = Color(0xFF4A2D21),
                borderColor = Color(0xFFE6C1AE),
                accentColor = Color(0xFFB66B45),
            )
        LiturgicalSeason.ORDINARY ->
            SeasonTone(
                containerColor = Color(0xFFEEF8F0),
                contentColor = Color(0xFF1F3A27),
                borderColor = Color(0xFFBAD7C0),
                accentColor = Color(0xFF4D7D57),
            )
    }
