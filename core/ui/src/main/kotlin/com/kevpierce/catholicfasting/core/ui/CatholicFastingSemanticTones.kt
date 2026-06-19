package com.kevpierce.catholicfasting.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CatholicFastingSemanticTone(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val accentColor: Color,
)

@Immutable
data class CatholicFastingSemanticTones(
    val ruleSource: CatholicFastingSemanticTone,
    val trackerActive: CatholicFastingSemanticTone,
    val trackerInactive: CatholicFastingSemanticTone,
    val premiumLocked: CatholicFastingSemanticTone,
    val premiumUnlocked: CatholicFastingSemanticTone,
    val reminderGranted: CatholicFastingSemanticTone,
    val reminderDenied: CatholicFastingSemanticTone,
)

internal fun catholicFastingSemanticTones(darkTheme: Boolean) =
    if (darkTheme) {
        darkCatholicFastingSemanticTones()
    } else {
        lightCatholicFastingSemanticTones()
    }

private fun darkCatholicFastingSemanticTones() =
    CatholicFastingSemanticTones(
        ruleSource =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFF2F312B),
                contentColor = Color(0xFFF0F2E9),
                borderColor = Color(0xFF7E846F),
                accentColor = Color(0xFFD3D9C3),
            ),
        trackerActive =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFF203835),
                contentColor = Color(0xFFE4F4F1),
                borderColor = Color(0xFF5B9A91),
                accentColor = Color(0xFFA7D7CF),
            ),
        trackerInactive =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFF303234),
                contentColor = Color(0xFFECEFF1),
                borderColor = Color(0xFF7D8387),
                accentColor = Color(0xFFC8CDD0),
            ),
        premiumLocked =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFF3B3325),
                contentColor = Color(0xFFF8EEDB),
                borderColor = Color(0xFF9B8156),
                accentColor = Color(0xFFE4C17D),
            ),
        premiumUnlocked =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFF26382A),
                contentColor = Color(0xFFE7F5E8),
                borderColor = Color(0xFF6CA276),
                accentColor = Color(0xFFB6DDBD),
            ),
        reminderGranted =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFF263544),
                contentColor = Color(0xFFE7F0FA),
                borderColor = Color(0xFF6E8EAE),
                accentColor = Color(0xFFB7D0EC),
            ),
        reminderDenied =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFF3A2E2D),
                contentColor = Color(0xFFF7E9E6),
                borderColor = Color(0xFFA47A72),
                accentColor = Color(0xFFE3B5AB),
            ),
    )

private fun lightCatholicFastingSemanticTones() =
    CatholicFastingSemanticTones(
        ruleSource =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFFF7F5ED),
                contentColor = Color(0xFF333527),
                borderColor = Color(0xFFD8D3BF),
                accentColor = Color(0xFF747B58),
            ),
        trackerActive =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFFEAF7F4),
                contentColor = Color(0xFF1E3A36),
                borderColor = Color(0xFFB8DAD4),
                accentColor = Color(0xFF4C8279),
            ),
        trackerInactive =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFFF2F4F5),
                contentColor = Color(0xFF303437),
                borderColor = Color(0xFFD2D7DA),
                accentColor = Color(0xFF687176),
            ),
        premiumLocked =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFFFFF6E4),
                contentColor = Color(0xFF46371E),
                borderColor = Color(0xFFE3CAA0),
                accentColor = Color(0xFF927033),
            ),
        premiumUnlocked =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFFECF8EE),
                contentColor = Color(0xFF233B27),
                borderColor = Color(0xFFC0DBC5),
                accentColor = Color(0xFF527D59),
            ),
        reminderGranted =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFFEDF5FC),
                contentColor = Color(0xFF24364A),
                borderColor = Color(0xFFC2D4E6),
                accentColor = Color(0xFF557596),
            ),
        reminderDenied =
            CatholicFastingSemanticTone(
                containerColor = Color(0xFFFFF1EE),
                contentColor = Color(0xFF4A2D2A),
                borderColor = Color(0xFFE4C4BD),
                accentColor = Color(0xFFA8665C),
            ),
    )
