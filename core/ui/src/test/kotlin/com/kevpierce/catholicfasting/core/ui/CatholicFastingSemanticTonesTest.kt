package com.kevpierce.catholicfasting.core.ui

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CatholicFastingSemanticTonesTest {
    @Test
    fun semanticTonesExposeDistinctProductStateRoles() {
        val tones =
            CatholicFastingSemanticTones(
                ruleSource = sampleTone(0xFFF7F5ED),
                trackerActive = sampleTone(0xFFEAF7F4),
                trackerInactive = sampleTone(0xFFF2F4F5),
                premiumLocked = sampleTone(0xFFFFF6E4),
                premiumUnlocked = sampleTone(0xFFECF8EE),
                reminderGranted = sampleTone(0xFFEDF5FC),
                reminderDenied = sampleTone(0xFFFFF1EE),
            )

        assertThat(tones.ruleSource.containerColor)
            .isNotEqualTo(tones.trackerActive.containerColor)
        assertThat(tones.trackerActive.containerColor)
            .isNotEqualTo(tones.trackerInactive.containerColor)
        assertThat(tones.premiumLocked.containerColor)
            .isNotEqualTo(tones.premiumUnlocked.containerColor)
        assertThat(tones.reminderGranted.containerColor)
            .isNotEqualTo(tones.reminderDenied.containerColor)
    }

    private fun sampleTone(container: Long) =
        CatholicFastingSemanticTone(
            containerColor = Color(container),
            contentColor = Color(0xFF111111),
            borderColor = Color(0xFF999999),
            accentColor = Color(0xFF555555),
        )
}
