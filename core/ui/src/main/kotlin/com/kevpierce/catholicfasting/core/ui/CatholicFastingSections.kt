package com.kevpierce.catholicfasting.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun CatholicFastingSection(
    title: String,
    modifier: Modifier = Modifier,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = CatholicFastingThemeValues.spacing
    val titleStyle =
        if (heroTitle) {
            CatholicFastingThemeValues.typography.heroTitle
        } else {
            CatholicFastingThemeValues.typography.sectionTitle
        }
    val toneModifier =
        tone?.let { seasonTone ->
            Modifier
                .background(
                    color = seasonTone.containerColor,
                    shape = RoundedCornerShape(20.dp),
                ).padding(spacing.medium)
        } ?: Modifier
    val sectionModifier = modifier.fillMaxWidth().then(toneModifier)

    CompositionLocalProvider(
        LocalContentColor provides (tone?.contentColor ?: MaterialTheme.colorScheme.onBackground),
    ) {
        Column(
            modifier = sectionModifier,
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = title,
                style = titleStyle,
                modifier = Modifier.semantics { heading() },
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
                content = content,
            )
        }
    }
}
