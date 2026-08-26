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
                containerColor = Color(0xFFF7F1F5),
                contentColor = Color(0xFF4D315E),
                borderColor = Color(0xFFC8B9C9),
                accentColor = Color(0xFF6B4776),
            )
        LiturgicalSeason.ADVENT ->
            SeasonTone(
                containerColor = Color(0xFFF5F1F6),
                contentColor = Color(0xFF4F3866),
                borderColor = Color(0xFFC5B8CC),
                accentColor = Color(0xFF674F77),
            )
        LiturgicalSeason.CHRISTMAS ->
            SeasonTone(
                containerColor = Color(0xFFFBF5E8),
                contentColor = Color(0xFF6B292B),
                borderColor = Color(0xFFD8C6A1),
                accentColor = Color(0xFF8B5D25),
            )
        LiturgicalSeason.EASTER ->
            SeasonTone(
                containerColor = Color(0xFFFBF7E9),
                contentColor = Color(0xFF6E4F1A),
                borderColor = Color(0xFFD5C9A6),
                accentColor = Color(0xFF8B6B24),
            )
        LiturgicalSeason.ORDINARY ->
            SeasonTone(
                containerColor = Color(0xFFF5F5E8),
                contentColor = Color(0xFF2E573B),
                borderColor = Color(0xFFB4C2AA),
                accentColor = Color(0xFF496F51),
            )
    }
