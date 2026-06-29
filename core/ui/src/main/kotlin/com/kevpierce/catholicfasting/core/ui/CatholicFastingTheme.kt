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
data class CatholicFastingShape(
    val cardRadius: Dp,
    val controlRadius: Dp,
    val sheetRadius: Dp,
)

@Immutable
data class CatholicFastingElevation(
    val flat: Dp,
    val raised: Dp,
)

@Immutable
data class CatholicFastingMotion(
    val quickMillis: Int,
    val standardMillis: Int,
    val deliberateMillis: Int,
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

private val LocalCatholicFastingShape =
    staticCompositionLocalOf<CatholicFastingShape> {
        error("CatholicFastingShape not provided")
    }

private val LocalCatholicFastingElevation =
    staticCompositionLocalOf<CatholicFastingElevation> {
        error("CatholicFastingElevation not provided")
    }

private val LocalCatholicFastingMotion =
    staticCompositionLocalOf<CatholicFastingMotion> {
        error("CatholicFastingMotion not provided")
    }

private val LocalCatholicFastingSemanticTones =
    staticCompositionLocalOf<CatholicFastingSemanticTones> {
        error("CatholicFastingSemanticTones not provided")
    }

object CatholicFastingThemeValues {
    val typography: CatholicFastingTypography
        @Composable get() = LocalCatholicFastingTypography.current

    val spacing: CatholicFastingSpacing
        @Composable get() = LocalCatholicFastingSpacing.current

    val cardDefaults: CatholicFastingCardDefaults
        @Composable get() = LocalCatholicFastingCardDefaults.current

    val shape: CatholicFastingShape
        @Composable get() = LocalCatholicFastingShape.current

    val elevation: CatholicFastingElevation
        @Composable get() = LocalCatholicFastingElevation.current

    val motion: CatholicFastingMotion
        @Composable get() = LocalCatholicFastingMotion.current

    val semanticTones: CatholicFastingSemanticTones
        @Composable get() = LocalCatholicFastingSemanticTones.current
}

@Composable
fun CatholicFastingTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = appColorScheme(darkTheme)
    val materialTypography = materialTypography()
    val typography = catholicFastingTypography(materialTypography)
    val spacing = catholicFastingSpacing()
    val cardDefaults = catholicFastingCardDefaults(spacing)
    val shape = catholicFastingShape()
    val elevation = catholicFastingElevation()
    val motion = catholicFastingMotion()
    val semanticTones = catholicFastingSemanticTones(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = materialTypography,
    ) {
        CompositionLocalProvider(
            LocalCatholicFastingTypography provides typography,
            LocalCatholicFastingSpacing provides spacing,
            LocalCatholicFastingCardDefaults provides cardDefaults,
            LocalCatholicFastingShape provides shape,
            LocalCatholicFastingElevation provides elevation,
            LocalCatholicFastingMotion provides motion,
            LocalCatholicFastingSemanticTones provides semanticTones,
            content = content,
        )
    }
}

private fun appColorScheme(darkTheme: Boolean) =
    if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFD0BDE6),
            onPrimary = Color(0xFF38254C),
            primaryContainer = Color(0xFF4F3B64),
            onPrimaryContainer = Color(0xFFF0E3FF),
            secondary = Color(0xFFD4C3A0),
            onSecondary = Color(0xFF392F1B),
            secondaryContainer = Color(0xFF51462F),
            onSecondaryContainer = Color(0xFFF2E1BB),
            tertiary = Color(0xFFADCFAE),
            onTertiary = Color(0xFF1E3622),
            tertiaryContainer = Color(0xFF354D38),
            onTertiaryContainer = Color(0xFFC9EBC9),
            background = Color(0xFF151318),
            onBackground = Color(0xFFE8E1E8),
            surface = Color(0xFF1C191F),
            onSurface = Color(0xFFE8E1E8),
            surfaceVariant = Color(0xFF4A454D),
            onSurfaceVariant = Color(0xFFCEC4D0),
            outline = Color(0xFF978E9A),
            outlineVariant = Color(0xFF4A454D),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF67517E),
            onPrimary = Color(0xFFFDF8FF),
            primaryContainer = Color(0xFFEEDBFF),
            onPrimaryContainer = Color(0xFF221036),
            secondary = Color(0xFF6A5D3E),
            onSecondary = Color(0xFFFFFAEE),
            secondaryContainer = Color(0xFFF2E1BB),
            onSecondaryContainer = Color(0xFF231A06),
            tertiary = Color(0xFF4D7D57),
            onTertiary = Color(0xFFF7FFF6),
            tertiaryContainer = Color(0xFFCFE9D1),
            onTertiaryContainer = Color(0xFF0A2A12),
            background = Color(0xFFFFFBFE),
            onBackground = Color(0xFF1E1A20),
            surface = Color(0xFFFFFBFE),
            onSurface = Color(0xFF1E1A20),
            surfaceVariant = Color(0xFFE9E0EA),
            onSurfaceVariant = Color(0xFF4A454D),
            outline = Color(0xFF7B747F),
            outlineVariant = Color(0xFFCCC3CE),
            error = Color(0xFFBA1A1A),
            onError = Color(0xFFFFFBFF),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),
        )
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
        cornerRadius = 16.dp,
        borderWidth = 1.dp,
        contentSpacing = spacing.small,
        contentPadding = spacing.medium,
    )

private fun catholicFastingShape() =
    CatholicFastingShape(
        cardRadius = 16.dp,
        controlRadius = 12.dp,
        sheetRadius = 24.dp,
    )

private fun catholicFastingElevation() =
    CatholicFastingElevation(
        flat = 0.dp,
        raised = 1.dp,
    )

private fun catholicFastingMotion() =
    CatholicFastingMotion(
        quickMillis = 120,
        standardMillis = 220,
        deliberateMillis = 320,
    )
