@file:Suppress("MatchingDeclarationName", "TooManyFunctions")

package com.kevpierce.catholicfasting.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kevpierce.catholicfasting.core.model.CatholicFastingQuote
import com.kevpierce.catholicfasting.core.model.CompanionNextAction
import com.kevpierce.catholicfasting.core.model.CompanionSnapshot
import com.kevpierce.catholicfasting.core.model.FastProgressState
import com.kevpierce.catholicfasting.core.model.IntermittentFastIntention
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.SacredImageryItem
import com.kevpierce.catholicfasting.core.model.SeasonalContentPack
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingEyebrow
import com.kevpierce.catholicfasting.core.ui.CatholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSeasonBadge
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSection
import com.kevpierce.catholicfasting.core.ui.CatholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.SacredImageryRail
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
const val TODAY_LIST_TEST_TAG = "today-content-list"
const val TODAY_HEADER_TEST_TAG = "today-section-header"
const val TODAY_COMPANION_TEST_TAG = "today-section-companion"
const val TODAY_OBSERVANCE_TEST_TAG = "today-section-observance"
const val TODAY_SEASONAL_FORMATION_TEST_TAG = "today-section-seasonal-formation"
const val TODAY_JOURNEY_TEST_TAG = "today-section-journey"
const val TODAY_RECOVERY_TEST_TAG = "today-section-recovery"
const val TODAY_GALLERY_TEST_TAG = "today-section-gallery"
const val TODAY_NOTICE_TEST_TAG = "today-section-notice"

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    modifier: Modifier = Modifier,
    onCompanionAction: (CompanionNextAction) -> Unit = {},
) {
    val todayDetail =
        uiState.todayObservance?.detail ?: stringResource(R.string.today_default_detail)
    val spacing = CatholicFastingThemeValues.spacing
    val seasonTone = rememberSeasonTone(uiState.premiumSnapshot.season)

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(TODAY_LIST_TEST_TAG),
        contentPadding = PaddingValues(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        todayContent(
            uiState = uiState,
            todayDetail = todayDetail,
            seasonTone = seasonTone,
            onCompanionAction = onCompanionAction,
        )
    }
}

private fun LazyListScope.todayContent(
    uiState: TodayUiState,
    todayDetail: String,
    seasonTone: SeasonTone,
    onCompanionAction: (CompanionNextAction) -> Unit,
) {
    item(key = TODAY_HEADER_TEST_TAG) {
        TodaySectionItem(TODAY_HEADER_TEST_TAG) {
            TodayHeader(uiState = uiState, seasonTone = seasonTone)
        }
    }
    item(key = TODAY_COMPANION_TEST_TAG) {
        TodaySectionItem(TODAY_COMPANION_TEST_TAG) {
            CompanionCard(
                snapshot = uiState.companionSnapshot,
                seasonTone = seasonTone,
                onCompanionAction = onCompanionAction,
            )
        }
    }
    item(key = TODAY_OBSERVANCE_TEST_TAG) {
        TodaySectionItem(TODAY_OBSERVANCE_TEST_TAG) {
            ObservanceSummaryCard(uiState, todayDetail)
        }
    }
    item(key = TODAY_SEASONAL_FORMATION_TEST_TAG) {
        TodaySectionItem(TODAY_SEASONAL_FORMATION_TEST_TAG) {
            SeasonalFormationCard(uiState, seasonTone)
        }
    }
    item(key = TODAY_JOURNEY_TEST_TAG) {
        TodaySectionItem(TODAY_JOURNEY_TEST_TAG) {
            JourneySnapshotSection(uiState, seasonTone)
        }
    }
    item(key = TODAY_RECOVERY_TEST_TAG) {
        TodaySectionItem(TODAY_RECOVERY_TEST_TAG) {
            RecoveryCoachCard(uiState)
        }
    }
    item(key = TODAY_GALLERY_TEST_TAG) {
        TodaySectionItem(TODAY_GALLERY_TEST_TAG) {
            DevotionalGalleryCard(uiState)
        }
    }
    item(key = TODAY_NOTICE_TEST_TAG) {
        TodaySectionItem(TODAY_NOTICE_TEST_TAG) {
            NoticeCard(uiState)
        }
    }
}

