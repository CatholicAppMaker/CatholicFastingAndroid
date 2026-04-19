package com.kevpierce.catholicfasting.core.ui

import androidx.compose.ui.text.font.FontFamily
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CatholicFastingTypographyTest {
    @Test
    fun typographyRolesKeepEditorialSerifLimitedToHeroTitle() {
        val typography =
            CatholicFastingTypography(
                heroTitle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif),
                screenTitle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif),
                sectionTitle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif),
                body = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif),
                supporting = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif),
                utility = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif),
            )

        assertThat(typography.heroTitle.fontFamily).isEqualTo(FontFamily.Serif)
        assertThat(typography.screenTitle.fontFamily).isEqualTo(FontFamily.SansSerif)
        assertThat(typography.sectionTitle.fontFamily).isEqualTo(FontFamily.SansSerif)
        assertThat(typography.body.fontFamily).isEqualTo(FontFamily.SansSerif)
        assertThat(typography.supporting.fontFamily).isEqualTo(FontFamily.SansSerif)
        assertThat(typography.utility.fontFamily).isEqualTo(FontFamily.SansSerif)
    }
}
