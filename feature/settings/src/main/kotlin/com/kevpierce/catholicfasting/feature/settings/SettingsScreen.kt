package com.kevpierce.catholicfasting.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.model.AscensionObservance
import com.kevpierce.catholicfasting.core.model.CalendarMode
import com.kevpierce.catholicfasting.core.model.FridayOutsideLentMode
import com.kevpierce.catholicfasting.core.model.RegionProfile
import com.kevpierce.catholicfasting.core.model.RuleSettings
import com.kevpierce.catholicfasting.core.ui.CatholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: RuleSettings,
    onSettingsChange: (RuleSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = CatholicFastingThemeValues.spacing
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        CatholicFastingScreenTitle(stringResource(R.string.settings_more_title))
        CatholicFastingSectionCard(title = stringResource(R.string.settings_profile_norms)) {
            SettingsForm(
                settings = settings,
                onSettingsChange = onSettingsChange,
            )
        }
    }
}

@Composable
private fun SettingsForm(
    settings: RuleSettings,
    onSettingsChange: (RuleSettings) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsEnumSections(
            settings = settings,
            onSettingsChange = onSettingsChange,
        )
        SettingsToggleSection(
            settings = settings,
            onSettingsChange = onSettingsChange,
        )
        BirthYearField(
            birthYear = settings.birthYear,
            onBirthYearChange = {
                onSettingsChange(settings.copy(birthYear = it))
            },
        )
    }
}

@Composable
private fun SettingsEnumSections(
    settings: RuleSettings,
    onSettingsChange: (RuleSettings) -> Unit,
) {
    SettingsEnumSection(
        title = stringResource(R.string.settings_region),
        options = RegionProfile.entries,
        selected = settings.regionProfile,
        labelFor = { it.localizedLabel() },
        onSelect = { onSettingsChange(settings.copy(regionProfile = it)) },
    )
    SettingsEnumSection(
        title = stringResource(R.string.settings_calendar),
        options = CalendarMode.entries,
        selected = settings.calendarMode,
        labelFor = { it.localizedLabel() },
        onSelect = { onSettingsChange(settings.copy(calendarMode = it)) },
    )
    SettingsEnumSection(
        title = stringResource(R.string.settings_friday_mode),
        options = FridayOutsideLentMode.entries,
        selected = settings.fridayOutsideLentMode,
        labelFor = { it.localizedLabel() },
        onSelect = { onSettingsChange(settings.copy(fridayOutsideLentMode = it)) },
    )
    SettingsEnumSection(
        title = stringResource(R.string.settings_ascension_observance),
        options = AscensionObservance.entries,
        selected = settings.ascensionObservance,
        labelFor = { it.localizedLabel() },
        onSelect = { onSettingsChange(settings.copy(ascensionObservance = it)) },
    )
}

@Composable
private fun SettingsToggleSection(
    settings: RuleSettings,
    onSettingsChange: (RuleSettings) -> Unit,
) {
    ToggleRow(
        title = stringResource(R.string.settings_age_14),
        checked = settings.isAge14OrOlderForAbstinence,
        onCheckedChange = {
            onSettingsChange(settings.copy(isAge14OrOlderForAbstinence = it))
        },
    )
    ToggleRow(
        title = stringResource(R.string.settings_age_18),
        checked = settings.isAge18OrOlderForFasting,
        onCheckedChange = {
            onSettingsChange(settings.copy(isAge18OrOlderForFasting = it))
        },
    )
    ToggleRow(
        title = stringResource(R.string.settings_medical_dispensation),
        checked = settings.hasMedicalDispensation,
        onCheckedChange = {
            onSettingsChange(settings.copy(hasMedicalDispensation = it))
        },
    )
}

@Composable
private fun <T> SettingsEnumSection(
    title: String,
    options: List<T>,
    selected: T,
    labelFor: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Text(title, style = CatholicFastingThemeValues.typography.supporting)
    EnumChips(
        options = options,
        selected = selected,
        labelFor = labelFor,
        onSelect = onSelect,
    )
}

@Composable
private fun BirthYearField(
    birthYear: Int,
    onBirthYearChange: (Int) -> Unit,
) {
    OutlinedTextField(
        value = birthYear.takeIf { it > 0 }?.toString().orEmpty(),
        onValueChange = { value ->
            onBirthYearChange(value.toIntOrNull() ?: 0)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.settings_birth_year)) },
    )
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> EnumChips(
    options: List<T>,
    selected: T,
    labelFor: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val label = labelFor(option)
            val selectedState =
                stringResource(
                    if (option == selected) {
                        R.string.settings_accessibility_selected
                    } else {
                        R.string.settings_accessibility_not_selected
                    },
                )
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                modifier =
                    Modifier.semantics {
                        contentDescription = label
                        stateDescription = selectedState
                    },
                label = { Text(label) },
            )
        }
    }
}
