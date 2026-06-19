package com.kevpierce.catholicfasting.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

@Composable
fun CatholicFastingScreenTitle(
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
fun CatholicFastingSectionCard(
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
        shape = RoundedCornerShape(cardDefaults.cornerRadius),
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

@Composable
fun CatholicFastingSemanticCard(
    title: String,
    tone: CatholicFastingSemanticTone,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CatholicFastingSectionCard(
        title = title,
        modifier = modifier,
        tone =
            SeasonTone(
                containerColor = tone.containerColor,
                contentColor = tone.contentColor,
                borderColor = tone.borderColor,
                accentColor = tone.accentColor,
            ),
        content = content,
    )
}

@Composable
fun CatholicFastingCompanionCard(
    title: String,
    tone: SeasonTone,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CatholicFastingSectionCard(
        title = title,
        modifier = modifier,
        tone = tone,
        heroTitle = true,
        content = content,
    )
}

@Composable
fun CatholicFastingActionRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.xSmall),
        content = content,
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun CatholicFastingStatusChips(
    labels: List<String>,
    selectedLabel: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.xSmall),
    ) {
        labels.forEach { label ->
            FilterChip(
                selected = label == selectedLabel,
                onClick = { onSelected(label) },
                label = {
                    Text(label, style = CatholicFastingThemeValues.typography.utility)
                },
            )
        }
    }
}

@Composable
fun CatholicFastingReminderControlPreviewSurface(
    permissionGranted: Boolean,
    modifier: Modifier = Modifier,
) {
    val tones = CatholicFastingThemeValues.semanticTones
    val tone = if (permissionGranted) tones.reminderGranted else tones.reminderDenied

    CatholicFastingSemanticCard(
        title = if (permissionGranted) "Reminders ready" else "Reminders need permission",
        tone = tone,
        modifier = modifier,
    ) {
        Text(
            text =
                if (permissionGranted) {
                    "Evening planning reminders are enabled."
                } else {
                    "Open Android notification settings to enable reminders."
                },
            style = CatholicFastingThemeValues.typography.body,
        )
        CatholicFastingActionRow {
            Button(onClick = {}) {
                Text(if (permissionGranted) "Review" else "Open settings")
            }
            OutlinedButton(onClick = {}) {
                Text("Later")
            }
        }
    }
}

@Composable
fun CatholicFastingTrackerControlPreviewSurface(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val tones = CatholicFastingThemeValues.semanticTones
    val tone = if (active) tones.trackerActive else tones.trackerInactive
    var intention by remember { mutableStateOf("For clarity and charity.") }

    CatholicFastingSemanticCard(
        title = if (active) "Fast in progress" else "Prepare a fast",
        tone = tone,
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = intention,
            onValueChange = { intention = it },
            label = { Text("Intention") },
            modifier = Modifier.fillMaxWidth(),
        )
        CatholicFastingActionRow {
            Button(onClick = {}) {
                Text(if (active) "End fast" else "Start fast")
            }
            OutlinedButton(onClick = {}) {
                Text(if (active) "Cancel" else "Save")
            }
        }
    }
}

@Composable
fun CatholicFastingPremiumSummaryPreviewSurface(
    unlocked: Boolean,
    modifier: Modifier = Modifier,
) {
    val tones = CatholicFastingThemeValues.semanticTones
    val tone = if (unlocked) tones.premiumUnlocked else tones.premiumLocked

    CatholicFastingSemanticCard(
        title = if (unlocked) "Premium active" else "Premium planning",
        tone = tone,
        modifier = modifier,
    ) {
        Text(
            text =
                if (unlocked) {
                    "Seasonal planning and recovery prompts are available."
                } else {
                    "Subscribe with Google Play to unlock companion planning."
                },
            style = CatholicFastingThemeValues.typography.body,
        )
        AssistChip(
            onClick = {},
            label = {
                Text(
                    if (unlocked) "Manage subscription" else "Review plans",
                    style = CatholicFastingThemeValues.typography.utility,
                )
            },
        )
    }
}
