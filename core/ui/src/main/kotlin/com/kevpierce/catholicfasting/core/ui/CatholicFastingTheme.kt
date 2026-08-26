package com.kevpierce.catholicfasting.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
        shapes =
            Shapes(
                extraSmall =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(8.dp),
                small =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(12.dp),
                medium =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(16.dp),
                large =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(24.dp),
                extraLarge =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(28.dp),
            ),
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
            primary = Color(0xFFB4D0B8),
            onPrimary = Color(0xFF193721),
            primaryContainer = Color(0xFF304C36),
            onPrimaryContainer = Color(0xFFD7E9D9),
            secondary = Color(0xFFE0C17E),
            onSecondary = Color(0xFF3D2F0A),
            secondaryContainer = Color(0xFF55471F),
            onSecondaryContainer = Color(0xFFF5E1A9),
            tertiary = Color(0xFFD8C69B),
            onTertiary = Color(0xFF392F18),
            tertiaryContainer = Color(0xFF50462D),
            onTertiaryContainer = Color(0xFFF1E3BE),
            background = Color(0xFF151713),
            onBackground = Color(0xFFE9E7DE),
            surface = Color(0xFF1C1F1A),
            onSurface = Color(0xFFE9E7DE),
            surfaceVariant = Color(0xFF42463D),
            onSurfaceVariant = Color(0xFFC9C9BD),
            outline = Color(0xFF929588),
            outlineVariant = Color(0xFF44483F),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF2E573B),
            onPrimary = Color(0xFFFBF9F1),
            primaryContainer = Color(0xFFDDE9DD),
            onPrimaryContainer = Color(0xFF163321),
            secondary = Color(0xFF7D5E1E),
            onSecondary = Color(0xFFFFF8E8),
            secondaryContainer = Color(0xFFF1E2B9),
            onSecondaryContainer = Color(0xFF2B2106),
            tertiary = Color(0xFF5C6F59),
            onTertiary = Color(0xFFF8FFF5),
            tertiaryContainer = Color(0xFFDCE8D8),
            onTertiaryContainer = Color(0xFF1B2D1A),
            background = Color(0xFFFBF9F1),
            onBackground = Color(0xFF171411),
            surface = Color(0xFFFFFDF7),
            onSurface = Color(0xFF171411),
            surfaceVariant = Color(0xFFEDEDE0),
            onSurfaceVariant = Color(0xFF4E4B41),
            outline = Color(0xFF7E7A6B),
            outlineVariant = Color(0xFFD5D0BF),
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
                fontSize = 29.sp,
                lineHeight = 35.sp,
                letterSpacing = (-0.2).sp,
            ),
        screenTitle =
            materialTypography.headlineMedium.copy(
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
            ),
        sectionTitle =
            materialTypography.titleLarge.copy(
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        body =
            materialTypography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        supporting =
            materialTypography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        utility =
            materialTypography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
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
        contentSpacing = spacing.small,
        contentPadding = spacing.medium,
    )

private fun catholicFastingShape() =
    CatholicFastingShape(
        cardRadius = 24.dp,
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
