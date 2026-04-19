package com.kevpierce.catholicfasting.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kevpierce.catholicfasting.core.model.LiturgicalSeason

@Immutable
data class CatholicFastingTypography(
    val heroTitle: TextStyle,
    val screenTitle: TextStyle,
    val sectionTitle: TextStyle,
    val body: TextStyle,
    val supporting: TextStyle,
    val utility: TextStyle,
)

@Immutable
data class CatholicFastingSpacing(
    val xxSmall: Dp,
    val xSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val xLarge: Dp,
)

@Immutable
data class CatholicFastingCardDefaults(
    val cornerRadius: Dp,
    val borderWidth: Dp,
    val contentSpacing: Dp,
    val contentPadding: Dp,
)

@Immutable
data class SeasonTone(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val accentColor: Color,
)

private val LocalCatholicFastingTypography =
    staticCompositionLocalOf<CatholicFastingTypography> {
        error("CatholicFastingTypography not provided")
    }

private val LocalCatholicFastingSpacing =
    staticCompositionLocalOf<CatholicFastingSpacing> {
        error("CatholicFastingSpacing not provided")
    }

private val LocalCatholicFastingCardDefaults =
    staticCompositionLocalOf<CatholicFastingCardDefaults> {
        error("CatholicFastingCardDefaults not provided")
    }

object CatholicFastingThemeValues {
    val typography: CatholicFastingTypography
        @Composable get() = LocalCatholicFastingTypography.current

    val spacing: CatholicFastingSpacing
        @Composable get() = LocalCatholicFastingSpacing.current

    val cardDefaults: CatholicFastingCardDefaults
        @Composable get() = LocalCatholicFastingCardDefaults.current
}

@Composable
fun catholicFastingTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = appColorScheme(darkTheme)
    val materialTypography = materialTypography()
    val typography = catholicFastingTypography(materialTypography)
    val spacing = catholicFastingSpacing()
    val cardDefaults = catholicFastingCardDefaults(spacing)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = materialTypography,
    ) {
        CompositionLocalProvider(
            LocalCatholicFastingTypography provides typography,
            LocalCatholicFastingSpacing provides spacing,
            LocalCatholicFastingCardDefaults provides cardDefaults,
            content = content,
        )
    }
}

fun seasonTone(
    season: LiturgicalSeason,
    darkTheme: Boolean,
): SeasonTone = if (darkTheme) darkSeasonTone(season) else lightSeasonTone(season)

@Composable
fun rememberSeasonTone(season: LiturgicalSeason): SeasonTone = seasonTone(season, isSystemInDarkTheme())

private fun appColorScheme(darkTheme: Boolean) =
    if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

private fun materialTypography(): Typography {
    val baseTypography = Typography()
    val sans = FontFamily.SansSerif
    val serif = FontFamily.Serif

    return baseTypography.copy(
        displaySmall =
            baseTypography.displaySmall.copy(
                fontFamily = serif,
                fontWeight = FontWeight.SemiBold,
            ),
        headlineMedium =
            baseTypography.headlineMedium.copy(
                fontFamily = sans,
                fontWeight = FontWeight.SemiBold,
            ),
        titleLarge =
            baseTypography.titleLarge.copy(
                fontFamily = sans,
                fontWeight = FontWeight.SemiBold,
            ),
        titleMedium =
            baseTypography.titleMedium.copy(
                fontFamily = sans,
                fontWeight = FontWeight.Medium,
            ),
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = sans),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = sans),
        bodySmall = baseTypography.bodySmall.copy(fontFamily = sans),
        labelLarge =
            baseTypography.labelLarge.copy(
                fontFamily = sans,
                fontWeight = FontWeight.Medium,
            ),
        labelMedium = baseTypography.labelMedium.copy(fontFamily = sans),
        labelSmall = baseTypography.labelSmall.copy(fontFamily = sans),
    )
}

private fun catholicFastingTypography(materialTypography: Typography) =
    CatholicFastingTypography(
        heroTitle =
            materialTypography.displaySmall.copy(
                letterSpacing = 0.2.sp,
            ),
        screenTitle = materialTypography.headlineMedium,
        sectionTitle = materialTypography.titleLarge,
        body = materialTypography.bodyLarge,
        supporting = materialTypography.bodyMedium,
        utility = materialTypography.bodySmall,
    )

private fun catholicFastingSpacing() =
    CatholicFastingSpacing(
        xxSmall = 4.dp,
        xSmall = 8.dp,
        small = 12.dp,
        medium = 16.dp,
        large = 24.dp,
        xLarge = 32.dp,
    )

private fun catholicFastingCardDefaults(spacing: CatholicFastingSpacing) =
    CatholicFastingCardDefaults(
        cornerRadius = 24.dp,
        borderWidth = 1.dp,
        contentSpacing = spacing.xSmall,
        contentPadding = spacing.medium,
    )

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
