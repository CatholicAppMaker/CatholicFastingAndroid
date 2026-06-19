@file:Suppress("MatchingDeclarationName", "TooManyFunctions")

package com.kevpierce.catholicfasting.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.CatholicFastingQuote
import com.kevpierce.catholicfasting.core.model.CompanionNextAction
import com.kevpierce.catholicfasting.core.model.CompanionSnapshot
import com.kevpierce.catholicfasting.core.model.FastProgressState
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.SacredImageryItem
import com.kevpierce.catholicfasting.core.model.SeasonalContentPack
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.SacredImageryCard
import com.kevpierce.catholicfasting.core.ui.SeasonTone
import com.kevpierce.catholicfasting.core.ui.rememberSeasonTone

data class TodayUiState(
    val todayObservance: Observance?,
    val companionSnapshot: CompanionSnapshot,
    val completionSummary: String,
    val premiumSnapshot: PremiumSnapshot,
    val seasonalContentPack: SeasonalContentPack,
    val dailyFormationLine: String,
    val dailyQuote: CatholicFastingQuote,
    val devotionalGallery: List<SacredImageryItem>,
    val setupProgressSummary: String,
    val yearPlanSummary: String,
    val weeklyRecap: String,
    val streakMessage: String,
    val noticeSummary: String,
)

const val TODAY_COMPANION_ACTION_TEST_TAG_PREFIX = "today-companion-action-"

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onCompanionAction: (CompanionNextAction) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val todayDetail =
        uiState.todayObservance?.detail ?: stringResource(R.string.today_default_detail)
    val spacing = CatholicFastingThemeValues.spacing

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        TodayContent(
            uiState = uiState,
            todayDetail = todayDetail,
            onCompanionAction = onCompanionAction,
        )
    }
}

@Composable
private fun ColumnScope.TodayContent(
    uiState: TodayUiState,
    todayDetail: String,
    onCompanionAction: (CompanionNextAction) -> Unit,
) {
    val seasonTone = rememberSeasonTone(uiState.premiumSnapshot.season)

    CatholicFastingScreenTitle(stringResource(R.string.today_title))
    CompanionCard(
        snapshot = uiState.companionSnapshot,
        seasonTone = seasonTone,
        onCompanionAction = onCompanionAction,
    )
    ObservanceSummaryCard(uiState, todayDetail)
    SeasonalFormationCard(uiState, seasonTone)
    YearPlanCard(uiState)
    PersonalInsightsCard(uiState)
    SeasonPlanCard(uiState, seasonTone)
    RecoveryCoachCard(uiState)
    DevotionalGalleryCard(uiState)
    NoticeCard(uiState)
}

