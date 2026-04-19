package com.kevpierce.catholicfasting.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

@Composable
fun catholicFastingScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = CatholicFastingThemeValues.typography.screenTitle,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
fun catholicFastingSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardDefaults = CatholicFastingThemeValues.cardDefaults
    val spacing = CatholicFastingThemeValues.spacing
    val titleStyle =
        if (heroTitle) {
            CatholicFastingThemeValues.typography.heroTitle
        } else {
            CatholicFastingThemeValues.typography.sectionTitle
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            if (tone != null) {
                CardDefaults.cardColors(
                    containerColor = tone.containerColor,
                    contentColor = tone.contentColor,
                )
            } else {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            },
        border =
            BorderStroke(
                width = cardDefaults.borderWidth,
                color = tone?.borderColor ?: MaterialTheme.colorScheme.outlineVariant,
            ),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides
                (
                    tone?.contentColor ?: MaterialTheme.colorScheme.onSurface
                ),
        ) {
            Column(
                modifier = Modifier.padding(cardDefaults.contentPadding),
                verticalArrangement = Arrangement.spacedBy(cardDefaults.contentSpacing),
            ) {
                Text(
                    title,
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
}
