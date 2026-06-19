package com.kevpierce.catholicfasting.feature.premium

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.PremiumReflection
import com.kevpierce.catholicfasting.core.model.ReflectionJournalEntry
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues

@Composable
internal fun ReflectionJournalCard(
    reflections: List<ReflectionJournalEntry>,
    prompt: PremiumReflection,
    onSaveReflection: (String, String) -> String,
    onStatus: (String) -> Unit,
) {
    var reflectionTitle by remember { mutableStateOf("") }
    var reflectionBody by remember { mutableStateOf("") }

    WorkspaceCard(title = stringResource(R.string.premium_reflection_journal_title)) {
        Text(prompt.title, style = CatholicFastingThemeValues.typography.sectionTitle)
        Text(prompt.body, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(R.string.premium_suggested_action_value, prompt.action),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        OutlinedTextField(
            value = reflectionTitle,
            onValueChange = { reflectionTitle = it },
            label = { Text(stringResource(R.string.premium_reflection_title_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = reflectionBody,
            onValueChange = { reflectionBody = it },
            label = { Text(stringResource(R.string.premium_reflection_body_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                onStatus(onSaveReflection(reflectionTitle, reflectionBody))
                reflectionTitle = ""
                reflectionBody = ""
            },
        ) {
            Text(stringResource(R.string.premium_save_reflection))
        }
        reflections.take(3).forEach { reflection ->
            Text(reflection.title, style = CatholicFastingThemeValues.typography.supporting)
            if (reflection.body.isNotBlank()) {
                Text(reflection.body, style = CatholicFastingThemeValues.typography.utility)
            }
        }
    }
}