@Composable
private fun CompanionCard(
    snapshot: CompanionSnapshot,
    seasonTone: SeasonTone,
    onCompanionAction: (CompanionNextAction) -> Unit,
) {
    TodayCard(
        title = stringResource(R.string.today_companion_title),
        tone = seasonTone,
        heroTitle = true,
    ) {
        Text(snapshot.ruleDecision.obligationLine, style = CatholicFastingThemeValues.typography.sectionTitle)
        Text(snapshot.ruleDecision.rationale, style = CatholicFastingThemeValues.typography.body)
        Text(snapshot.ruleDecision.sourceLine, style = CatholicFastingThemeValues.typography.utility)
        LiveFastLine(snapshot)
        Text(
            text = snapshot.formation.recoverySummary ?: snapshot.formation.completionSummary,
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            text =
                stringResource(
                    R.string.today_companion_formation_value,
                    snapshot.formation.seasonLabel,
                    snapshot.formation.nextJourneyActionTitle,
                ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        CompanionActionButtons(
            primaryAction = snapshot.primaryAction,
            secondaryActions = snapshot.secondaryActions,
            onCompanionAction = onCompanionAction,
        )
    }
}

@Composable
private fun LiveFastLine(snapshot: CompanionSnapshot) {
    val progress = snapshot.liveFast.progress
    val intentionLabel =
        snapshot.liveFast.intentionId
            ?.let { id -> IntermittentFastIntention.entries.firstOrNull { it.name == id }?.label }
    val detail =
        when (progress) {
            is FastProgressState.Active ->
                stringResource(
                    R.string.today_companion_active_fast_value,
                    formatDuration(progress.elapsedSeconds),
                    formatDuration(progress.remainingSeconds),
                )
            is FastProgressState.TargetReached ->
                stringResource(
                    R.string.today_companion_target_reached_value,
                    formatDuration(progress.elapsedSeconds),
                )
            is FastProgressState.CompletedRecap ->
                stringResource(
                    R.string.today_companion_recap_value,
                    progress.recap.title,
                    progress.recap.suggestedNextAction,
                )
            is FastProgressState.EatingWindow ->
                stringResource(
                    R.string.today_companion_eating_window_value,
                    formatDuration(progress.windowRemainingSeconds),
                )
            is FastProgressState.Inactive ->
                progress.detail
        }
    Text(
        text =
            if (intentionLabel == null) {
                detail
            } else {
                stringResource(R.string.today_companion_intention_value, detail, intentionLabel)
            },
        style = CatholicFastingThemeValues.typography.body,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompanionActionButtons(
    primaryAction: CompanionNextAction,
    secondaryActions: List<CompanionNextAction>,
    onCompanionAction: (CompanionNextAction) -> Unit,
) {
    val spacing = CatholicFastingThemeValues.spacing
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        Button(
            onClick = { onCompanionAction(primaryAction) },
            modifier = Modifier.testTag(TODAY_COMPANION_ACTION_TEST_TAG_PREFIX + primaryAction.id),
        ) {
            Text(primaryAction.title)
        }
        secondaryActions.take(2).forEach { action ->
            OutlinedButton(
                onClick = { onCompanionAction(action) },
                modifier = Modifier.testTag(TODAY_COMPANION_ACTION_TEST_TAG_PREFIX + action.id),
            ) {
                Text(action.title)
            }
        }
    }
}

@Composable
private fun TodayCard(
    title: String,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    CatholicFastingSectionCard(
        title = title,
        tone = tone,
        heroTitle = heroTitle,
        content = content,
    )
}

@Composable
private fun ObservanceSummaryCard(
    uiState: TodayUiState,
    todayDetail: String,
) {
    TodayCard(title = uiState.todayObservance?.title ?: stringResource(R.string.today_no_observance)) {
        Text(todayDetail, style = CatholicFastingThemeValues.typography.body)
        Text(
            uiState.completionSummary,
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@Composable
private fun SeasonalFormationCard(
    uiState: TodayUiState,
    seasonTone: SeasonTone,
) {
    TodayCard(
        title = uiState.seasonalContentPack.campaignTitle,
        tone = seasonTone,
        heroTitle = true,
    ) {
        Text(
            uiState.seasonalContentPack.campaignSubtitle,
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        Text(uiState.dailyFormationLine, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(R.string.today_quote_value, uiState.dailyQuote.text),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            stringResource(
                R.string.today_quote_author_value,
                uiState.dailyQuote.author,
                uiState.dailyQuote.tradition,
            ),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}

@Composable
private fun YearPlanCard(uiState: TodayUiState) {
    TodayCard(title = stringResource(R.string.today_year_plan_title)) {
        Text(uiState.yearPlanSummary, style = CatholicFastingThemeValues.typography.body)
        Text(uiState.weeklyRecap, style = CatholicFastingThemeValues.typography.supporting)
        Text(uiState.setupProgressSummary, style = CatholicFastingThemeValues.typography.supporting)
    }
}

@Composable
private fun PersonalInsightsCard(uiState: TodayUiState) {
    TodayCard(title = stringResource(R.string.today_personal_insights_title)) {
        Text(uiState.streakMessage, style = CatholicFastingThemeValues.typography.body)
        Text(uiState.premiumSnapshot.motivationLine, style = CatholicFastingThemeValues.typography.supporting)
        Text(
            uiState.premiumSnapshot.reminderRecommendation.summaryLine,
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@Composable
private fun SeasonPlanCard(
    uiState: TodayUiState,
    seasonTone: SeasonTone,
) {
    TodayCard(
        title = uiState.premiumSnapshot.seasonPlan.titleLine,
        tone = seasonTone,
    ) {
        Text(
            text =
                stringResource(
                    R.string.today_season_motivation_value,
                    uiState.premiumSnapshot.season.localizedLabel(),
                    uiState.premiumSnapshot.motivationLine,
                ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(uiState.premiumSnapshot.seasonPlan.focusLine, style = CatholicFastingThemeValues.typography.body)
        uiState.premiumSnapshot.seasonPlan.practices.forEach { practice ->
            Text(
                stringResource(R.string.today_bullet_value, practice),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
    }
}

@Composable
private fun RecoveryCoachCard(uiState: TodayUiState) {
    TodayCard(title = uiState.premiumSnapshot.recoveryCoachPlan.title) {
        Text(uiState.premiumSnapshot.recoveryCoachPlan.summary, style = CatholicFastingThemeValues.typography.body)
        uiState.premiumSnapshot.recoveryCoachPlan.steps.take(3).forEach { step ->
            Text(
                stringResource(R.string.today_bullet_value, step),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
        Text(
            text = uiState.premiumSnapshot.reflection.title,
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
        Text(uiState.premiumSnapshot.reflection.body, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(
                R.string.today_action_value,
                uiState.premiumSnapshot.reflection.action,
            ),
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DevotionalGalleryCard(uiState: TodayUiState) {
    val spacing = CatholicFastingThemeValues.spacing

    TodayCard(title = stringResource(R.string.today_devotional_gallery_title)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
            verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
        ) {
            uiState.devotionalGallery.take(6).forEach { item ->
                SacredImageryCard(item = item)
            }
        }
    }
}

@Composable
private fun NoticeCard(uiState: TodayUiState) {
    TodayCard(title = stringResource(R.string.today_important_notice_title)) {
        Text(uiState.noticeSummary, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(R.string.today_notice_body),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    return "${hours}h ${minutes}m"
}
