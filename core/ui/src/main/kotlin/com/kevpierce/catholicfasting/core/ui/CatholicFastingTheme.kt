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

private fun catholicFastingShape() =
    CatholicFastingShape(
        cardRadius = 24.dp,
        controlRadius = 12.dp,
        sheetRadius = 28.dp,
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