@Composable
private fun TodaySectionItem(
    testTag: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.testTag(testTag)) { content() }
}

@Composable
private fun TodayHeader(
    uiState: TodayUiState,
    seasonTone: SeasonTone,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CatholicFastingScreenTitle(stringResource(R.string.today_title))
        CatholicFastingSeasonBadge(
            label = uiState.premiumSnapshot.season.localizedLabel(),
            tone = seasonTone,
        )
    }
}

@Composable
private fun CompanionCard(
    snapshot: CompanionSnapshot,
    seasonTone: SeasonTone,
    onCompanionAction: (CompanionNextAction) -> Unit,
) {
    TodayCard(
        title = snapshot.ruleDecision.obligationLine,
        tone = seasonTone,
        heroTitle = true,
    ) {
        CatholicFastingEyebrow(
            text = stringResource(R.string.today_companion_title),
            color = seasonTone.accentColor,
        )
        Text(snapshot.ruleDecision.rationale, style = CatholicFastingThemeValues.typography.body)
        HorizontalDivider(color = seasonTone.borderColor.copy(alpha = 0.72f))
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
        Text(snapshot.ruleDecision.sourceLine, style = CatholicFastingThemeValues.typography.utility)
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
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xSmall)) {
        Button(
            onClick = { onCompanionAction(primaryAction) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag(TODAY_COMPANION_ACTION_TEST_TAG_PREFIX + primaryAction.id),
        ) {
            Text(primaryAction.title)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
            verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
        ) {
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
    TodaySection(title = uiState.todayObservance?.title ?: stringResource(R.string.today_no_observance)) {
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
private fun JourneySnapshotSection(
    uiState: TodayUiState,
    seasonTone: SeasonTone,
) {
    TodaySection(title = stringResource(R.string.today_journey_snapshot_title)) {
        Text(
            text = stringResource(R.string.today_year_plan_title),
            style = CatholicFastingThemeValues.typography.sectionTitle,
            modifier = Modifier.semantics { heading() },
        )
        Text(uiState.yearPlanSummary, style = CatholicFastingThemeValues.typography.body)
        Text(uiState.weeklyRecap, style = CatholicFastingThemeValues.typography.supporting)
        Text(uiState.setupProgressSummary, style = CatholicFastingThemeValues.typography.supporting)
        Text(
            text = stringResource(R.string.today_personal_insights_title),
            style = CatholicFastingThemeValues.typography.sectionTitle,
            modifier = Modifier.semantics { heading() },
        )
        Text(uiState.streakMessage, style = CatholicFastingThemeValues.typography.body)
        Text(uiState.premiumSnapshot.motivationLine, style = CatholicFastingThemeValues.typography.supporting)
        Text(
            uiState.premiumSnapshot.reminderRecommendation.summaryLine,
            style = CatholicFastingThemeValues.typography.supporting,
        )
        Text(
            text = uiState.premiumSnapshot.seasonPlan.titleLine,
            style = CatholicFastingThemeValues.typography.sectionTitle,
            color = seasonTone.accentColor,
            modifier = Modifier.semantics { heading() },
        )
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

@Composable
private fun DevotionalGalleryCard(uiState: TodayUiState) {
    SacredImageryRail(
        title = stringResource(R.string.today_devotional_gallery_title),
        imagery = uiState.devotionalGallery.take(6),
    )
}

@Composable
private fun NoticeCard(uiState: TodayUiState) {
    TodaySection(title = stringResource(R.string.today_important_notice_title)) {
        Text(uiState.noticeSummary, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(R.string.today_notice_body),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}

@Composable
private fun TodaySection(
    title: String,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    CatholicFastingSection(
        title = title,
        tone = tone,
        heroTitle = heroTitle,
        content = content,
    )
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    return "${hours}h ${minutes}m"
}
