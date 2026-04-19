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
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.model.CatholicFastingQuote
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.SacredImageryItem
import com.kevpierce.catholicfasting.core.model.SeasonalContentPack
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfasting.core.ui.SeasonTone
import com.kevpierce.catholicfasting.core.ui.catholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.catholicFastingSectionCard
import com.kevpierce.catholicfasting.core.ui.rememberSeasonTone

data class TodayUiState(
    val todayObservance: Observance?,
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

@Composable
fun todayScreen(
    uiState: TodayUiState,
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
        todayContent(
            uiState = uiState,
            todayDetail = todayDetail,
        )
    }
}

@Composable
private fun ColumnScope.todayContent(
    uiState: TodayUiState,
    todayDetail: String,
) {
    val seasonTone = rememberSeasonTone(uiState.premiumSnapshot.season)

    catholicFastingScreenTitle(stringResource(R.string.today_title))
    observanceSummaryCard(uiState, todayDetail)
    seasonalFormationCard(uiState, seasonTone)
    yearPlanCard(uiState)
    personalInsightsCard(uiState)
    seasonPlanCard(uiState, seasonTone)
    recoveryCoachCard(uiState)
    devotionalGalleryCard(uiState)
    noticeCard(uiState)
}

@Composable
private fun todayCard(
    title: String,
    tone: SeasonTone? = null,
    heroTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    catholicFastingSectionCard(
        title = title,
        tone = tone,
        heroTitle = heroTitle,
        content = content,
    )
}

@Composable
private fun observanceSummaryCard(
    uiState: TodayUiState,
    todayDetail: String,
) {
    todayCard(title = uiState.todayObservance?.title ?: stringResource(R.string.today_no_observance)) {
        Text(todayDetail, style = CatholicFastingThemeValues.typography.body)
        Text(
            uiState.completionSummary,
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@Composable
private fun seasonalFormationCard(
    uiState: TodayUiState,
    seasonTone: SeasonTone,
) {
    todayCard(
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
private fun yearPlanCard(uiState: TodayUiState) {
    todayCard(title = stringResource(R.string.today_year_plan_title)) {
        Text(uiState.yearPlanSummary, style = CatholicFastingThemeValues.typography.body)
        Text(uiState.weeklyRecap, style = CatholicFastingThemeValues.typography.supporting)
        Text(uiState.setupProgressSummary, style = CatholicFastingThemeValues.typography.supporting)
    }
}

@Composable
private fun personalInsightsCard(uiState: TodayUiState) {
    todayCard(title = stringResource(R.string.today_personal_insights_title)) {
        Text(uiState.streakMessage, style = CatholicFastingThemeValues.typography.body)
        Text(uiState.premiumSnapshot.motivationLine, style = CatholicFastingThemeValues.typography.supporting)
        Text(
            uiState.premiumSnapshot.reminderRecommendation.summaryLine,
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
}

@Composable
private fun seasonPlanCard(
    uiState: TodayUiState,
    seasonTone: SeasonTone,
) {
    todayCard(
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
private fun recoveryCoachCard(uiState: TodayUiState) {
    todayCard(title = uiState.premiumSnapshot.recoveryCoachPlan.title) {
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
private fun devotionalGalleryCard(uiState: TodayUiState) {
    val spacing = CatholicFastingThemeValues.spacing

    todayCard(title = stringResource(R.string.today_devotional_gallery_title)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
            verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
        ) {
            uiState.devotionalGallery.take(6).forEach { item ->
                Card {
                    Column(
                        modifier = Modifier.padding(spacing.small),
                        verticalArrangement = Arrangement.spacedBy(spacing.xxSmall),
                    ) {
                        Text(item.title, style = CatholicFastingThemeValues.typography.supporting)
                        Text(item.subtitle, style = CatholicFastingThemeValues.typography.utility)
                    }
                }
            }
        }
    }
}

@Composable
private fun noticeCard(uiState: TodayUiState) {
    todayCard(title = stringResource(R.string.today_important_notice_title)) {
        Text(uiState.noticeSummary, style = CatholicFastingThemeValues.typography.body)
        Text(
            stringResource(R.string.today_notice_body),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}
